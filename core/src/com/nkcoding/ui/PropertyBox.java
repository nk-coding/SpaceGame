package com.nkcoding.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.interpreter.compiler.NormalMethodDefinition;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyData;

import java.util.Map;

public class PropertyBox extends WidgetGroup {
    //Map with all the methods
    private final Map<String, NormalMethodDefinition> methods;
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

    //the ImageButton which redirects to the code
    private ImageButton codeImageButton;

    public PropertyBox(PropertyBoxStyle style, String name, ExternalPropertyData data,
                       Map<String, NormalMethodDefinition> methods) {
        this.style = style;
        this.name = name;
        this.data = data;
        this.methods = methods;
        init();
    }

    public PropertyBoxStyle getStyle() {
        return style;
    }

    protected String getHandlerName() {
        return changedTextField.getText();
    }

    /**
     * updates this control to a new ExternalPropertyData
     * automatically calls save()
     */
    public void update(String name, ExternalPropertyData data) {
        save();
        this.name = name;
        this.data = data;
        init();
    }

    /**
     * saves the TextFields to the ExternalPropertyData, should be called before it is deleted
     */
    public void save() {
        if (data != null) {
            if (!data.readonly) data.initData = valueTextField.getText();
            data.handlerName = changedTextField.getText();
        }
    }

    private void init() {
        //init the name label
        if (nameLabel == null) {
            nameLabel = new Label(name, style.labelStyle);
            addActor(nameLabel);
        } else nameLabel.setText(name);
        //init the changed handler stuff
        if (changedLabel == null) {
            changedLabel = new Label("handler: " + data.type, style.labelStyle);
            addActor(changedLabel);
        } else {
            changedLabel.setText("handler: " + data.type);
        }
        if (changedTextField == null) {
            changedTextField = new TextField(data.handlerName, style.textFieldStyle);
            changedTextField.setTextFieldListener((textField, c) -> verify());
            addActor(changedTextField);
        } else changedTextField.setText(data.handlerName);
        if (codeImageButton == null) {
            codeImageButton = new ImageButton(style.codeButtonDrawable);
            codeImageButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    codeButtonClicked();
                }
            });
            addActor(codeImageButton);
        }

        //init the value stuff if necessary
        if (!data.readonly) {
            if (valueLabel == null) valueLabel = new Label("value", style.labelStyle);
            if (valueTextField == null) {
                valueTextField = new TextField(data.initData, style.textFieldStyle);
                valueTextField.setTextFieldListener((textField, c) -> verify());
                verify();
                addActor(valueTextField);
            } else valueTextField.setText(data.initData);
        }
        verify();
        invalidateHierarchy();
    }

    public void verify() {
        if (valueTextField != null)
            valueTextField.setColor(valueTextField.getText().equals("") || data.verifyInit(valueTextField.getText())
                    ? style.legalInputColor : style.illegalInputColor);
        changedTextField.setColor(data.verifyHandler(changedTextField.getText(), methods)
                ? style.legalInputColor : style.illegalInputColor);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        applyTransform(batch, computeTransform());
        batch.setColor(getColor());

        //draw background if possible
        if (style.background != null) {
            style.background.draw(batch, 0, 0, getWidth(), getHeight());
        }
        //draw all the children
        nameLabel.draw(batch, parentAlpha);
        changedLabel.draw(batch, parentAlpha);
        changedTextField.draw(batch, parentAlpha);
        codeImageButton.draw(batch, parentAlpha);
        if (!data.readonly) {
            valueLabel.draw(batch, parentAlpha);
            valueTextField.draw(batch, parentAlpha);
        }
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
        changedTextField.setWidth(getWidth() - style.spacing - bgLeftWidth - bgRightWidth - style.spacing - changedTextField.getHeight());
        codeImageButton.setX(getWidth() - bgRightWidth - changedTextField.getHeight());
        codeImageButton.setY(posY);
        codeImageButton.setWidth(changedTextField.getHeight());
        codeImageButton.setHeight(changedTextField.getHeight());
        posY += changedTextField.getHeight() + style.spacing;
        changedLabel.setX(style.spacing + bgLeftWidth);
        changedLabel.setY(posY);
        posY += changedLabel.getHeight() + style.spacing;

        //value if it exists
        if (!data.readonly) {
            valueTextField.setX(style.spacing + bgLeftWidth);
            valueTextField.setY(posY);
            valueTextField.setWidth(getWidth() - style.spacing - bgLeftWidth - bgRightWidth);
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
        if (style.background != null)
            prefHeight += style.background.getTopHeight() + style.background.getBottomHeight();
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

    public DataType getDataType() {
        return data.type;
    }

    public void codeButtonClicked() {

    }

    public static class PropertyBoxStyle {
        public Drawable background;

        public Drawable codeButtonDrawable;

        public Label.LabelStyle labelStyle;

        public TextField.TextFieldStyle textFieldStyle;

        public Color illegalInputColor;

        public Color legalInputColor;

        public float spacing;
    }
}
