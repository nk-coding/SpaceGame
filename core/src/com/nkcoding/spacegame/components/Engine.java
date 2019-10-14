package com.nkcoding.spacegame.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.nkcoding.spacegame.spaceship.*;

public class Engine extends Component {
    public static final String EnginePowerKey = "EnginePower";

    public final IntProperty enginePower = register(new IntProperty(false, true, EnginePowerKey));

    public Engine(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        int enginePower = this.enginePower.get();
        enginePower = (enginePower < 0) ? 0 : (enginePower > 100) ? 100 : enginePower;
        //update requested power
        powerRequested.set(enginePower);
        //System.out.println("received power: " + powerReceived.get());
        enginePower = Math.min((int)powerReceived.get(), enginePower);
        applyForce(enginePower / 100f);
    }

    @Override
    protected boolean attachComponentAt(int x, int y, int side) {
        if (y != 1) {
            System.out.printf("x:%d, y:%d, side:%d%n", x,y,side);
        }
        return y == 1;
    }

    public void applyForce(float strength){
        final Body body = getShip().getBody();
        Vector2 pos = localToWorld(new Vector2(ShipDef.UNIT_SIZE / 2f, 0));
        Vector2 force = body.getWorldVector(new Vector2(0, strength).rotate(90 * getComponentDef().getRotation()));
        body.applyForce(force, pos, true);
    }
}
