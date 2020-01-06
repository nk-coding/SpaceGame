package com.nkcoding.spacegame.simulation.communication;

public class RemoveTransmission extends SimulatedTransmission {
    public RemoveTransmission(int simulatedID) {
        super(TransmissionID.REMOVE, simulatedID);
    }
}
