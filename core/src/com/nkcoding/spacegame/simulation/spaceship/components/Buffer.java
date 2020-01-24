package com.nkcoding.spacegame.simulation.spaceship.components;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.properties.FloatProperty;

import java.io.DataInputStream;

public abstract class Buffer extends Component {
    public static final String BUFFER_LEVEL_KEY = "BufferLevel";

    /**
     * mirror constructor
     */
    protected Buffer(ComponentDefBase componentDef, DataInputStream inputStream, Ship ship) {
        super(componentDef, ship);
    }

    /**
     * original constructor
     */
    protected Buffer(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }

    public class BufferModel extends ComponentModel {

        protected float capacity;
        public final FloatProperty bufferLevel = register(new FloatProperty( BUFFER_LEVEL_KEY) {
            @Override
            public void set(float value) {
                super.set(Math.min(capacity, value));
            }
        });
        protected float rechargeSpeed;

        public BufferModel(Ship.ShipModel shipModel, ComponentDef componentDef, float capacity, float rechargeSpeed) {
            super(shipModel, componentDef);
            this.capacity = capacity;
            this.rechargeSpeed = rechargeSpeed;
        }

        protected boolean isCharged() {
            return bufferLevel.get() == capacity;
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
    }
}
