package com.nkcoding.spacegame.spaceship;

public abstract class VirtualProperty<T> extends ExternalProperty<T> {

    public VirtualProperty(boolean notifyChanges, String name, String type) {
        super(true, notifyChanges, name, type);
    }

    @Override
    public void setInitValue(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(T value){
        changed = true;
    }

    @Override
    public abstract T get2();
}
