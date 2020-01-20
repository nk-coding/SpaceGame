package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RadiusTransmission extends UpdateComponentTransmission {
    public final float radius;

    public RadiusTransmission(Component component, float radius) {
        super(ComponentUpdateID.RADIUS, component);
        this.radius = radius;
    }

    public RadiusTransmission(DataInputStream inputStream) throws IOException {
        super(ComponentUpdateID.RADIUS, inputStream);
        this.radius = inputStream.readFloat();
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeFloat(radius);
    }
}
