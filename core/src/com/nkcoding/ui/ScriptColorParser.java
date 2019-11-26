package com.nkcoding.ui;

import com.badlogic.gdx.graphics.Color;
import com.nkcoding.interpreter.compiler.Token;

public class ScriptColorParser implements ColorParser {

    //colors
    private final Color stringColor = new Color(0x4b7758ff);
    private final Color keyWordColor = new Color(0xcc7832ff);
    private final Color numberColor = new Color(0x4a85baff);
    private final Color commentColor = new Color(0x808080ff);

    @Override
    public Color chooseColor(int id) {
        switch (id) {
            case Token.STRING_LITERAL:
                return stringColor;
            case Token.KEYWORD:
                return keyWordColor;
            case Token.INT_LITERAL:
                return numberColor;
            case Token.COMMENT:
                return commentColor;
            default:
                return null;
        }
    }

}
