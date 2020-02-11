package com.nkcoding.spacegame.simulation;

import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.util.IOBiFunction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum SimulatedType {
    Ship(com.nkcoding.spacegame.simulation.Ship::deserialize, (short)0),
    Explosion(com.nkcoding.spacegame.simulation.Explosion::deserialize, (short)1),
    CannonBullet(com.nkcoding.spacegame.simulation.CannonBullet::deserialize,(short)2);

    //constructor to create new instances via multiplayer
    public final IOBiFunction<SpaceSimulation, DataInputStream, Simulated> constructor;
    private final short index;

    SimulatedType(IOBiFunction<SpaceSimulation, DataInputStream, Simulated> constructor, short index) {
        this.constructor = constructor;
        this.index = index;
    }

    public static SimulatedType deserialize(DataInputStream inputStream) throws IOException {
        switch (inputStream.readShort()) {
            case 0:
                return Ship;
            case 1:
                return Explosion;
            case 2:
                return CannonBullet;
            default:
                throw new IllegalStateException();
        }
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeShort(index);
    }
}
