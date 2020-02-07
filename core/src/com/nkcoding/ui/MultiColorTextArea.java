/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * THIS IS A MODIFIED VERSION
 ******************************************************************************/

package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.nkcoding.interpreter.compiler.CompileException;
import com.nkcoding.interpreter.compiler.Lexer;
import com.nkcoding.interpreter.compiler.Token;

import java.util.*;
import java.util.stream.Collectors;

public class MultiColorTextArea extends TextFieldBase implements Cullable {

    /**
     * Index of the first line showed by the text area
     **/
    int firstLineShowing;
    /**
     * Number of lines showed by the text area
     **/
    int linesShowing;
    /**
     * Array storing lines breaks positions
     **/
    private IntArray linesBreak;
    /**
     * Last text processed. This attribute is used to avoid unnecessary computations while calculating offsets
     **/
    private String lastText = "";
    private boolean multiLineChange = true;
    private Lexer lexer = new Lexer();
    /**
     * Current line for the cursor
     **/
    private int cursorLine;
    private Rectangle cullingArea;
    /**
     * until which width it has to render
     */
    private float renderUntilX = Float.POSITIVE_INFINITY;

    /**
     * Variable to maintain the x offset of the cursor when moving up and down. If it's set to -1, the offset is reset
     **/
    private float moveOffset;

    private float prefWidth;

    /**
     * the parser for the multiColor stuff
     */
    private ColorParser colorParser = null;

    /**
     * the Strings for the autocompletion popup
     */
    private TreeSet<String> currentAutocompletionItems = new TreeSet<>();

    private List<String> autocompletionItems = new LinkedList<>();

    private float maxAutocompletionWidth = 0;

    private boolean autocompletionEnabled = false;
    private String autocompletionText = "";
    private int autocompletionPosition = 0;
    private int selectedAutocompletionIndex = 0;
    private int prefAutocompletionOffset = 0;

    //constructors
    public MultiColorTextArea(String text, MultiColorTextAreaStyle style) {
        super(text, style);
    }

    public void setColorParser(ColorParser colorParser) {
        this.colorParser = colorParser;
    }

    @Override
    protected void initialize() {
        super.initialize();
        writeEnters = true;
        linesBreak = new IntArray();
        cursorLine = 0;
        firstLineShowing = 0;
        moveOffset = -1;
        linesShowing = Integer.MAX_VALUE / 2;
        prefWidth = 0f;
    }

    private void updateLinesBreak() {
        linesBreak.clear();
        int lineStart = 0;
        char lastCharacter;
        for (int i = 0; i < text.length(); i++) {
            lastCharacter = text.charAt(i);
            if (lastCharacter == ENTER_DESKTOP || lastCharacter == ENTER_ANDROID) {
                linesBreak.add(lineStart);
                linesBreak.add(i);
                lineStart = i + 1;
            }
        }
    }

    /**
     * gets the current position of the cursor, counting starts a the beginning of the line
     */
    public int getCursorPosInLine() {
        if (2 * getCursorLine() < linesBreak.size) {
            return getCursorPosition() - linesBreak.get(2 * getCursorLine());
        } else {
            return 0;
        }

    }

    @Override
    public void setText(String str) {
        super.setText(str);
        multiLineChange = true;
        updateLinesBreak();
    }

    @Override
    void paste(String content, boolean fireChangeEvent, boolean moveCursor) {
        super.paste(content, fireChangeEvent, moveCursor);
        multiLineChange = true;
    }

    @Override
    void cut(boolean fireChangeEvent) {
        super.cut(fireChangeEvent);
        multiLineChange = true;
    }

    protected int letterUnderCursor(float x) {
        if (linesBreak.size > 0) {
            if (cursorLine * 2 >= linesBreak.size) {
                return text.length();
            } else {
                float[] glyphPositions = this.glyphPositions.items;
                int start = linesBreak.items[cursorLine * 2];
                x += glyphPositions[start];
                int end = linesBreak.items[cursorLine * 2 + 1];
                int i = start;
                for (; i < end; i++)
                    if (glyphPositions[i] > x) break;
                if (i > 0 && glyphPositions[i] - x <= x - glyphPositions[i - 1]) return i;
                return Math.max(0, i - 1);
            }
        } else {
            return 0;
        }
    }

    @Override
    public float getPrefHeight() {
        float prefHeight = style.font.getLineHeight() * getLines();
        if (style.background != null)
            prefHeight += (style.background.getBottomHeight() + style.background.getTopHeight());
        return prefHeight;
    }

    @Override
    public float getPrefWidth() {
        return prefWidth + 10;
    }

    /**
     * Returns total number of lines that the text occupies
     **/
    public int getLines() {
        return linesBreak.size / 2 + (newLineAtEnd() ? 1 : 0);
    }

    /**
     * Returns if there's a new line at then end of the text
     **/
    public boolean newLineAtEnd() {
        return text.length() != 0
                && (text.charAt(text.length() - 1) == ENTER_ANDROID || text.charAt(text.length() - 1) == ENTER_DESKTOP);
    }

    /**
     * Moves the cursor to the given number line
     **/
    public void moveCursorLine(int line) {

        if (line < 0) {
            cursorLine = 0;
            cursor = 0;
            moveOffset = -1;
        } else if (line >= getLines()) {
            int newLine = getLines() - 1;
            cursor = text.length();
            if (line > getLines() || newLine == cursorLine) {
                moveOffset = -1;
            }
            cursorLine = newLine;
        } else if (line != cursorLine) {
            if (moveOffset < 0) {
                moveOffset = linesBreak.size <= cursorLine * 2 ? 0
                        : glyphPositions.get(cursor) - glyphPositions.get(linesBreak.get(cursorLine * 2));
            }
            cursorLine = line;
            cursor = cursorLine * 2 >= linesBreak.size ? text.length() : linesBreak.get(cursorLine * 2);
            while (cursor < text.length() && cursor <= linesBreak.get(cursorLine * 2 + 1) - 1
                    && glyphPositions.get(cursor) - glyphPositions.get(linesBreak.get(cursorLine * 2)) < moveOffset) {
                cursor++;
            }
            updateCurrentLine();
        }
    }


    /**
     * Updates the current line, checking the cursor position in the text
     * Scroll the text area to show the line of the cursor
     **/
    void updateCurrentLine() {
        int index = calculateCurrentLineIndex(cursor);
        int line = index / 2;
        // Special case when cursor moves to the beginning of the line from the end of another and a word
        // wider than the box
        if (index % 2 == 0 || index + 1 >= linesBreak.size || cursor != linesBreak.items[index]
                || linesBreak.items[index + 1] != linesBreak.items[index]) {
            if (line < linesBreak.size / 2 || text.length() == 0 || text.charAt(text.length() - 1) == ENTER_ANDROID
                    || text.charAt(text.length() - 1) == ENTER_DESKTOP) {
                cursorLine = line;
            }
        }
        updateScrollPane();
    }


    /**
     * Calculates the text area line for the given cursor position
     **/
    private int calculateCurrentLineIndex(int cursor) {
        int index = 0;
        while (index < linesBreak.size && cursor > linesBreak.items[index]) {
            index++;
        }
        return index;
    }

    @Override
    protected float getTextY(BitmapFont font, Drawable background) {
        float textY = getHeight();
        if (background != null) {
            textY = (int) (textY - background.getTopHeight());
        }
        return textY;
    }

    @Override
    protected void drawSelection(Drawable selection, Batch batch, BitmapFont font, float x, float y) {
        int i = firstLineShowing * 2;
        int iMax = Math.min((firstLineShowing + linesShowing) * 2, linesBreak.size);
        float offsetY = firstLineShowing * font.getLineHeight();
        int minIndex = Math.min(cursor, selectionStart);
        int maxIndex = Math.max(cursor, selectionStart);
        while (i + 1 < iMax) {

            int lineStart = linesBreak.get(i);
            int lineEnd = linesBreak.get(i + 1);

            if (!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
                    || (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))) {

                int start = Math.max(linesBreak.get(i), minIndex);
                int end = Math.min(linesBreak.get(i + 1), maxIndex);

                float selectionX = glyphPositions.get(start) - glyphPositions.get(linesBreak.get(i));
                float selectionWidth = glyphPositions.get(end) - glyphPositions.get(start);

                selection.draw(batch, x + selectionX + fontOffset, y - textHeight - offsetY, selectionWidth,
                        font.getLineHeight());
                System.out.println("selection: " + (y - textHeight - offsetY));
            }

            offsetY += font.getLineHeight();
            i += 2;
        }
    }

    @Override
    protected void drawText(Batch batch, BitmapFont font, float x, float y) {
        boolean debug = false;
        if (debug) System.out.println();
        float offsetY = -firstLineShowing * font.getLineHeight() + font.getDescent();
        float offsetX = 0;
        //GlyphLayout for the calculation of the offsetX
        Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
        GlyphLayout layout = layoutPool.obtain();
        ColorRegion region = new ColorRegion(firstLineShowing, lexer.getLineStartIterator(firstLineShowing));

        while (getNextColorRegion(region, debug)) {
            if (offsetX <= renderUntilX) {
                font.setColor(region.color);
                font.draw(batch, displayText, x + offsetX, y + offsetY, region.startPos, region.endPos + 1, 0, Align.left, false);
                if (debug)
                    System.out.println("draw: [" + displayText.subSequence(region.startPos, region.endPos + 1) + "]");
            }
            if (region.newLineAfter) {
                offsetY -= font.getLineHeight();
                offsetX = 0;
            } else {
                layout.setText(font, displayText.subSequence(region.startPos, region.endPos + 1));
                offsetX += layout.width;
            }
        }
        if (debug) System.out.println();

        layoutPool.free(layout);
    }

    @Override
    protected void drawCursor(Drawable cursorPatch, Batch batch, BitmapFont font, float x, float y) {
        float textOffset = cursor >= glyphPositions.size || cursorLine * 2 >= linesBreak.size ? 0
                : glyphPositions.get(cursor) - glyphPositions.get(linesBreak.items[cursorLine * 2]);
        cursorPatch.draw(batch, x + textOffset + fontOffset + font.getData().cursorX,
                y - (cursorLine) * font.getLineHeight() - textHeight,
                cursorPatch.getMinWidth(),
                font.getLineHeight());
    }

    @Override
    protected void drawAutocompletion(Batch batch, BitmapFont font, float x, float y) {
        super.drawAutocompletion(batch, font, x, y);
        if (currentAutocompletionItems.size() > 0) {
            final float abs = 20;
            Drawable autocompletion = ((MultiColorTextAreaStyle) style).autocompletion;
            Drawable selectedAutocompletion = ((MultiColorTextAreaStyle)style).selectedAutocompletion;
            if (autocompletion != null) {
                float textOffset = (linesBreak.items[cursorLine * 2] + autocompletionPosition) >= glyphPositions.size || cursorLine * 2 >= linesBreak.size ? 0
                        : glyphPositions.get(linesBreak.items[cursorLine * 2] + autocompletionPosition) - glyphPositions.get(linesBreak.items[cursorLine * 2]);
                Rectangle screenArea = getArea();

                float cursorPosX = x + textOffset + fontOffset;
                float cursorPosY = y - font.getDescent() / 2 - (cursorLine + 1) * font.getLineHeight();

                float posX = cursorPosX - abs / 2;
                float posY;
                float maxWidth = screenArea.getWidth() - posX - abs;
                float height;
                float requestedWidth = this.maxAutocompletionWidth + abs;
                float requestedHeight = this.currentAutocompletionItems.size() * font.getLineHeight() + abs;
                float width = Math.min(requestedWidth, maxWidth);

                if (cursorPosY < screenArea.getHeight() / 2) {
                    //above cursor
                    posY = cursorPosY + font.getLineHeight();
                    height = Math.min(screenArea.getHeight() - abs - posY, requestedHeight);
                } else {
                    //below cursor
                    posY = abs;
                    height = cursorPosY - abs;
                    if (height > requestedHeight) {
                        posY += height - requestedHeight;
                        height = requestedHeight;
                    }
                }
                int maxLinesAmount = Math.min(currentAutocompletionItems.size(), (int)((height - abs) / font.getLineHeight()));
                //correct offset if necessary
                if (maxLinesAmount >= currentAutocompletionItems.size()) {
                    prefAutocompletionOffset = 0;
                }
                if (prefAutocompletionOffset > selectedAutocompletionIndex) {
                    prefAutocompletionOffset = selectedAutocompletionIndex;
                } else if (prefAutocompletionOffset + maxLinesAmount < selectedAutocompletionIndex) {
                    prefAutocompletionOffset = selectedAutocompletionIndex - maxLinesAmount;
                }

                autocompletion.draw(batch, posX, posY, width, height);

                int i = 0;

                for (String item : currentAutocompletionItems) {
                    if (i >= prefAutocompletionOffset && i < prefAutocompletionOffset + maxLinesAmount) {
                        int drawIndex = i - prefAutocompletionOffset;
                        if (i == selectedAutocompletionIndex) {
                            selectedAutocompletion.draw(batch,
                                    posX, posY + font.getLineHeight() * (drawIndex) - font.getDescent(),
                                    width, font.getLineHeight());
                        }
                        font.draw(batch, item, posX + abs / 2, posY + font.getLineHeight() * (drawIndex + 1),
                                0, item.length(), width - abs / 2, Align.left, false, "...");
                    }
                    i++;
                }
            }
        }
    }

    /**
     * set the new autocompletion, also calculates the maxWith
     *
     * @param autocompletionItems new autocompletion
     */
    public void updateAutocompletionItems(TreeSet<String> autocompletionItems) {
        this.currentAutocompletionItems = autocompletionItems;
        Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
        GlyphLayout layout = layoutPool.obtain();
        float maxWidth = 0;
        for (String s : autocompletionItems) {
            layout.setText(style.font, s);
            if (layout.width > maxWidth) {
                maxWidth = layout.width;
            }
        }
        this.maxAutocompletionWidth = maxWidth;
        layoutPool.free(layout);
    }

    public void updateAutocompletion(boolean enabled) {
        autocompletionEnabled = enabled;
    }

    public void updateAutocompletionText() {
        String autocompletionInput = autocompletionEnabled ? getAutocompletionText() : "";
        final String input = (autocompletionInput.trim().equals(autocompletionInput)) ? autocompletionInput : "";
        this.autocompletionText = input;
        if (input.equals("")) {
            updateAutocompletionItems(new TreeSet<>());
        } else {
            updateAutocompletionItems(autocompletionItems.stream()
                    .filter(item -> item.contains(input) && !item.equals(autocompletionInput))
                    .collect(Collectors.toCollection(TreeSet::new)));
        }
    }

    //gets the next colorRegion
    private boolean getNextColorRegion(ColorRegion cr, boolean debug) {
        int lineStart = -1;
        int colorStart = -1;
        if (cr.lineBreakIndex < linesBreak.size / 2) {
            lineStart = linesBreak.get(cr.lineBreakIndex * 2);
        }

        Token currentToken = null;
        while (currentToken == null && cr.currentToken != null) {
            Token possibleToken = cr.currentToken;
            if (colorParser.chooseColor(possibleToken.getType()) != null) {
                currentToken = possibleToken;
                colorStart = currentToken.getPos() + linesBreak.get(currentToken.getLine() * 2);
            } else {
                cr.next();
            }
        }
        if (debug) System.out.println(currentToken);
        //only increase an area when it is fully handled!
        //case 1: finished
        //-> can't get both
        if (lineStart == -1 || cr.lineBreakIndex >= (firstLineShowing + linesShowing)) {
            if (debug) System.out.print("case 1, ");
            return false;
        } else {
            //case 2: last color region handled, so draw the rest of the line and continue with the next line
            if (colorStart == -1) {
                if (debug) System.out.print("case 2, ");
                cr.color = style.fontColor;
                cr.startPos = cr.endPos + 1;
                cr.endPos = linesBreak.get(cr.lineBreakIndex * 2 + 1) - 1;
                cr.newLineAfter = true;

                cr.lineBreakIndex++;
            } else {
                //case 3: there is a color region to handle, but it is after the current position
                if (cr.endPos < colorStart - 1) {
                    //find out what is the end, the begin of the color region or the end of the current line
                    int lineEnd = linesBreak.get(cr.lineBreakIndex * 2 + 1);
                    //case 3.1: the line ends first
                    if (lineEnd < colorStart) {
                        if (debug) System.out.print("case 3.1, ");
                        cr.color = style.fontColor;
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = lineEnd - 1;
                        cr.newLineAfter = true;

                        cr.lineBreakIndex++;
                    }
                    //case 3.2: the color region begins first
                    else if (lineEnd > colorStart) {
                        if (debug) System.out.print("case 3.2, ");
                        cr.color = style.fontColor;
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = colorStart - 1;
                        cr.newLineAfter = false;
                    }
                    //case 3.3: the color region starts where the line ends (this makes no sense, but who cares
                    //merge in future with case 3.1
                    else {
                        if (debug) System.out.print("case 3.3, ");
                        cr.color = style.fontColor;
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = lineEnd - 1;
                        cr.newLineAfter = true;

                        cr.lineBreakIndex++;
                    }
                }
                //case 4: there is a color region to handle, and it starts with or before the current position
                else {
                    //find out what is the end, the end of the color region or the end of the line
                    int colorEnd = lineStart + currentToken.getPos() + currentToken.getLength() - 1;
                    int lineEnd = linesBreak.get(cr.lineBreakIndex * 2 + 1) - 1;
                    //case 4.1: the color region ends first
                    if (colorEnd < lineEnd) {
                        if (debug) System.out.print("case 4.1, ");
                        cr.color = colorParser.chooseColor(currentToken.getType());
                        cr.startPos = currentToken.getPos() + lineStart;
                        cr.endPos = colorEnd;
                        cr.newLineAfter = false;

                        cr.next();
                    }
                    //case 4.2: the line ends first
                    else if (colorEnd > lineEnd) {
                        if (debug) System.out.print("case 4.2, " + colorEnd + ", " + lineEnd);
                        cr.color = colorParser.chooseColor(currentToken.getType());
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = lineEnd;
                        cr.newLineAfter = true;

                        cr.lineBreakIndex++;
                    }
                    //case 4.3: the color region ends where the line ends
                    else {
                        if (debug) System.out.print("case 4.3, ");
                        cr.color = colorParser.chooseColor(currentToken.getType());
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = lineEnd;
                        cr.newLineAfter = true;

                        cr.lineBreakIndex++;
                        cr.next();
                    }
                }

            }
            //correct possible line breaks
            if (displayText.charAt(cr.startPos) == '\n' || displayText.charAt((cr.startPos)) == '\r') {
                if (cr.startPos < cr.endPos) {
                    cr.startPos++;
                }
            }
            return true;
        }
    }

    @Override
    public void setCullingArea(Rectangle cullingArea) {
        this.cullingArea = cullingArea;
        firstLineShowing = (int) ((getHeight() - cullingArea.y - cullingArea.height) / style.font.getLineHeight());
        if (firstLineShowing < 0) firstLineShowing = 0;
        linesShowing = (int) (cullingArea.height / style.font.getLineHeight());
        //correct possible error produced by rounding
        linesShowing += 2;
        renderUntilX = cullingArea.x + cullingArea.width;
    }

    private Rectangle getArea() {
        if (cullingArea != null) {
            return cullingArea;
        } else {
            return new Rectangle(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void calculateOffsets() {
        super.calculateOffsets();
        if (this.text != lastText) {
            float newPrefWidth = 0f;
            int oldLinesCount = getLines();

            this.lastText = text;
            BitmapFont font = style.font;
            linesBreak.clear();
            int lineStart = 0;

            char lastCharacter;
            Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
            GlyphLayout layout = layoutPool.obtain();
            for (int i = 0; i < text.length(); i++) {
                lastCharacter = text.charAt(i);
                if (lastCharacter == ENTER_DESKTOP || lastCharacter == ENTER_ANDROID) {
                    //check if I have to update prefWidth
                    layout.setText(font, text.subSequence(lineStart, i));
                    float tempPrefWidth = layout.width;
                    if (tempPrefWidth > newPrefWidth) newPrefWidth = tempPrefWidth;

                    linesBreak.add(lineStart);
                    linesBreak.add(i);
                    lineStart = i + 1;
                }
            }

            //check the length of the last line manually
            layout.setText(font, text.subSequence(lineStart, text.length()));
            float tempPrefWidth = layout.width;
            if (tempPrefWidth > newPrefWidth) newPrefWidth = tempPrefWidth;

            layoutPool.free(layout);
            // Add last line
            if (lineStart < text.length()) {
                linesBreak.add(lineStart);
                linesBreak.add(text.length());
            }

            //update prefWidth if necessary
            if ((prefWidth != newPrefWidth) || (oldLinesCount != getLines())) {
                prefWidth = newPrefWidth;
                invalidateHierarchy();
            }

            updateCurrentLine();

            try {
                if (multiLineChange) {
                    lexer.update(getText(), true);
                } else {
                    updateCurrentLine();
                    lexer.updateLine(getTextLine(getCursorLine()), getCursorLine(), true);
                }
                this.autocompletionItems = lexer.getTokens().stream().filter(token -> (token.getType() & (Token.IDENTIFIER | Token.KEYWORD)) != 0)
                        .map(Token::getContent).distinct().collect(Collectors.toList());
                updateAutocompletionText();
            } catch (CompileException e) {
                e.printStackTrace();
            }

            multiLineChange = false;
        }
    }

    @Override
    protected InputListener createInputListener() {
        return new TextAreaListener();
    }

    @Override
    public void setSelection(int selectionStart, int selectionEnd) {
        super.setSelection(selectionStart, selectionEnd);
        updateAutocompletion(false);
        updateCurrentLine();
    }

    @Override
    protected void moveCursor(boolean forward, boolean jump) {
        updateAutocompletion(false);
        int count = forward ? 1 : -1;
        int index = (cursorLine * 2) + count;
        if (index >= 0 && index + 1 < linesBreak.size && linesBreak.items[index] == cursor
                && linesBreak.items[index + 1] == cursor) {
            cursorLine += count;
            if (jump) {
                super.moveCursor(forward, jump);
            }
            updateCurrentLine();
        } else {
            super.moveCursor(forward, jump);
        }

        updateCurrentLine();
    }

    @Override
    protected boolean continueCursor(int index, int offset) {
        int pos = calculateCurrentLineIndex(index + offset);
        return super.continueCursor(index, offset) && (pos < 0 || pos >= linesBreak.size - 2 || (linesBreak.items[pos + 1] != index)
                || (linesBreak.items[pos + 1] == linesBreak.items[pos + 2]));
    }

    public int getCursorLine() {
        return cursorLine;
    }

    public float getCursorX() {
        if (cursor >= glyphPositions.size) return 0;
        else if (cursorLine * 2 >= linesBreak.size) return 0;
        else if (cursor < 0) return 0;
        else {
            return glyphPositions.get(cursor) - glyphPositions.get(linesBreak.items[cursorLine * 2]);
        }
    }

    public float getCursorY() {
        BitmapFont font = style.font;
        return -(-font.getDescent() / 2 - (cursorLine + 1) * font.getLineHeight());
    }

    /**
     * can be overwritten if necessary
     * is called before the text is changed
     * warning: is also called if the character is no legal char
     *
     * @param event     the corresponding input event
     * @param character the typed char
     */
    public Optional<Boolean> preInput(InputEvent event, char character) {
        return Optional.empty();
    }

    /**
     * can be overwritten if necessary
     * is called after the text is changed
     *
     * @param event     the corresponding input event
     * @param character the typed char
     */
    public boolean postInput(InputEvent event, char character) {
        return false;
    }

    private void updateScrollPane() {
        if (getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) getParent();
            float posX = getCursorX();
            //just a hack, but it works fine
            //it is necessary, because of some really weired behaviour
            if (posX > 0) {
                if (posX > scrollPane.getScrollWidth() / 2) posX += 10;
                else if (posX < 100) posX = 0;

                scrollPane.scrollTo(posX, getHeight() - getCursorY(), 0, style.font.getLineHeight());
            }
        }
    }

    //helper methods for manipulating the text based on the input
    protected String getTextLine(int line) {
        //special case newLineAtEnd
        if (linesBreak.size == 2 * line) return "";
        return text.substring(Math.min(linesBreak.get(2 * line), text.length()), Math.min(linesBreak.get(2 * line + 1) + 1, text.length()));
    }

    //calculates the amount of space chars at the beginning of a String
    protected int calculateSpaceChars(String str) {
        int x = 0;
        while (str.length() > x && str.charAt(x) == ' ') x++;
        return x;
    }

    private String getAutocompletionText() {
        int cursorPos = getCursorPosInLine() - 1;
        Token cursorToken = lexer.getTokenBefore(getCursorLine(), cursorPos);
        if (cursorToken == null) {
            return "";
        } else if (cursorToken.getLine() != getCursorLine()) {
            return "";
        } else if (cursorToken.getType() == Token.COMMENT || cursorToken.getType() == Token.STRING_LITERAL) {
            return "";
        } else if (cursorToken.getPos() + cursorToken.getLength() <= cursorPos) {
            return "";
        } else {
            autocompletionPosition = cursorToken.getPos();
            return cursorToken.getContent().substring(0, cursorPos - cursorToken.getPos() + 1);
        }
    }

    public static class MultiColorTextAreaStyle extends TextField.TextFieldStyle {
        public Drawable autocompletion;
        public Drawable selectedAutocompletion;

        /**
         * default constructor
         */
        public MultiColorTextAreaStyle() {
            super();
        }

        public MultiColorTextAreaStyle(BitmapFont font, Color fontColor, Drawable cursor,
                                       Drawable selection, Drawable background, Drawable autocompletion, Drawable selectedAutocompletion) {
            super(font, fontColor, cursor, selection, background);
            this.autocompletion = autocompletion;
            this.selectedAutocompletion = selectedAutocompletion;
        }

        public MultiColorTextAreaStyle(TextField.TextFieldStyle style, Drawable autocompletion, Drawable selectedAutocompletion) {
            super(style);
            this.autocompletion = autocompletion;
            this.selectedAutocompletion = selectedAutocompletion;
        }
    }

    private class ColorRegion {
        int startPos = -1;
        int endPos = -1;
        Color color = null;
        boolean newLineAfter = false;

        //what should it draw now
        int lineBreakIndex;
        Iterator<Token> tokensIterator;
        Token currentToken;
        private void next() {
            currentToken = tokensIterator.hasNext() ? tokensIterator.next() : null;
        }

        ColorRegion(int lineBreakIndex, Iterator<Token> tokensIterator) {
            this.lineBreakIndex = lineBreakIndex;
            this.tokensIterator = tokensIterator;
            if (lineBreakIndex < linesBreak.size / 2) {
                startPos = linesBreak.get(lineBreakIndex * 2);
                endPos = startPos - 1;
            }
            currentToken = tokensIterator.next();
        }
    }

    /**
     * Input listener for the text area
     **/
    public class TextAreaListener extends TextFieldClickListener {

        @Override
        protected void setCursorPosition(float x, float y) {
            moveOffset = -1;
            updateAutocompletion(false);

            Drawable background = style.background;
            BitmapFont font = style.font;

            float height = getHeight();

            if (background != null) {
                height -= background.getTopHeight();
                x -= background.getLeftWidth();
            }
            x = Math.max(0, x);
            if (background != null) {
                y -= background.getTopHeight();
            }

            cursorLine = (int) Math.floor((height - y) / font.getLineHeight()) /*+ firstLineShowing*/;
            cursorLine = Math.max(0, Math.min(cursorLine, getLines() - 1));

            super.setCursorPosition(x, y);
            updateCurrentLine();
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            boolean result = super.keyDown(event, keycode);
            if (keycode == Input.Keys.FORWARD_DEL || keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                updateAutocompletion(false);
            } else if (keycode == Input.Keys.DEL) {
                updateAutocompletion(true);
            }
            if (hasKeyboardFocus()) {
                boolean repeat = false;
                boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                if (keycode == Input.Keys.DOWN) {
                    if (autocompletionEnabled) {
                        selectedAutocompletionIndex++;
                        selectedAutocompletionIndex += currentAutocompletionItems.size();
                        selectedAutocompletionIndex %= currentAutocompletionItems.size();
                    } else {
                        if (shift) {
                            if (!hasSelection) {
                                selectionStart = cursor;
                                hasSelection = true;
                            }
                        } else {
                            clearSelection();
                        }
                        moveCursorLine(cursorLine + 1);
                        repeat = true;
                    }
                } else if (keycode == Input.Keys.UP) {
                    if (autocompletionEnabled) {
                        selectedAutocompletionIndex--;
                        selectedAutocompletionIndex += currentAutocompletionItems.size();
                        selectedAutocompletionIndex %= currentAutocompletionItems.size();
                    } else {
                        if (shift) {
                            if (!hasSelection) {
                                selectionStart = cursor;
                                hasSelection = true;
                            }
                        } else {
                            clearSelection();
                        }
                        moveCursorLine(cursorLine - 1);
                        repeat = true;
                    }
                } else {
                    moveOffset = -1;
                }
                if (repeat) {
                    scheduleKeyRepeatTask(keycode);
                }
                updateCurrentLine();
                return true;
            }
            return result;
        }

        @Override
        public boolean keyTyped(InputEvent event, char character) {
            event.getKeyCode();
            multiLineChange = true;
            boolean hadSelection = hasSelection;
            //preInput
            Optional<Boolean> res = preInput(event, character);
            if (res.isPresent()) return res.get();

            //every other character
            boolean result = super.keyTyped(event, character);
            //postInput
            if (result) {
                if (!postInput(event, character) && !hadSelection) {
                    if (character != 8 && character != 127) {
                        multiLineChange = false;
                        updateAutocompletion(true);
                    }
                }
            }

            updateCurrentLine(); //this always produced serious errors, I don't know why I can do this now
            return result;
        }

        @Override
        protected void goHome(boolean jump) {
            updateAutocompletion(false);
            if (jump) {
                cursor = 0;
            } else if (cursorLine * 2 < linesBreak.size) {
                String currentLine = getTextLine(cursorLine);
                int amountLeadingWhitespace = currentLine.length() - currentLine.replaceAll("^\\s+", "").length();
                cursor = linesBreak.get(cursorLine * 2) + amountLeadingWhitespace;
            }
        }

        @Override
        protected void goEnd(boolean jump) {
            updateAutocompletion(false);
            if (jump || cursorLine >= getLines()) {
                cursor = text.length();
            } else if (cursorLine * 2 + 1 < linesBreak.size) {
                cursor = linesBreak.get(cursorLine * 2 + 1);
            }
        }
    }
}