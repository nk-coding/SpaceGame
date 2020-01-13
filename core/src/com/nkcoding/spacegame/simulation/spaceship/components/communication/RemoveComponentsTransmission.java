package com.nkcoding.spacegame.simulation.spaceship.components.communication;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class RemoveComponentsTransmission extends UpdateTransmission {
    public final int[][] components;

    public RemoveComponentsTransmission(List<Component.ComponentModel> componentModels) {
        super(Ship.REMOVE_COMPONENTS);
        this.components = new int[componentModels.size()][2];
        for (int x = 0; x < componentModels.size(); x++) {
            components[x][0] = componentModels.get(x).getComponentDef().getX();
            components[x][1] = componentModels.get(x).getComponentDef().getY();
        }
    }

    public RemoveComponentsTransmission(DataInputStream inputStream) throws IOException {
        super(Ship.REMOVE_COMPONENTS);
        int amount = inputStream.readInt();
        components = new int[amount][2];
        for (int i = 0; i < amount; i++) {
            components[i][0] = inputStream.readInt();
            components[i][1] = inputStream.readInt();
        }
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeInt(components.length);
        for (int i = 0; i < components.length; i++) {
            outputStream.writeInt(components[i][0]);
            outputStream.writeInt(components[i][1]);
        }
    }
}
