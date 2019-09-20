package com.nkcoding.spacegame;

public enum Asset {
    //fonts
    Consolas_18("fonts/consolas_18.fnt"),
    Consolas_32("fonts/consolas_32.fnt"),

    //textures
    //game
    BasicHull("textures/game/basicHull.png"),
    //other
    Badlogic("textures/other/badlogic.jpg"),
    Numbers("textures/other/numbers.png"),
    //ui
    Cursor("textures/ui/cursor.png"),
    NoComponent("textures/ui/noComponent.png"),
    ScrollBarBackground("textures/ui/scrollBarBackground.png"),
    ScrollBarKnob("textures/ui/scrollBarKnob.png"),
    SimpleBorder("textures/ui/simpleBorder.png"),
    CodeSymbol("textures/ui/codeSymbol.png"),
    SaveSymbol("textures/ui/saveSymbol.png"),
    CloseSymbol("textures/ui/closeSymbol.png");

    private String value;

    Asset(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }

}