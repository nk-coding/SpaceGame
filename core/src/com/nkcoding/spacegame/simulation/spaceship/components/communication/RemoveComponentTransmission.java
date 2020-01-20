package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RemoveComponentTransmission extends UpdateTransmission {

    public final int x;
    public final int y;

    public RemoveComponentTransmission(Component component, int simulatedID) {
        super(Ship.REMOVE_COMPONENT, simulatedID);
        this.x = component.getX();
        this.y = component.getY();
    }

    public RemoveComponentTransmission(DataInputStream inputStream) throws IOException {
        super(Ship.REMOVE_COMPONENT, 0);
        this.x = inputStream.readInt();
        this.y = inputStream.readInt();
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeInt(x);
        outputStream.writeInt(y);
    }
}
