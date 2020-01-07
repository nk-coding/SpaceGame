package com.nkcoding.spacegame.simulation.spaceship.components;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;

public class PowerCore extends Component {

    /**
     * mirror constructor
     */
    protected PowerCore(ComponentDefBase defBase, Ship ship) {
        super(defBase, ship);
    }

    /**
     * original constructor
     */
    protected PowerCore(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new PowerCoreModel(shipModel, componentDef);
    }

    public class PowerCoreModel extends ComponentModel {

        public PowerCoreModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef);
            powerRequested.set(-100);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            //TODO explodes when too much power is not used
        }

        @Override
        protected void destroy() {
            super.destroy();
            spawnExplosion(ShipDef.UNIT_SIZE * 3, 400, 1);
        }
    }
}
