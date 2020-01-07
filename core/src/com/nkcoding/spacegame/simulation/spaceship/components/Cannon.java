package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.nkcoding.spacegame.simulation.CannonBullet;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.properties.BooleanProperty;


public class Cannon extends Buffer {
    public static final String IS_SHOOTING_KEY = "IsShooting";

    /**
     * mirror constructor
     */
    protected Cannon(ComponentDefBase defBase, Ship ship) {
        super(defBase, ship);
    }

    /**
     * original constructor
     */
    protected Cannon(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new CannonModel(shipModel, componentDef);
    }

    public class CannonModel extends BufferModel {

        //should the cannon fire?
        protected final BooleanProperty isShootingProperty = register(new BooleanProperty(false, true, IS_SHOOTING_KEY));

        public CannonModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef, 50, 50);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (isCharged() && isShootingProperty.get()) {
                bufferLevel.set(0f);
                float angle = getShip().getRotation() + getComponentDef().getRotation() * 90 * MathUtils.degreesToRadians;
                CannonBullet bullet = new CannonBullet(getSpaceSimulation(),
                        localToWorld(new Vector2(0.5f * ShipDef.UNIT_SIZE, 2.3f * ShipDef.UNIT_SIZE)),
                        angle,
                        0.1f,
                        getShip().getBody().getLinearVelocity().add(new Vector2(0, 2).rotateRad(angle)));
                getSpaceSimulation().addSimulated(bullet);
            }
        }

        @Override
        protected boolean attachComponentAt(int x, int y, int side) {
            //don't attach at the top
            return y == 0;
        }
    }

}
