package com.nkcoding.spacegame.simulation.spaceship.properties;

import com.nkcoding.interpreter.ConcurrentStackItem;
import com.nkcoding.interpreter.ScriptingEngine;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class NotifyProperty<T> extends ExternalProperty<T> {
    private LinkedList<T> updatedValues;

    public NotifyProperty(String name) {
        super(name);
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
    public void startChangedHandler(ScriptingEngine engine, final ConcurrentHashMap<String, ConcurrentStackItem> globalVariables) {
        if (allowParallel) {
            while (!updatedValues.isEmpty() && getChangedMethodStatement() != null) {
                engine.runMethod(getChangedMethodStatement(), this, globalVariables, updatedValues.pop());
            }
        } else if (!isRunning) {
            if (!updatedValues.isEmpty() && getChangedMethodStatement() != null) {
                engine.runMethod(getChangedMethodStatement(), this, globalVariables, updatedValues.pop());
            }
        }

    }

    @Override
    public T get2() {
        //does nothing
        return null;
    }
}
