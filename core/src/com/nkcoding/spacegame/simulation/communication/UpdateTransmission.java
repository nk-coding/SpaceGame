package com.nkcoding.spacegame.simulation.communication;

import java.io.DataOutputStream;
import java.io.IOException;

public class UpdateTransmission {
    public final int updateID;

    public UpdateTransmission(int updateID) {
        this.updateID = updateID;
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(updateID);
    }
}
