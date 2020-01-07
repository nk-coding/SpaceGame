package com.nkcoding.spacegame.simulation.communication;

import com.nkcoding.communication.Transmission;
import com.nkcoding.spacegame.simulation.BodyState;

public class UpdateBodysTransmission extends Transmission {
    public final BodyState[] bodyStates;

    public UpdateBodysTransmission(BodyState[] bodyStates) {
        super(TransmissionID.UPDATE_BODY_STATE);
        this.bodyStates = bodyStates;
    }
}
