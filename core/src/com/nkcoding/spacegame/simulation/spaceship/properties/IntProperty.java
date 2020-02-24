package com.nkcoding.spacegame.simulation.spaceship.properties;

public class IntProperty extends CacheProperty<Integer> {
    private int value = 0;

    public IntProperty(String name) {
        super(name);
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        if (this.value != value) changed = true;
        this.value = value;
    }

    @Override
    public void setInitValue(String value) {
        try {
            set(Integer.parseInt(value));
        } catch (Exception e) {
            set(0);
        }
    }

    @Override
    public final void set(Integer value) {
        set(value.intValue());
    }

    @Override
    public Integer get2() {
        return value;
    }
}
