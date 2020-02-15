package com.nkcoding.spacegame.simulation.spaceship.properties;

public abstract class CacheProperty<T> extends ExternalProperty<T>{
    private T cache;

    public CacheProperty(String name) {
        super(name);
    }


}
