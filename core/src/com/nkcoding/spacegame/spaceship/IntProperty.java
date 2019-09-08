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

    public IntProperty(boolean readonly, boolean notifyChanges, String name) {
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
            this.value = Integer.parseInt(value);
        }
        catch (Exception e) {
            this.value = 0;
        }
    }
}
