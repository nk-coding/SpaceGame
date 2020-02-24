package com.nkcoding.spacegame.simulation.spaceship.properties;

public class StringProperty extends CacheProperty<String> {
    private String value = "";

    public StringProperty(String name) {
        super(name);
    }

    public String get() {
        return value;
    }

    public void set(String value) {
        if (this.value.equals(value)) changed = true;
        this.value = value;
    }

    @Override
    public String get2() {
        return value;
    }

    @Override
    public void setInitValue(String value) {
        set(value);
    }
}
