package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.math.Vector2;

public interface CoreUnit {
    Vector2 getWorldCenterPosition();

    float getRequestedHeight();

    boolean keyDown(int keycode);

    boolean keyUp(int keycode);

    void setCameraFocus(boolean cameraFocus);

    boolean isOriginal();
}
