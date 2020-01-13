package com.nkcoding.spacegame.simulation.communication;

public final class TransmissionID {
    /**
     * SimulatedType
     * Simulated (via SimulatedType constructor
     * deserialize BodyState
     */
    public static final int CREATE_NEW = 1;
    /**
     * simulatedID
     * Ship: owner, simulatedID,
     *      componentsAmount
     *      [ComponentType, x, y, rotation, constructor][]
     */
    public static final int REMOVE = 2;
    /**
     * simulated id
     * receiveTransmission()
     */
    public static final int UPDATE = 3;
    /**
     * amount
     * [simulatedID, BodyState][]
     */
    public static final int UPDATE_BODY_STATE = 4;


}
