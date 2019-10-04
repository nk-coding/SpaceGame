package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;

public class StringProperty extends ExternalProperty<String> {
    private String value = "";

    public String get() {
        return value;
    }

    public void set(String value) {
        this.value = value;
        changed = true;
    }

    @Override
    public String get2() {
        return value;
    }

    public StringProperty(boolean readonly, boolean notifyChanges, String name) {
        super(readonly, notifyChanges, name, DataTypes.String);
    }

    @Override
    public void startChangedHandler(ScriptingEngine engine) {
        if (notifyChanges && changed && getChangedMethodStatement() != null) {
            engine.runMethod(getChangedMethodStatement(), value);
        }
    }

    @Override
    public void setInitValue(String value) {
        this.value = value;
    }
}
