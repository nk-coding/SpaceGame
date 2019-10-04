package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;

public class IntProperty extends ExternalProperty<Integer> {
    private int value = 0;

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
        changed = true;
    }

    public IntProperty(boolean readonly, boolean notifyChanges, String name) {
        super(readonly, notifyChanges, name, DataTypes.Integer);
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

    @Override
    public void set(Integer value) {
        this.value = value;
        changed = true;
    }

    @Override
    public Integer get2() {
        return value;
    }
}
