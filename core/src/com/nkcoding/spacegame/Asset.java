package com.nkcoding.spacegame;

public enum Asset {
    //fonts
    SourceCodePro_18("fonts/sourceCodePro_18.fnt"),
    SourceCodePro_32("fonts/sourceCodePro_32.fnt"),

    //textures
    //game
    BasicHull("textures/game/basicHull.png"),
    Engine("textures/game/engine.png"),
    PowerCore("textures/game/powerCore.png"),
    Bullet("textures/game/bullet.png"),
    VerySimpleExplosion("textures/game/verySimpleExplosion.png"),
    StarBackground("textures/game/starBackground.png"),
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
