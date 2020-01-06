package com.nkcoding.spacegame.simulation.spaceship.components;

import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.properties.BooleanProperty;

public class ExplosiveCanister extends Component {
    public static final String EXPLODE_KEY = "Explode";

    public final BooleanProperty explode = register(new BooleanProperty(false, true, EXPLODE_KEY) {
        @Override
        public void set(boolean value) {
            super.set(value);
            if (value) {
                health.set(0);
            }
        }
    });

    public ExplosiveCanister(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship);
    }

    @Override
    protected void destroy() {
        super.destroy();
        spawnExplosion(ShipDef.UNIT_SIZE * 2, 500, 1);
    }
}
