package com.nkcoding.spacegame.simulation.spaceship.components;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.properties.BooleanProperty;

import java.io.DataInputStream;

public class ExplosiveCanister extends Component {
    public static final String EXPLODE_KEY = "Explode";

    /**
     * mirror constructor
     */
    protected ExplosiveCanister(ComponentDefBase componentDef, DataInputStream inputStream, Ship ship) {
        super(componentDef, ship);
    }

    /**
     * original constructor
     */
    protected ExplosiveCanister(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new ExplosiveCanisterModel(shipModel, componentDef);
    }

    public class ExplosiveCanisterModel extends ComponentModel {

        public final BooleanProperty explode = register(new BooleanProperty(false, true, EXPLODE_KEY) {
            @Override
            public void set(boolean value) {
                super.set(value);
                if (value) {
                    health.set(0);
                }
            }
        });

        public ExplosiveCanisterModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef);
        }

        @Override
        protected void destroy() {
            super.destroy();
            spawnExplosion(ShipDef.UNIT_SIZE * 2, 500, 1);
        }
    }
}
