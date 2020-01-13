package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.properties.IntProperty;

import java.io.DataInputStream;

public class Engine extends Component {
    public static final String ENGINE_POWER_KEY = "EnginePower";

    //TODO enabled key for drawing when I want to do this

    /**
     * mirror constructor
     */
    protected Engine(ComponentDefBase componentDef, DataInputStream inputStream, Ship ship) {
        super(componentDef, ship);
    }

    /**
     * original constructor
     */
    protected Engine(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new EngineModel(shipModel, componentDef);
    }

    public class EngineModel extends ComponentModel {

        public final IntProperty enginePower = register(new IntProperty(false, true, ENGINE_POWER_KEY));

        public EngineModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            int enginePower = this.enginePower.get();
            enginePower = (enginePower < 0) ? 0 : (enginePower > 100) ? 100 : enginePower;
            //update requested power
            powerRequested.set(enginePower);
            //System.out.println("received power: " + powerReceived.get());
            enginePower = Math.min((int) powerReceived.get(), enginePower);
            applyForce(enginePower / 1000f);
        }

        @Override
        protected boolean attachComponentAt(int x, int y, int side) {
            return y == 1;
        }

        public void applyForce(float strength) {
            final Body body = getShip().getBody();
            Vector2 pos = localToWorld(new Vector2(ShipDef.UNIT_SIZE / 2f, 0));
            Vector2 force = body.getWorldVector(new Vector2(0, strength).rotate(90 * getComponentDef().getRotation()));
            body.applyForce(force, pos, true);
        }
    }
}
