package com.nkcoding.spacegame.simulation.communication;

public class UpdateTransmission extends SimulatedTransmission {
    public final int updateID;

    public UpdateTransmission(int simulatedID, int updateID) {
        super(TransmissionID.UPDATE, simulatedID);
        this.updateID = updateID;
    }
}
