package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Engine extends Component {
    public static final String EnginePowerKey = "EnginePower";

    public final IntProperty enginePower = register(new IntProperty(false, true, EnginePowerKey));

    public Engine(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        //TODO implement power system usage
        int enginePower = this.enginePower.get();
        enginePower = (enginePower < 0) ? 0 : (enginePower > 100) ? 100 : enginePower;
        applyForce(enginePower / 100f);
    }

    public void applyForce(float strength){
        final Body body = getShip().getBody();
        Vector2 pos = localToWorld(new Vector2(ShipDef.UNIT_SIZE / 2f, 0));
        body.applyForce(body.getWorldVector(new Vector2(0, strength)), localToWorld(pos), true);
    }
}
