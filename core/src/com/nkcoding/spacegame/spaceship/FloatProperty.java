package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;

public class FloatProperty extends ExternalProperty<Float> {
    private float value = 0f;

    public float get() {
        return value;
    }

    public void set(float value) {
        this.value = value;
    }

    public FloatProperty(boolean readonly, boolean notifyChanges, String name) {
        super(readonly, notifyChanges, name);
    }

    @Override
    public void startChangedHandler(ScriptingEngine engine) {
        if (notifyChanges && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), value);
        }
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
        this.value = value;
    }

    @Override
    public Float get2() {
        return value;
    }
}
