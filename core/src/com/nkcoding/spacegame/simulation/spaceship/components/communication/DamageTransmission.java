package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.spaceship.components.Component;

public class DamageTransmission extends UpdateComponentTransmission {

    public final int damage;
    public final int damageID;

    public DamageTransmission(Component component, int damage, int damageID) {
        super(component, ComponentUpdateID.DAMAGE);
        this.damage = damage;
        this.damageID = damageID;
    }
}
