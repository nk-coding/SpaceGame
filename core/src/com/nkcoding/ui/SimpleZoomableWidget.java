package com.nkcoding.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SimpleZoomableWidget extends Widget implements Zoomable {
    private Drawable drawable;
    private int amountX, amountY;
    private float fieldSize;

    private float activeZoom = 1f;

    public SimpleZoomableWidget(Drawable drawable, int amountX, int amountY, float fieldSize) {
        this.drawable = drawable;
        this.amountX = amountX;
        this.amountY = amountY;
        this.fieldSize = fieldSize;
    }

    @Override
    public void setCullingArea(Rectangle cullingArea, float zoom) {
        //ignore the culling stuff, because this is only a test class
        if (activeZoom != zoom) {
            activeZoom = zoom;
            invalidateHierarchy();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //invalidateHierarchy();
        validate();
        //System.out.println(("posY: " + getY()));

        for (int x = 0; x < amountX; x++) {
            for (int y = 0; y < amountY; y++) {
                //draw the tiles
                drawable.draw(batch,getX() + x * fieldSize * activeZoom, getY() + getHeight() - (y + 1) * fieldSize * activeZoom, fieldSize * activeZoom, fieldSize * activeZoom);
            }
        }
    }

    @Override
    public float getPrefWidth() {
        return amountX * fieldSize * activeZoom;
    }

    @Override
    public float getPrefHeight() {
        return amountY * fieldSize * activeZoom;
    }
}
