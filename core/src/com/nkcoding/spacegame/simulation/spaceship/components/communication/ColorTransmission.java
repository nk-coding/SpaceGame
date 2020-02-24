package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ColorTransmission extends UpdateComponentTransmission {
    public final int color;

    public ColorTransmission(Component component, int color) {
        super(ComponentUpdateID.COLOR, component);
        this.color = color;
    }

    public ColorTransmission(DataInputStream inputStream) throws IOException {
        super(ComponentUpdateID.COLOR, inputStream);
        color = inputStream.readInt();
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeInt(color);
    }
}
