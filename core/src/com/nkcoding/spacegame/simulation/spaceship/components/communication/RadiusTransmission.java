package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.spaceship.components.Component;

public class RadiusTransmission extends UpdateComponentTransmission {
    public final float radius;

    public RadiusTransmission(Component component, float radius) {
        super(component, ComponentUpdateID.RADIUS);
        this.radius = radius;
    }
}
