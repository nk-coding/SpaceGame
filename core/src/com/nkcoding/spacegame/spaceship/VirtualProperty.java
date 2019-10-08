package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ScriptingEngine;

import java.util.LinkedList;

public class VirtualProperty<T> extends ExternalProperty<T>{
    private LinkedList<T> updatedValues;

    public VirtualProperty(String name, String type) {
        super(true, true, name, type);
        updatedValues = new LinkedList<>();
    }

    @Override
    public void setInitValue(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(T value) {
        if (value != null)
            updatedValues.add(value);
    }

    @Override
    public void startChangedHandler(ScriptingEngine engine) {
        while (!updatedValues.isEmpty()) {
            engine.runMethod(getChangedMethodStatement(), updatedValues.pop());
        }
    }

    @Override
    public T get2() {
        //does nothing
        return null;
    }
}
