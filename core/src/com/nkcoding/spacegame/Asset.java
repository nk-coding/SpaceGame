package com.nkcoding.spacegame;

public enum Asset {
    //fonts
    Inconsolata_18("fonts/inconsolata_18.fnt"),
    Inconsolata_32("fonts/inconsolata_32.fnt"),

    //textures
    //game
    BasicHull("textures/game/basicHull.png"),
    Engine("textures/game/engine.png"),
    PowerCore("textures/game/powerCore.png"),
    Bullet("textures/game/bullet.png"),
    VerySimpleExplosion("textures/game/verySimpleExplosion.png"),
    //others
    Numbers("textures/other/numbers.png"),
    //ui
    Cursor("textures/ui/cursor.png"),
    NoComponent("textures/ui/noComponent.png"),
    ScrollBarBackground("textures/ui/scrollBarBackground.png"),
    ScrollBarKnob("textures/ui/scrollBarKnob.png"),
    SimpleBorder("textures/ui/simpleBorder.png"),
    CodeSymbol("textures/ui/codeSymbol.png"),
    CodeErrorSymbol("textures/ui/codeErrorSymbol.png"),
    SaveSymbol("textures/ui/saveSymbol.png"),
    CloseSymbol("textures/ui/closeSymbol.png"),
    OkSymbol("textures/ui/okSymbol.png"),
    RotateSymbol("textures/ui/rotateSymbol.png"),
    ErrorSymbol("textures/ui/errorSymbol.png"),
    ActionNecessarySymbol("textures/ui/actionNecessarySymbol.png"),
    Selection("textures/ui/selection.png"),
    BasicBackground("textures/ui/basicBackground.png");

    private String value;

    Asset(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }

}
