package com.nkcoding.spacegame.components;

import com.nkcoding.spacegame.spaceship.Component;
import com.nkcoding.spacegame.spaceship.ComponentDef;
import com.nkcoding.spacegame.spaceship.Ship;

public class PowerCore extends Component {
    /**
     * default constructor
     */
    public PowerCore(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship);
        powerRequested.set(-200);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
