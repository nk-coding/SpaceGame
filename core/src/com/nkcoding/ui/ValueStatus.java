package com.nkcoding.ui;

import com.badlogic.gdx.graphics.Color;

public enum ValueStatus {
    OK(new Color(0xffffffff)),
    ERROR(new Color(0xff0000ff)),
    WARNING(new Color(0xffff00ff));

    public final Color color;

    ValueStatus(Color color) {
        this.color = color;
    }

    public static ValueStatus of(boolean isOK) {
        return isOK ? OK : ERROR;
    }
}
