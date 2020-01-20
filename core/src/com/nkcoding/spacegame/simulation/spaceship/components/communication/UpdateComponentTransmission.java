package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UpdateComponentTransmission extends UpdateTransmission {
    public final int componentUpdateID;
    public final int x;
    public final int y;

    public UpdateComponentTransmission(short componentUpdateID, int simulatedID, int x, int y) {
        super(Ship.UPDATE_COMPONENT, simulatedID);
        this.componentUpdateID = componentUpdateID;
        this.x = x;
        this.y = y;
    }

    public UpdateComponentTransmission(short componentUpdateID, Component component) {
        this(componentUpdateID, component.getShip().id, component.getX(), component.getY());
    }

    public UpdateComponentTransmission(short componentUpdateID, DataInputStream inputStream) throws IOException {
        this(componentUpdateID, 0, inputStream.readInt(), inputStream.readInt());
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeInt(x);
        outputStream.writeInt(y);
    }

    @Override
    public String toString() {
        return String.format("%d: (%d, %d)", componentUpdateID, x, y);
    }
}
