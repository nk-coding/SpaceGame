package com.nkcoding.spacegame.simulation.spaceship.properties;

public abstract class VirtualProperty<T> extends ExternalProperty<T> {

    public VirtualProperty(String name) {
        super(name);
    }

    @Override
    public void set(T value) {
        changed = true;
    }

    @Override
    public abstract T get2();
}
