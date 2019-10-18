package com.nkcoding.spacegame.components;

import com.nkcoding.spacegame.spaceship.*;

public class ExplosiveCanister extends Component {
    public static final String ExplodeKey = "Explode";

    public final BooleanProperty explode = register(new BooleanProperty(false, true, ExplodeKey) {
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
