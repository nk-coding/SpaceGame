package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;

public class BooleanProperty extends ExternalProperty<Boolean> {
    private boolean value = false;

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
        changed = true;
    }

    public BooleanProperty(boolean readonly, boolean notifyChanges, String name) {
        super(readonly, notifyChanges, name, DataTypes.Boolean);
    }


    @Override
    public void setInitValue(String value) {
        this.value = value.equalsIgnoreCase("true");
    }

    @Override
    public void set(Boolean value) {
        this.value = value;
        changed = true;
    }

    @Override
    public Boolean get2() {
        return value;
    }
}
