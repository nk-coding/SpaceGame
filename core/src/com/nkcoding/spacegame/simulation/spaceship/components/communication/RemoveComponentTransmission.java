package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

public class RemoveComponentTransmission extends UpdateComponentTransmission {
    public RemoveComponentTransmission(Component component) {
        super(component, Ship.REMOVE_COMPONENT);
    }
}
