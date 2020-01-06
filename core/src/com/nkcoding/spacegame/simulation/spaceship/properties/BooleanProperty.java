package com.nkcoding.spacegame.simulation.spaceship.properties;

public class BooleanProperty extends ExternalProperty<Boolean> {
    private boolean value = false;

    public BooleanProperty(boolean readonly, boolean notifyChanges, String name) {
        super(readonly, notifyChanges, name);
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        if (this.value != value) changed = true;
        this.value = value;
    }

    @Override
    public void setInitValue(String value) {
        this.value = value.equalsIgnoreCase("true");
    }

    @Override
    public final void set(Boolean value) {
        set(value.booleanValue());
    }

    @Override
    public Boolean get2() {
        return value;
    }
}
