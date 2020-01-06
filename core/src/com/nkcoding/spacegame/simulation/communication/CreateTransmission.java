package com.nkcoding.spacegame.simulation.communication;

import com.nkcoding.communication.Transmission;
import com.nkcoding.spacegame.simulation.BodyState;
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

    /**
     * the bodyState at transfer time
     */
    public final BodyState bodyState;

    public CreateTransmission(SimulatedType type, int owner, BodyState bodyState) {
        super(TransmissionID.CREATE_NEW);
        this.type = type;
        this.owner = owner;
        this.bodyState = bodyState;
    }
}
