package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.spaceship.components.Component;

public class ShieldTransmission extends UpdateComponentTransmission {

    public final boolean shieldEnabled;

    public ShieldTransmission(Component component, boolean shieldEnabled) {
        super(component, ComponentUpdateID.SHIELD);
        this.shieldEnabled = shieldEnabled;
    }
}
