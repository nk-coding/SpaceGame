package com.nkcoding.spacegame.simulation.communication;

import java.io.DataOutputStream;
import java.io.IOException;

public class UpdateTransmission {
    public final short updateID;
    public final int simulatedID;

    public UpdateTransmission(short updateID, int simulatedID) {
        this.updateID = updateID;
        this.simulatedID = simulatedID;
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(simulatedID);
        outputStream.writeShort(updateID);
    }
}
