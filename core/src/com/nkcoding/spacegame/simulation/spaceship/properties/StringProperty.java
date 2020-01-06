package com.nkcoding.spacegame.simulation.spaceship.properties;

public class StringProperty extends ExternalProperty<String> {
    private String value = "";

    public StringProperty(boolean readonly, boolean notifyChanges, String name) {
        super(readonly, notifyChanges, name);
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
        this.value = value;
    }
}
