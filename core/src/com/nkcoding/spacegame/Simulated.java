package com.nkcoding.spacegame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Simulated {
    /**updates physics etc.*/
    void act(float time);

    /**draw the simulated object*/
    void draw(SpriteBatch batch);
}
