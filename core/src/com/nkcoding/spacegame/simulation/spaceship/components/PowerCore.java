package com.nkcoding.spacegame.simulation.spaceship.components;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;

public class PowerCore extends Component {
    /**
     * default constructor
     */
    public PowerCore(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship);
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
