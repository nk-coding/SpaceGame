package com.nkcoding.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.nkcoding.spacegame.spaceship.ExternalPropertyData;

public class PropertyBox extends WidgetGroup {
    //the name of the ExternalProperty
    private String name;

    //the data for the ExternalProperty
    private ExternalPropertyData data;

    //the style of this
    private PropertyBoxStyle style;

    //the Label for the Name
    private Label nameLabel;

    //the Label for "value"
    private Label valueLabel;

    //the Label for "change handler":
    private Label changedLabel;

    //the TextField for "value"
    private TextField valueTextField;

    //the TextField for "changed handler"
    private TextField changedTextField;


    public PropertyBox(PropertyBoxStyle style, String name, ExternalPropertyData data) {
        this.style = style;
        this.name = name;
        this.data = data;
        //setTouchable(Touchable.enabled);
        init();
    }

    public void update(String name, ExternalPropertyData data) {
        //TODO maybe add some safe stuff
        this.name = name;
        this.data = data;
        init();
    }

    private void init() {
        //init the name label
        if (nameLabel == null) nameLabel = new Label(name, style.labelStyle);
        else nameLabel.setText(name);
        //init the changed handler stuff
        if (changedLabel == null) changedLabel = new Label("changed handler", style.labelStyle);
        if (changedTextField == null) changedTextField = new TextField("", style.textFieldStyle);
        else changedTextField.setText("");
        addActor(changedTextField);
        //init the value stuff if necessary
        if (!data.readonly) {
            if (valueLabel == null) valueLabel = new Label("value", style.labelStyle);
            if (valueTextField == null) valueTextField = new TextField("", style.textFieldStyle);
            else valueTextField.setText("");
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        applyTransform(batch, computeTransform());

        //draw background if possible
        if (style.background != null) {
            style.background.draw(batch, 0, 0, getWidth(), getHeight());
        }
        //draw all the children
        nameLabel.draw(batch, parentAlpha);
        changedLabel.draw(batch, parentAlpha);
        changedTextField.draw(batch, parentAlpha);
        if (!data.readonly) {
            valueLabel.draw(batch, parentAlpha);
            valueTextField.draw(batch, parentAlpha);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        changedTextField.act(delta);
        if (!data.readonly) valueTextField.act(delta);
    }

    @Override
    public void layout() {
        float bgLeftWidth = 0, bgRightWidth = 0, bgTopHeight = 0, bgBottomHeight = 0;
        if (style.background != null) {
            bgLeftWidth = style.background.getLeftWidth();
            bgRightWidth = style.background.getRightWidth();
            bgTopHeight = style.background.getTopHeight();
            bgBottomHeight = style.background.getBottomHeight();
        }
        float posY = bgBottomHeight;

        //changed handler
        changedTextField.setX(style.spacing + bgLeftWidth);
        changedTextField.setY(posY);
        posY += changedTextField.getHeight() + style.spacing;
        changedLabel.setX(style.spacing + bgLeftWidth);
        changedLabel.setY(posY);
        posY += changedLabel.getHeight() + style.spacing;

        //value if it exists
        if (!data.readonly) {
            valueTextField.setX(style.spacing + bgLeftWidth);
            valueTextField.setY(posY);
            posY += valueTextField.getHeight() + style.spacing;
            valueLabel.setX(style.spacing + bgLeftWidth);
            valueLabel.setY(posY);
            posY += valueLabel.getHeight() + style.spacing;
        }

        //name label
        nameLabel.setX(bgLeftWidth);
        nameLabel.setY(posY);
        nameLabel.setWidth(getWidth() - bgLeftWidth - bgRightWidth);
    }


    @Override
    public float getPrefHeight() {
        float prefHeight = 0;
        //border from background drawable if it exists
        if (style.background != null) prefHeight += style.background.getTopHeight() + style.background.getBottomHeight();
        //name label
        prefHeight += nameLabel.getPrefHeight();
        prefHeight += style.spacing;
        //changed handler
        prefHeight += changedLabel.getPrefHeight();
        prefHeight += changedTextField.getPrefHeight();
        prefHeight += style.spacing;
        //value if it exists
        if (!data.readonly) {
            prefHeight += valueLabel.getPrefHeight();
            prefHeight += valueTextField.getPrefHeight();
            prefHeight += style.spacing * 2;
        }
        return prefHeight;
    }

    public static class PropertyBoxStyle {
        public Drawable background;

        public Label.LabelStyle labelStyle;

        public TextField.TextFieldStyle textFieldStyle;

        public float spacing;
    }
}
