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
    private final Label nameLabel;

    //the TextField for "value"
    private final TextField valueTextField;

    //the label for the getter
    private final Label getterLabel;

    //the label for the setter
    private final Label setterLabel;

    //the TextField for "changed handler"
    private final TextField handlerTextField;

    //the ImageButton which redirects to the code
    private final ImageButton codeImageButton;

    public PropertyBox(PropertyBoxStyle style, String name, ExternalPropertyData data,
                       Map<String, NormalMethodDefinition> methods) {
        this.style = style;
        this.name = name;
        this.data = data;
        this.methods = methods;
        //create all the UI components
        nameLabel = new Label("", style.labelStyle);
        getterLabel = new Label("", style.labelStyle);
        setterLabel = new Label("", style.labelStyle);
        valueTextField = new TextField("", style.textFieldStyle);
        valueTextField.setTextFieldListener((textField, c) -> verify());
        handlerTextField = new TextField("", style.textFieldStyle);
        handlerTextField.setTextFieldListener((textField, c) -> verify());
        codeImageButton = new ImageButton(style.codeButtonDrawable);
        codeImageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                codeButtonClicked();
            }
        });
        addActor(nameLabel);
        addActor(getterLabel);
        addActor(setterLabel);
        addActor(valueTextField);
        addActor(handlerTextField);
        addActor(codeImageButton);
        init();
    }

    public PropertyBoxStyle getStyle() {
        return style;
    }

    protected String getHandlerName() {
        return handlerTextField.getText();
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
            if (data.supportsWrite) {
                data.initData = valueTextField.getText();
            }
            if (data.supportsChangedHandler) {
                data.handlerName = handlerTextField.getText();
            }
        }
    }

    /**
     * updates this PropertyBox with the data from a new ExternalPropertyData
     */
    private void init() {
        nameLabel.setText(name);
        if (data.supportsRead) {
            getterLabel.setText(data.type + " " +  data.getterName + "()");
        }
        if (data.supportsWrite) {
            getterLabel.setText("void " + data.setterName + "(string id, " + data.type + " val)");
            valueTextField.setText(data.initData);
        }
        if (data.supportsChangedHandler) {
            handlerTextField.setText(data.handlerName);
        }
        verify();
        invalidateHierarchy();
    }

    public void verify() {
        if (valueTextField != null && data.supportsWrite) {
            valueTextField.setColor(valueTextField.getText().equals("") || data.verifyInit(valueTextField.getText())
                    ? style.legalInputColor : style.illegalInputColor);
        }

        if (data.supportsChangedHandler) {
            handlerTextField.setColor(data.verifyHandler(handlerTextField.getText(), methods)
                    ? style.legalInputColor : style.illegalInputColor);
        }

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
        if (data.supportsRead) {
            getterLabel.draw(batch, parentAlpha);
        }
        if (data.supportsWrite) {
            setterLabel.draw(batch, parentAlpha);
            valueTextField.draw(batch, parentAlpha);
        }
        if (data.supportsChangedHandler) {
            codeImageButton.draw(batch, parentAlpha);
            handlerTextField.draw(batch, parentAlpha);
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
        if (data.supportsChangedHandler) {
            handlerTextField.setX(style.spacing + bgLeftWidth);
            handlerTextField.setY(posY);
            handlerTextField.setWidth(getWidth() - style.spacing - bgLeftWidth - bgRightWidth - style.spacing - handlerTextField.getHeight());

            codeImageButton.setX(getWidth() - bgRightWidth - handlerTextField.getHeight());
            codeImageButton.setY(posY);
            codeImageButton.setWidth(handlerTextField.getHeight());
            codeImageButton.setHeight(handlerTextField.getHeight());

            posY += handlerTextField.getHeight() + style.spacing;
        }

        //value text field
        if (data.supportsWrite) {
            valueTextField.setX(style.spacing + bgLeftWidth);
            valueTextField.setY(posY);
            valueTextField.setWidth(getWidth() - style.spacing - bgLeftWidth - bgRightWidth);
            posY += valueTextField.getHeight() + style.spacing;

            setterLabel.setX(bgLeftWidth);
            setterLabel.setY(posY);
            setterLabel.setWidth(getWidth() - bgLeftWidth - bgRightWidth);
            posY += setterLabel.getHeight() + style.spacing;
        }

        //getter
        if (data.supportsRead) {
            getterLabel.setX(bgLeftWidth);
            getterLabel.setY(posY);
            getterLabel.setWidth(getWidth() - bgLeftWidth - bgRightWidth);
            posY += getterLabel.getHeight() + style.spacing;
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
        if (style.background != null) {
            prefHeight += style.background.getTopHeight() + style.background.getBottomHeight();
        }
        prefHeight += nameLabel.getPrefHeight();

        if (data.supportsRead) {
            prefHeight += getterLabel.getPrefHeight();
            prefHeight += style.spacing;
        }

        if (data.supportsWrite) {
            prefHeight += setterLabel.getPrefHeight();
            prefHeight += valueTextField.getPrefHeight();
            prefHeight += 2 * style.spacing;
        }

        if (data.supportsChangedHandler) {
            prefHeight += handlerTextField.getPrefHeight();
            prefHeight += style.spacing;
        }

        return prefHeight;
    }

    public DataType getDataType() {
        return data.type;
    }

    /**
     * overwrite this to handle the code button events
     */
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
