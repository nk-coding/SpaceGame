package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.util.List;

public class RemoveComponentsTransmission extends UpdateTransmission {
    public final int[][] components;

    public RemoveComponentsTransmission(int simulatedID, List<Component.ComponentModel> componentModels) {
        super(simulatedID, Ship.REMOVE_COMPONENTS);
        this.components = new int[componentModels.size()][2];
        for (int x = 0; x < componentModels.size(); x++) {
            components[x][0] = componentModels.get(x).getComponentDef().getX();
            components[x][1] = componentModels.get(x).getComponentDef().getY();
        }
    }
}
