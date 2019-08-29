package com.nkcoding.ui;

import com.badlogic.gdx.math.Rectangle;

public interface Zoomable {
    /**
     * @param cullingArea The culling area in the child actor's coordinates.
     * @param zoom the zoom that the control should apply*/
    public void setCullingArea(Rectangle cullingArea, float zoom);
}
