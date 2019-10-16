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

    @Override
    public void copyFrom(ExternalProperty<T> from) {
        //don't copy value, because this does not make any sense for a virtual property
        setChangedMethodStatement(from.getChangedMethodStatement());
    }
}
