package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

public class UpdateComponentTransmission extends UpdateTransmission {
    public final int x;
    public final int y;

    public UpdateComponentTransmission(int simulatedID, int updateID, int x, int y) {
        super(simulatedID, updateID);
        this.x = x;
        this.y = y;
    }

    public UpdateComponentTransmission(Component component, int updateID) {
        this(component.getShip().id, updateID, component.getX(), component.getY());
    }
}
