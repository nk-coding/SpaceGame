package com.nkcoding.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;


public class CodeEditor extends WidgetGroup {

    /**<code>ScrollPane</code> which contains the <code>MultiColorTextArea</code> which contains the code*/
    private ScrollPane codeScrollPane;

    /**<code>MultiColorTextArea</code> which contains the code*/
    private MultiColorTextArea codeTextArea;

    /**for the clip of the line numbers area*/
    private Rectangle numbersAreaClip;

    /**font for the line numbers*/
    private BitmapFont font;

    /**style for the MultiColorTextArea*/
    private TextField.TextFieldStyle textFieldStyle;

    private final Color lineNumberColor = new Color(0x808080ff);
    private final Color highlightLineNumberColor = new Color(0xffffffff);

    private float numberCharWidth;

    private int maxLineNumberCharCount = 0;

    /**
     * the default constructor
     * @param textFieldStyle    the style for the <code>MultiColorTextArea</code> which shows the code in this control
     * @param scrollPaneStyle   the style for the <code>ScrollPane</code> used to scroll the code
     * @param codeParser        used to color-format the code
     */
    public CodeEditor(TextField.TextFieldStyle textFieldStyle, ScrollPane.ScrollPaneStyle scrollPaneStyle, ColorParser codeParser) {
        this.textFieldStyle = textFieldStyle;
        codeTextArea = new MultiColorTextArea("", textFieldStyle);
        codeScrollPane = new ScrollPane(codeTextArea, scrollPaneStyle);
        //st attributes on ScrollPane
        codeScrollPane.setFadeScrollBars(false);
        if (Gdx.app.getType() != Application.ApplicationType.Android) codeScrollPane.setFlickScroll(false);

        super.addActor(codeScrollPane);
        codeTextArea.setColorParser(codeParser);
        font = textFieldStyle.font;
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

        int i = (int)Math.log10(codeTextArea.getLines());
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
            float offset = -font.getLineHeight() * codeTextArea.firstLineShowing + codeScrollPane.getVisualScrollY() + numbersAreaClip.height - textFieldStyle.background.getTopHeight();
            int drawUntil = codeTextArea.linesShowing;
            if (codeTextArea.getLines() < codeTextArea.firstLineShowing + codeTextArea.linesShowing)
                drawUntil = codeTextArea.getLines() - codeTextArea.firstLineShowing;


            for (int x = 0; x < drawUntil; x++) {
                if (x + codeTextArea.firstLineShowing == codeTextArea.getCursorLine()) font.setColor(highlightLineNumberColor);
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
    public boolean needsLayout() {
        return super.needsLayout() || codeScrollPane.needsLayout();
    }
}
