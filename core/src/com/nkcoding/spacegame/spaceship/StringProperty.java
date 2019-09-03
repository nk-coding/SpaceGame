package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;

public class StringProperty extends ExternalProperty {
    private String value = "";

    public String get() {
        return value;
    }

    public void set(String value) {
        this.value = value;
    }

    public StringProperty(boolean readonly, boolean notifyChanges) {
        super(readonly, notifyChanges);
    }

    @Override
    public void StartChangedHandler(ScriptingEngine engine) {
        if (notifyChanges && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), value);
        }
    }
}
