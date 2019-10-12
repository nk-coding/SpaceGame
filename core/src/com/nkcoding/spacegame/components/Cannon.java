package com.nkcoding.spacegame.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.spaceship.*;

public class Cannon extends Component {
    public static final String IsShoothingKey = "IsShooting";

    //should the cannon fire?
    public final BooleanProperty isShootingProperty = register(new BooleanProperty(false, true, IsShoothingKey));

    private float lastFired = 0f;


    public Cannon(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        lastFired += delta;
        if (lastFired > 1f && isShootingProperty.get()) {

        }
    }

    @Override
    protected boolean attachComponentAt(int x, int y, int side) {
        //don't attach at the top
        return y == 0;
    }

    //represents a bullet of this cannon
    public static class CannonBullet extends Simulated {

        //the amount of time this bullet exists
        private float movingSince = 0f;

        //the Shape of this bullet
        private EdgeShape shape;

        protected CannonBullet(SpaceSimulation spaceSimulation, Vector2 pos1, Vector2 pos2, float velocity) {
            super(spaceSimulation, BodyDef.BodyType.KinematicBody);
            final Body body = getBody();
            body.setBullet(true);
            EdgeShape edgeShape = new EdgeShape();
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            //limit reach
            movingSince += delta;
            if (movingSince > 10f) {
                getSpaceSimulation().removeSimulated(this);
            }
        }
    }
}
