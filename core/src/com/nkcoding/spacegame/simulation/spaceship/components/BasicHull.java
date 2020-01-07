package com.nkcoding.spacegame.simulation.spaceship.components;

import com.nkcoding.spacegame.simulation.Ship;

//test implementation
public class BasicHull extends Component {

    /**
     * mirror constructor
     */
    public BasicHull(ComponentDefBase componentDef, Ship ship) {
        super(componentDef, ship);
    }

    /**
     * original constructor
     */
    protected BasicHull(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }
}
