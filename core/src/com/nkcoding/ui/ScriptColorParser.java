package com.nkcoding.ui;

import com.badlogic.gdx.graphics.Color;
import com.nkcoding.interpreter.compiler.CompilerHelper;

public class ScriptColorParser implements ColorParser {

    //colors
    private final Color stringColor = new Color(0x4b7758ff);
    private final Color keyWordColor = new Color(0xcc7832ff);
    private final Color numberColor = new Color(0x4a85baff);
    private final Color commentColor = new Color(0x808080ff);

    @Override
    public void parse(String str, ColorParserHandler handler) {
        //a simple parser for a script based on java, it will only highlight keywords
        boolean inString = false;
        boolean escaped = false;
        boolean inNumber = false;
        boolean inPossibleNumber = false;
        boolean inWord = false;
        boolean inComment = false;
        int startPos = 0;
        //iterate over the string
        for (int x = 0; x < str.length(); x++) {
            char c = str.charAt(x);
            //check for comment
            if (inComment) {
                if (c == '\n') {
                    //end of comment
                    inComment = false;
                    handler.addColorRegion(startPos, x - 1, commentColor);
                }
            } else {
                //not in String, comment start, comment found
                if (!inString && c == '/' && (x + 1) < str.length() && str.charAt(x + 1) == '/') {
                    // a wild comment was found
                    inComment = true;
                    startPos = x;
                }
                //check for all the other stuff
                else if (inString) {
                    //actual in String, this is the worst
                    if (c == '\\') {
                        //escape char
                        if (!escaped) {
                            //not escaped
                            escaped = true;
                        } else {
                            escaped = false;
                        }
                    } else if (c == '"') {
                        if (!escaped) {
                            //end of String
                            handler.addColorRegion(startPos, x, stringColor);
                            inString = false;
                        } else {
                            escaped = false;
                        }
                    } else {
                        //reset escaped
                        escaped = false;
                    }
                } else if (inNumber) {
                    if (!(Character.isDigit(c) || c == '.')) {
                        //end of number
                        inNumber = false;
                        handler.addColorRegion(startPos, x - 1, numberColor);
                    }
                } else if (inPossibleNumber) {
                    //something that started with a dot
                    //is only a number if there is a digit now
                    inPossibleNumber = false;
                    if (Character.isDigit(c)) {
                        inNumber = true;
                    }
                } else if (inWord) {
                    if (!(Character.isLetterOrDigit(c) || c == '_')) {
                        //end of word
                        inWord = false;
                        String subStr = str.substring(startPos, x);
                        if (CompilerHelper.isReservedKeyword(subStr)) {
                            handler.addColorRegion(startPos, x - 1, keyWordColor);
                        }
                    }
                } else {
                    //the default behaviour, check for different stuff
                    if (!Character.isWhitespace(c)) {
                        //set start pos
                        startPos = x;
                        if (c == '"') {
                            //start of string
                            inString = true;
                        } else if (c == '.') {
                            inPossibleNumber = true;
                        } else if (Character.isDigit(c)) {
                            inNumber = true;
                        } else if (Character.isLetter(c) || c == '_') {
                            inWord = true;
                        }
                    }
                }
            }


        }
        //check for things that might have been ignored because they are at the end
        if (inNumber) handler.addColorRegion(startPos, str.length() - 1, numberColor);
        else if (inWord) {
            String subStr = str.substring(startPos);
            if (CompilerHelper.isReservedKeyword(subStr)) {
                handler.addColorRegion(startPos, str.length() - 1, keyWordColor);
            }
        } else if (inComment) {
            handler.addColorRegion(startPos, str.length() - 1, commentColor);
        }
    }
}
