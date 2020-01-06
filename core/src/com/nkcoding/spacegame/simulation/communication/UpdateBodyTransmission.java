package com.nkcoding.spacegame.simulation.communication;

import com.nkcoding.communication.Transmission;
import com.nkcoding.spacegame.simulation.BodyState;

public class UpdateBodyTransmission extends Transmission {
    public final BodyState bodyState;

    public UpdateBodyTransmission(BodyState bodyState) {
        super(TransmissionID.UPDATE_BODY_STATE);
        this.bodyState = bodyState;
    }
}
