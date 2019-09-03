package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;

public class IntProperty extends ExternalProperty {
    private int value = 0;

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
    }

    public IntProperty(boolean readonly, boolean notifyChanges) {
        super(readonly, notifyChanges);
    }

    @Override
    public void StartChangedHandler(ScriptingEngine engine) {
        if (notifyChanges && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), value);
        }
    }
}
