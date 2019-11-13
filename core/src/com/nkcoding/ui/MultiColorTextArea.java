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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import java.util.ArrayList;

public class MultiColorTextArea extends TextFieldBase implements ColorParserHandler, Cullable {

    /**
     * Array storing lines breaks positions
     **/
    IntArray linesBreak;

    /**
     * Last text processed. This attribute is used to avoid unnecessary computations while calculating offsets
     **/
    private String lastText;

    /**
     * Current line for the cursor
     **/
    int cursorLine;

    /**
     * Index of the first line showed by the text area
     **/
    int firstLineShowing;

    /**
     * Number of lines showed by the text area
     **/
    int linesShowing;

    /**
     * until which width it has to render
     */
    private float renderUntilX = Float.POSITIVE_INFINITY;

    /**
     * the first ColorRegion which is important
     **/
    private int firstRelevantColorArea;

    /**
     * Variable to maintain the x offset of the cursor when moving up and down. If it's set to -1, the offset is reset
     **/
    float moveOffset;

    private float prefRows;

    private float prefWidth;

    //lists to handle the color stuff
    private IntArray colorAreas;
    private ArrayList<Color> colors;

    //the parser for the multiColor stuff
    private ColorParser colorParser = null;

    public void setColorParser(ColorParser colorParser) {
        this.colorParser = colorParser;
    }

    //constructors

    public MultiColorTextArea(String text, Skin skin) {
        super(text, skin);
    }

    public MultiColorTextArea(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public MultiColorTextArea(String text, TextField.TextFieldStyle style) {
        super(text, style);
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
        firstRelevantColorArea = -1;
        prefWidth = 0f;
        colorAreas = new IntArray();
        colors = new ArrayList<>();
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

    /**
     * Sets the preferred number of rows (lines) for this text area. Used to calculate preferred height
     */
    public void setPrefRows(float prefRows) {
        this.prefRows = prefRows;
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
            showCursor();
        }
    }


    /**
     * Updates the current line, checking the cursor position in the text
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
     * Scroll the text area to show the line of the cursor
     **/
    void showCursor() {
        updateCurrentLine();
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

    // OVERRIDE from TextField

    @Override
    protected void sizeChanged() {
        lastText = null; // Cause calculateOffsets to recalculate the line breaks.

        // The number of lines showed must be updated whenever the height is updated
        BitmapFont font = style.font;
        Drawable background = style.background;
        //float availableHeight = getHeight() - (background == null ? 0 : background.getBottomHeight() + background.getTopHeight());
        //linesShowing = (int)Math.floor(availableHeight / font.getLineHeight());
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

                selection.draw(batch, x + selectionX + fontOffset, y - textHeight - font.getDescent() - offsetY, selectionWidth,
                        font.getLineHeight());
            }

            offsetY += font.getLineHeight();
            i += 2;
        }
    }

    @Override
    protected void drawText(Batch batch, BitmapFont font, float x, float y) {
        float offsetY = -firstLineShowing * font.getLineHeight();
        float offsetX = 0;
        //GlyphLayout for the calculation of the offsetX
        Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
        GlyphLayout layout = layoutPool.obtain();
        ColorRegion region = new ColorRegion(firstLineShowing, firstRelevantColorArea);
        boolean debug = false;
        while (getNextColorRegion(region, debug)) {
            if (offsetX <= renderUntilX) {
                font.setColor(region.color);
                font.draw(batch, displayText, x + offsetX, y + offsetY, region.startPos, region.endPos + 1, 0, Align.left, false);
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

    //gets the next colorRegion
    private boolean getNextColorRegion(ColorRegion cr, boolean debug) {
        int lineStart = -1;
        int colorStart = -1;
        if (cr.lineBreakIndex < linesBreak.size / 2) {
            lineStart = linesBreak.get(cr.lineBreakIndex * 2);
        }
        if (cr.colorsIndex < colors.size() && cr.colorsIndex > -1) {
            colorStart = colorAreas.get(cr.colorsIndex * 2);
        }
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
                    int colorEnd = colorAreas.get(cr.colorsIndex * 2 + 1);
                    int lineEnd = linesBreak.get(cr.lineBreakIndex * 2 + 1) - 1;
                    //case 4.1: the color region ends first
                    if (colorEnd < lineEnd) {
                        if (debug) System.out.print("case 4.1, ");
                        cr.color = colors.get(cr.colorsIndex);
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = colorEnd;
                        cr.newLineAfter = false;

                        cr.colorsIndex++;
                    }
                    //case 4.2: the line ends first
                    else if (colorEnd > lineEnd) {
                        if (debug) System.out.print("case 4.2, ");
                        cr.color = colors.get(cr.colorsIndex);
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = lineEnd;
                        cr.newLineAfter = true;

                        cr.lineBreakIndex++;
                    }
                    //case 4.3: the color region ends where the line ends
                    else {
                        if (debug) System.out.print("case 4.3, ");
                        cr.color = colors.get(cr.colorsIndex);
                        cr.startPos = cr.endPos + 1;
                        cr.endPos = lineEnd;
                        cr.newLineAfter = true;

                        cr.lineBreakIndex++;
                        cr.colorsIndex++;
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
        int oldFirstLineShowing = firstLineShowing;
        firstLineShowing = (int) ((getHeight() - cullingArea.y - cullingArea.height) / style.font.getLineHeight());
        if (firstLineShowing < 0) firstLineShowing = 0;
        linesShowing = (int) (cullingArea.height / style.font.getLineHeight());
        //correct possible error produced by rounding
        linesShowing += 2;
        renderUntilX = cullingArea.x + cullingArea.width;
        if (oldFirstLineShowing != firstLineShowing) updateRelevantColorRegions();
    }

    private class ColorRegion {
        int startPos = -1;
        int endPos = -1;
        Color color = null;
        boolean newLineAfter = false;

        //what should it draw now
        int lineBreakIndex = 0;
        int colorsIndex = 0;

        //constructor which sets a bit more
        ColorRegion(int lineBreakIndex, int colorsIndex) {
            this.lineBreakIndex = lineBreakIndex;
            this.colorsIndex = colorsIndex;
            if (lineBreakIndex < linesBreak.size / 2) {
                startPos = linesBreak.get(lineBreakIndex * 2);
                endPos = startPos - 1;
            }
        }
    }

    @Override
    protected void drawCursor(Drawable cursorPatch, Batch batch, BitmapFont font, float x, float y) {
        float textOffset = cursor >= glyphPositions.size || cursorLine * 2 >= linesBreak.size ? 0
                : glyphPositions.get(cursor) - glyphPositions.get(linesBreak.items[cursorLine * 2]);
        cursorPatch.draw(batch, x + textOffset + fontOffset + font.getData().cursorX,
                y - font.getDescent() / 2 - (cursorLine /*- firstLineShowing*/ + 1) * font.getLineHeight(), cursorPatch.getMinWidth(),
                font.getLineHeight());
    }

    @Override
    protected void calculateOffsets() {
        super.calculateOffsets();
        if (!this.text.equals(lastText)) {
            float newPrefWidth = 0f;
            int oldLinesCount = getLines();

            this.lastText = text;
            BitmapFont font = style.font;
            float maxWidthLine = this.getWidth()
                    - (style.background != null ? style.background.getLeftWidth() + style.background.getRightWidth() : 0);
            linesBreak.clear();
            int lineStart = 0;
            int lastSpace = 0;
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

            //update position if necessary and possible
            //System.out.println("update form calculate offsets");
            //updateScrollPane();

            //if there is a color parser, now is the wright time
            if (colorParser != null) {
                clearColors();
                colorParser.parse(getText(), this);
                updateRelevantColorRegions();
            }

            showCursor();
        }
    }

    @Override
    protected InputListener createInputListener() {
        return new TextAreaListener();
    }

    @Override
    public void setSelection(int selectionStart, int selectionEnd) {
        super.setSelection(selectionStart, selectionEnd);
        updateCurrentLine();
    }

    @Override
    protected void moveCursor(boolean forward, boolean jump) {
        int count = forward ? 1 : -1;
        int index = (cursorLine * 2) + count;
        if (index >= 0 && index + 1 < linesBreak.size && linesBreak.items[index] == cursor
                && linesBreak.items[index + 1] == cursor) {
            cursorLine += count;
            if (jump) {
                super.moveCursor(forward, jump);
            }
            showCursor();
        } else {
            super.moveCursor(forward, jump);
        }

        updateCurrentLine();
        //updateScrollPane();

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

    @Override
    public void addColorRegion(int start, int end, Color color) {
        colorAreas.add(start, end);
        colors.add(color);
    }

    private void clearColors() {
        colorAreas.clear();
        colors.clear();
    }

    private void updateRelevantColorRegions() {
        //reset
        firstRelevantColorArea = -1;
        //check if there are colorAreas
        if (colors.size() >= 1 && linesBreak.size > firstLineShowing * 2) {
            int lineStartPos = linesBreak.get(firstLineShowing * 2);
            boolean finised = false;
            int i = 0;
            while (!finised) {
                int startPos = colorAreas.get(2 * i);
                int endPos = colorAreas.get(2 * i + 1);
                if (startPos >= lineStartPos || endPos >= lineStartPos) {
                    finised = true;
                    firstRelevantColorArea = i;
                }
                i++;
                finised |= i >= colors.size();
            }
        }
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
    private String getTextLine(int line) {
        //special case newLineAtEnd
        if (linesBreak.size == 2 * line) return "";
        if (linesBreak.size < 2 * line) throw new IllegalArgumentException("line is too high");
        else if (line < 0) throw new IllegalArgumentException("line is too low");
        return text.substring(linesBreak.get(2 * line), linesBreak.get(2 * line + 1) + 1);
    }

    //important: offsets are not calculated for the last input!!!
    private boolean isAtLineEnd() {
        if (linesBreak.size <= getCursorLine() * 2) return true;
        else {
            System.out.println(getCursorPosition() + ", " + linesBreak.get(getCursorLine() * 2 + 1));
            return getCursorPosition() == linesBreak.get(getCursorLine() * 2 + 1);
        }
    }

    //calculates the amount of space chars at the beginning of a String
    private int calculateSpaceChars(String str) {
        int x = 0;
        while (str.length() > x && str.charAt(x) == ' ') x++;
        return x;
    }

    /**
     * Input listener for the text area
     **/
    public class TextAreaListener extends TextFieldClickListener {

        @Override
        protected void setCursorPosition(float x, float y) {
            moveOffset = -1;

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
            if (hasKeyboardFocus()) {
                boolean repeat = false;
                boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                if (keycode == Input.Keys.DOWN) {
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

                } else if (keycode == Input.Keys.UP) {
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

                } else {
                    moveOffset = -1;
                }
                if (repeat) {
                    scheduleKeyRepeatTask(keycode);
                }
                showCursor();
                return true;
            }
            return result;
        }

        @Override
        public boolean keyTyped(InputEvent event, char character) {
            //correct tab
            if (character == '\t') {
                System.out.println("TAB");
                paste("   ", true, true);
                return true;
            } else if (character == ')') {
                boolean endBracketExists = text.length() > getCursorPosition() && text.charAt(getCursorPosition()) == ')';
                if (endBracketExists) {
                    moveCursor(true, false);
                    return true;
                }
            } else if (character == ']') {
                boolean endBracketExists = text.length() > getCursorPosition() && text.charAt(getCursorPosition()) == ']';
                if (endBracketExists) {
                    moveCursor(true, false);
                    return true;
                }
            }
            //every other character
            int lineNumber = getCursorLine();
            boolean result = super.keyTyped(event, character);

            if (result) {
                switch (character) {
                    case '{':
                        paste("}", false, false);
                        break;
                    case ENTER_ANDROID:
                    case ENTER_DESKTOP:
                        //region
                        String lastLine = getTextLine(lineNumber);
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
                        break;
                    case '(':
                        paste(")", false, false);
                        break;
                    case '[':
                        paste("]", false, false);
                        break;
                }
            }

            showCursor(); //this always produced serious errors, I don't know why I can do this now


            return result;
        }

        @Override
        protected void goHome(boolean jump) {
            if (jump) {
                cursor = 0;
            } else if (cursorLine * 2 < linesBreak.size) {
                cursor = linesBreak.get(cursorLine * 2);
            }
        }

        @Override
        protected void goEnd(boolean jump) {
            if (jump || cursorLine >= getLines()) {
                cursor = text.length();
            } else if (cursorLine * 2 + 1 < linesBreak.size) {
                cursor = linesBreak.get(cursorLine * 2 + 1);
            }
        }
    }
}