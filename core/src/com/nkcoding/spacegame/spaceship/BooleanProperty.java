package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;

public class BooleanProperty extends ExternalProperty {
    private boolean value = false;

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    public BooleanProperty(boolean readonly, boolean notifyChanges, String name) {
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
        this.value = value.equalsIgnoreCase("true");
    }
}
