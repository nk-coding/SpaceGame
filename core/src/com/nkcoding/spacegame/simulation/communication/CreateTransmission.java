package com.nkcoding.spacegame.simulation.communication;

import com.nkcoding.communication.Transmission;
import com.nkcoding.spacegame.simulation.SimulatedType;

public class CreateTransmission extends Transmission {
    /**
     * which type of simulated should be added
     */
    public final SimulatedType type;

    /**
     * id of the owner
     */
    public final int owner;

    public CreateTransmission(SimulatedType type, int owner) {
        super(TransmissionID.CREATE_NEW);
        this.type = type;
        this.owner = owner;
    }
}
