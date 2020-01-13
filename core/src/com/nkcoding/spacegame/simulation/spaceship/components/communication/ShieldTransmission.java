package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.IOException;

public class ShieldTransmission extends UpdateComponentTransmission {

    public final boolean shieldEnabled;

    public ShieldTransmission(Component component, boolean shieldEnabled) {
        super(ComponentUpdateID.SHIELD, component);
        this.shieldEnabled = shieldEnabled;
    }

    public ShieldTransmission(DataInputStream inputStream) throws IOException {
        super(ComponentUpdateID.SHIELD, inputStream);
        this.shieldEnabled = inputStream.readBoolean();
    }
}
