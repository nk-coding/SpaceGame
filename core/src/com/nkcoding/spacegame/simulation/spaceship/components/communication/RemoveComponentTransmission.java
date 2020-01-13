package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.IOException;

public class RemoveComponentTransmission extends UpdateComponentTransmission {
    public RemoveComponentTransmission(Component component) {
        super(Ship.REMOVE_COMPONENT, component);
    }

    public RemoveComponentTransmission(DataInputStream inputStream) throws IOException {
        super(Ship.REMOVE_COMPONENT, inputStream);
    }
}
