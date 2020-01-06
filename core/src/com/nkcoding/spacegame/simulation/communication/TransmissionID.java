package com.nkcoding.spacegame.simulation.communication;

public final class TransmissionID {
    public static final int CREATE_NEW = 1;
    public static final int REMOVE = 2;
    public static final int UPDATE = 3;
    public static final int UPDATE_BODY_STATE = 4;

    private TransmissionID() {
    }
}
