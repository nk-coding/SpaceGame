package com.nkcoding.spacegame.simulation.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UpdateTransmission {
    public final short updateID;

    public UpdateTransmission(short updateID) {
        this.updateID = updateID;
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeShort(updateID);
    }
}
