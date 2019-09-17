package com.nkcoding.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

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
        return isScrollingDisabledX() ? getPrefWidth() : 0;
    }

    @Override
    public float getMinHeight() {
        return isScrollingDisabledY() ? getPrefHeight() : 0;
    }
}
