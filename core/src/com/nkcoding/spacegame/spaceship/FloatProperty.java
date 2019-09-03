package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;

public class FloatProperty extends ExternalProperty {
    private float value = 0f;

    public float get() {
        return value;
    }

    public void set(float value) {
        this.value = value;
    }

    public FloatProperty(boolean readonly, boolean notifyChanges) {
        super(readonly, notifyChanges);
    }

    @Override
    public void StartChangedHandler(ScriptingEngine engine) {
        if (notifyChanges && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), value);
        }
    }
}
