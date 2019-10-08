package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;

public class FloatProperty extends ExternalProperty<Float> {
    private float value = 0f;

    public float get() {
        return value;
    }

    public void set(float value) {
        if (this.value != value) changed = true;
        this.value = value;
    }

    public FloatProperty(boolean readonly, boolean notifyChanges, String name) {
        super(readonly, notifyChanges, name, DataTypes.Float);
    }

    @Override
    public void setInitValue(String value) {
        try {
            this.value = Float.parseFloat(value);
        } catch (Exception e) {
            this.value = 0f;
        }
    }

    @Override
    public void set(Float value) {
        if (this.value != value) changed = true;
        this.value = value;
    }

    @Override
    public Float get2() {
        return value;
    }
}
