package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DamageTransmission extends UpdateComponentTransmission {

    public final int damage;
    public final int damageID;

    public DamageTransmission(Component component, int damage, int damageID) {
        super(ComponentUpdateID.DAMAGE, component);
        this.damage = damage;
        this.damageID = damageID;
    }

    public DamageTransmission(DataInputStream inputStream) throws IOException {
        super(ComponentUpdateID.DAMAGE, inputStream);
        this.damage = inputStream.readInt();
        this.damageID = inputStream.readInt();
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeInt(damage);
        outputStream.writeInt(damageID);
    }

    @Override
    public String toString() {
        return "damageID: " + damageID + ", damage: " + damage;
    }
}
