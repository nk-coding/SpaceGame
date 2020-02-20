package com.nkcoding.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

//ScrollPane which sets min width / height to pref if scrolling is disabled
public class CustomScrollPane extends ScrollPane {
    public CustomScrollPane(Actor widget) {
        super(widget);
    }

    public CustomScrollPane(Actor widget, Skin skin) {
        super(widget, skin);
    }

    public CustomScrollPane(Actor widget, Skin skin, String styleName) {
        super(widget, skin, styleName);
    }

    public CustomScrollPane(Actor widget, ScrollPaneStyle style) {
        super(widget, style);
    }

    @Override
    public float getMinWidth() {
        float z = isScrollingDisabledX() ? getPrefWidth() : 0;
        return isScrollingDisabledX() ? getPrefWidth() : 0;
    }

    @Override
    public float getMinHeight() {
        return isScrollingDisabledY() ? getPrefHeight() : 0;
    }

    /**
     * it fu***ing works
     * don't touch it
     * don't question it
     * this fixes the 2 layout() for resize bug
     */
    @Override
    public void setBounds(float x, float y, float width, float height) {
        Actor widget = getActor();
        if (widget instanceof Layout) {
            validate();
            float prefHeight = ((Layout) widget).getPrefHeight();
            if (height > prefHeight) {
                if (isScrollY()) {
                    if (!isForceScrollY()) {
                        setHeight(1000000000);
                        invalidateHierarchy();
                    }
                }
            } else {
                if (!isScrollY()) {
                    if (!isScrollingDisabledY()) {
                        setHeight(0);
                        invalidateHierarchy();
                    }
                }
            }
        }
        super.setBounds(x, y, width, height);
    }

    @Override
    public void layout() {
        super.layout();
        visualScrollX(MathUtils.clamp(getVisualScrollX(), 0, getMaxX()));
        visualScrollY(MathUtils.clamp(getVisualScrollY(), 0, getMaxY()));
    }
}
