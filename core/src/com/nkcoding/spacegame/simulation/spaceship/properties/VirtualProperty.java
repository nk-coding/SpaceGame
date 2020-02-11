package com.nkcoding.spacegame.simulation.spaceship.properties;

public abstract class VirtualProperty<T> extends ExternalProperty<T> {

    public VirtualProperty(String name) {
        super(name);
    }

    @Override
    public void setInitValue(String value) {
        throw new UnsupportedOperationException("implement this if this functionality is necessary");
    }

    @Override
    public void set(T value) {
        changed = true;
    }
}
