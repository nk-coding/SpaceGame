package com.nkcoding.spacegame.simulation.spaceship.components;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.properties.FloatProperty;

public class Buffer extends Component {
    public static final String BUFFER_LEVEL_KEY = "BufferLevel";
    protected float capacity;
    public final FloatProperty bufferLevel = register(new FloatProperty(true, true, BUFFER_LEVEL_KEY) {
        @Override
        public void set(float value) {
            super.set(Math.min(capacity, value));
        }
    });
    protected float rechargeSpeed;

    /**
     * creates a new BufferComponent
     *
     * @param componentDef  the definition for this Component
     * @param ship          the Ship for this Component
     * @param capacity      how much power is allowed
     * @param rechargeSpeed how much power does it consume per tick
     */
    protected Buffer(ComponentDef componentDef, Ship ship, float capacity, float rechargeSpeed) {
        super(componentDef, ship);
        this.capacity = capacity;
        this.rechargeSpeed = rechargeSpeed;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        //set the requested power
        //on the last step, it still wants full power because otherwise this could lead to a long time where it tries to recharge
        // 0.0 -> 0.99 -> 0.999 etc.
        powerRequested.set(bufferLevel.get() < capacity ? rechargeSpeed : 0f);
        bufferLevel.set(bufferLevel.get() + powerReceived.get() * delta);
    }

    protected boolean isCharged() {
        return bufferLevel.get() == capacity;
    }
}
