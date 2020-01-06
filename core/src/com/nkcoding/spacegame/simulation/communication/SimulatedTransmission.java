package com.nkcoding.spacegame.simulation.communication;

import com.nkcoding.communication.Transmission;

public class SimulatedTransmission extends Transmission {
    public final int simulatedID;

    public SimulatedTransmission(int id, int simulatedID) {
        super(id);
        this.simulatedID = simulatedID;
    }
}
