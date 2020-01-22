package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.math.Vector2;

public interface CoreUnit {
    Vector2 getCenterPosition();

    float getRadius();

    boolean keyDown(int keycode);

    boolean keyUp(int keycode);
}
