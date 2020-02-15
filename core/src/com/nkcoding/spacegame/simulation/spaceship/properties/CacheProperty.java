package com.nkcoding.spacegame.simulation.spaceship.properties;

public abstract class CacheProperty<T> extends ExternalProperty<T>{
    private T cache = null;

    public CacheProperty(String name) {
        super(name);
    }

    @Override
    public void setFromExternal(T value) {
        cache = value;
    }

    @Override
    public void update() {
        if (cache != null) {
            set(cache);
            cache = null;
        }
    }
}
