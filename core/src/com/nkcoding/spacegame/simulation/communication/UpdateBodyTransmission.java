package com.nkcoding.spacegame.simulation.communication;

import com.nkcoding.spacegame.simulation.BodyState;

public class UpdateBodyTransmission extends SimulatedTransmission {
    public final BodyState bodyState;

    public UpdateBodyTransmission(int simulatedID, BodyState bodyState) {
        super(TransmissionID.UPDATE_BODY_STATE, simulatedID);
        this.bodyState = bodyState;
    }
}
