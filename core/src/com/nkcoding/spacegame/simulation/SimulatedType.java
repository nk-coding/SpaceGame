package com.nkcoding.spacegame.simulation;

import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.simulation.communication.CreateTransmission;

import java.util.function.BiFunction;

public enum SimulatedType {
    Ship(null),
    Explosion(null),
    CannonBullet(null);

    public final BiFunction<SpaceSimulation, CreateTransmission, Simulated> constructor;

    SimulatedType(BiFunction<SpaceSimulation, CreateTransmission, Simulated> constructor) {
        this.constructor = constructor;
    }
}
