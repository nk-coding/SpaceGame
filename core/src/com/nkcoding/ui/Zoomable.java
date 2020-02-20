package com.nkcoding.ui;

import com.badlogic.gdx.math.Rectangle;

public interface Zoomable {
    /**
     * @param cullingArea The culling area in the child actor's coordinates.
     * @param zoom        the zoom that the control should apply
     */
    void setCullingArea(Rectangle cullingArea, float zoom);

    /**
     * gets the width of the component if no scale is applied
     */
    float getUnscaledWidth();

    /**
     * gets the height of the component if no scale is applied
     */
    float getUnscaledHeight();
}
