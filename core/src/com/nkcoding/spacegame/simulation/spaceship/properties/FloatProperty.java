package com.nkcoding.spacegame.simulation.spaceship.properties;

public class FloatProperty extends CacheProperty<Float> {
    private float value = 0f;

    public FloatProperty(String name) {
        super(name);
    }

    public float get() {
        return value;
    }

    public void set(float value) {
        if (this.value != value) changed = true;
        this.value = value;
    }

    @Override
    public void setInitValue(String value) {
        try {
            set(Float.parseFloat(value));
        } catch (Exception e) {
            set(0f);
        }
    }

    @Override
    public final void set(Float value) {
        set(value.floatValue());
    }

    @Override
    public Float get2() {
        return value;
    }
}
