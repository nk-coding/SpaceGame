package com.nkcoding.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import java.util.Optional;


public class CodeEditor extends WidgetGroup {

    private final Color lineNumberColor = new Color(0x808080ff);
    private final Color highlightLineNumberColor = new Color(0xffffffff);
    /**
     * <code>ScrollPane</code> which contains the <code>MultiColorTextArea</code> which contains the code
     */
    private ScrollPane codeScrollPane;
    /**
     * <code>MultiColorTextArea</code> which contains the code
     */
    private MultiColorTextArea codeTextArea;
    /**
     * for the clip of the line numbers area
     */
    private Rectangle numbersAreaClip;
    /**
     * font for the line numbers
     */
    private BitmapFont font;
    /**
     * style for the MultiColorTextArea
     */
    private CodeEditorStyle codeEditorStyle;
    private float numberCharWidth;

    private int maxLineNumberCharCount = 0;

    /**
     * default constructor
     *
     * @param codeEditorStyle the style for this control
     */
    public CodeEditor(CodeEditorStyle codeEditorStyle) {
        this.codeEditorStyle = codeEditorStyle;
        codeTextArea = new MultiColorTextArea("//you can write your code here", codeEditorStyle.createTextFieldStyle()) {
            @Override
            public Optional<Boolean> preInput(InputEvent event, char character) {
                //correct tab
                if (character == '\t') {
                    System.out.println("TAB");
                    paste("   ", true, true);
                    return Optional.of(true);
                } else if (character == ')') {
                    boolean endBracketExists = text.length() > getCursorPosition() && text.charAt(getCursorPosition()) == ')';
                    if (endBracketExists) {
                        moveCursor(true, false);
                        return Optional.of(true);
                    }
                } else if (character == ']') {
                    boolean endBracketExists = text.length() > getCursorPosition() && text.charAt(getCursorPosition()) == ']';
                    if (endBracketExists) {
                        moveCursor(true, false);
                        return Optional.of(true);
                    }
                }
                return Optional.empty();
            }

            @Override
            public boolean postInput(InputEvent event, char character) {
                switch (character) {
                    case '{':
                        paste("}", false, false);
                        return true;
                    case ENTER_ANDROID:
                    case ENTER_DESKTOP:
                        //region
                        String lastLine = getTextLine(getCursorLine());
                        int spaces = calculateSpaceChars(lastLine);
                        //determine if it was after a '{'
                        if (getCursorPosition() > 1 && text.charAt(getCursorPosition() - 2) == '{') {
                            //check if there is a end bracket
                            boolean endBracketExists = text.length() > getCursorPosition() && text.charAt(getCursorPosition()) == '}';
                            paste(" ".repeat(spaces + 3), false, true);
                            if (endBracketExists) paste("\n" + " ".repeat(spaces), false, false);
                        } else {
                            // just a normal new line
                            paste(" ".repeat(spaces), false, true);
                        }
                        //endregion
                        return true;
                    case '(':
                        paste(")", false, false);
                        return true;
                    case '[':
                        paste("]", false, false);
                        return true;
                    default:
                        return false;
                }
            }
        };
        codeTextArea.setFocusTraversal(false);
        codeScrollPane = new ScrollPane(codeTextArea, codeEditorStyle.createScrollPaneStyle());
        codeScrollPane.setOverscroll(false, false);
        //set attributes on ScrollPane
        codeScrollPane.setFadeScrollBars(false);
        if (Gdx.app.getType() != Application.ApplicationType.Android) codeScrollPane.setFlickScroll(false);

        super.addActor(codeScrollPane);
        codeTextArea.setColorParser(codeEditorStyle.colorParser);
        font = this.codeEditorStyle.font;
        font.setFixedWidthGlyphs("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~* ");

        //init the char size
        GlyphLayout calcLayout = new GlyphLayout();
        calcLayout.setText(font, "0");
        numberCharWidth = calcLayout.width;

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Stage stage = getStage();
        if (stage == null) return;

        int i = (int) Math.log10(codeTextArea.getLines());
        i++;
        if (i != maxLineNumberCharCount) {
            maxLineNumberCharCount = i;
            invalidate();
        }

        validate();

        if (isTransform()) {
            applyTransform(batch, computeTransform());
            drawChildren(batch, parentAlpha);
            drawLineNumbers(batch);
            resetTransform(batch);
        } else {
            super.draw(batch, parentAlpha);
            drawLineNumbers(batch);
        }
    }

    private void drawLineNumbers(Batch batch) {
        batch.flush();
        Rectangle scissors = new Rectangle();
        ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), numbersAreaClip, scissors);
        if (ScissorStack.pushScissors(scissors)) {
            //draw line numbers
            float lineHight = font.getLineHeight();
            float offset = -font.getLineHeight() * codeTextArea.firstLineShowing + codeScrollPane.getVisualScrollY() + numbersAreaClip.height - codeEditorStyle.background.getTopHeight();
            int drawUntil = Math.max(codeTextArea.linesShowing, 1);
            if (codeTextArea.getLines() < codeTextArea.firstLineShowing + codeTextArea.linesShowing)
                drawUntil = codeTextArea.getLines() - codeTextArea.firstLineShowing;


            for (int x = 0; x < drawUntil; x++) {
                if (x + codeTextArea.firstLineShowing == codeTextArea.getCursorLine())
                    font.setColor(highlightLineNumberColor);
                else font.setColor(lineNumberColor);

                String lineNumber = String.valueOf(x + codeTextArea.firstLineShowing + 1);
                int charCount = lineNumber.length();
                font.draw(batch, lineNumber, (maxLineNumberCharCount - charCount) * numberCharWidth, offset - x * lineHight);
            }
            batch.flush();
            ScissorStack.popScissors();
        }
    }

    @Override
    public void layout() {
        super.layout();
        float numbersAreaWidth = maxLineNumberCharCount * numberCharWidth + 20;
        //the main control
        codeScrollPane.setX(numbersAreaWidth);
        codeScrollPane.setWidth(getWidth() - numbersAreaWidth);
        codeScrollPane.setY(0);
        codeScrollPane.setHeight(getHeight());
        codeScrollPane.layout();

        //for the line numbers area
        numbersAreaClip = new Rectangle(0, 0, numbersAreaWidth, getHeight());
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), 100);
    }

    @Override
    public float getPrefHeight() {
        return Math.max(super.getPrefHeight(), 100);
    }

    @Override
    public boolean needsLayout() {
        return super.needsLayout() || codeScrollPane.needsLayout();
    }

    /**
     * adda a listener which handles TextArea updates
     */
    public void setTextFieldListener(TextFieldBase.TextFieldListener textFieldListener) {
        codeTextArea.setTextFieldListener(textFieldListener);
    }

    /**
     * get the text
     */
    public String getText() {
        return codeTextArea.getText();
    }

    /**
     * set the text
     */
    public void setText(String text) {
        codeTextArea.setText(text);
    }

    /**
     * moves the scrollPane to a specific position
     */
    public void moveTo(int line) {
        //this is to make scroll available on the ScrollBar
        codeScrollPane.layout();
        codeScrollPane.setScrollX(0);
        codeScrollPane.setScrollY(codeEditorStyle.font.getLineHeight() * (line - 1));
        codeScrollPane.updateVisualScroll();
    }

    public static class CodeEditorStyle extends MultiColorTextArea.MultiColorTextAreaStyle {
        public ColorParser colorParser;

        public Drawable corner;

        public Drawable hScroll;

        public Drawable hScrollKnob;

        public Drawable vScroll;

        public Drawable vScrollKnob;

        public CodeEditorStyle(BitmapFont font, Color fontColor, Drawable cursor, Drawable selection, Drawable background, Drawable autocompletion, ColorParser colorParser) {
            super(font, fontColor, cursor, selection, background, autocompletion);
            this.colorParser = colorParser;
        }

        public CodeEditorStyle(TextField.TextFieldStyle textFieldStyle, Drawable autocompletion, ScrollPane.ScrollPaneStyle scrollPaneStyle, ColorParser colorParser) {
            super(textFieldStyle, autocompletion);
            this.colorParser = colorParser;
            this.corner = scrollPaneStyle.corner;
            this.hScroll = scrollPaneStyle.hScroll;
            this.hScrollKnob = scrollPaneStyle.hScrollKnob;
            this.vScroll = scrollPaneStyle.vScroll;
            this.vScrollKnob = scrollPaneStyle.vScrollKnob;
        }

        private ScrollPane.ScrollPaneStyle createScrollPaneStyle() {
            ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle(background, hScroll, hScrollKnob, vScroll, vScrollKnob);
            style.corner = corner;
            return style;
        }

        private MultiColorTextArea.MultiColorTextAreaStyle createTextFieldStyle() {
            MultiColorTextArea.MultiColorTextAreaStyle style = new MultiColorTextArea.MultiColorTextAreaStyle(this, autocompletion);
            style.background = null;
            return style;
        }
    }
}
