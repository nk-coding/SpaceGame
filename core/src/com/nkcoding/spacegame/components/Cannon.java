package com.nkcoding.spacegame.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.nkcoding.spacegame.Asset;
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
            lastFired = 0;
            CannonBullet bullet = new CannonBullet(getShip().getSpaceSimulation(),
                    localToWorld(new Vector2(0.5f * ShipDef.UNIT_SIZE, 2f * ShipDef.UNIT_SIZE)),
                    getShip().getRotation() + getComponentDef().getRotation() * 90 * MathUtils.degreesToRadians,
                    0.1f, 3);
            getShip().getSpaceSimulation().addSimulated(bullet);
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

        //the texture
        private Texture texture;

        //the length
        private float length;

        protected CannonBullet(SpaceSimulation spaceSimulation, Vector2 pos, float angle, float length, float velocity) {
            super(spaceSimulation, BodyDef.BodyType.KinematicBody);
            final Body body = getBody();
            body.setBullet(true);
            EdgeShape edgeShape = new EdgeShape();
            edgeShape.set(new Vector2(0,0), new Vector2(0, length));
            body.createFixture(edgeShape, 0f);
            body.setTransform(pos, angle);
            body.setLinearVelocity(new Vector2(0, velocity).rotateRad(angle));
            this.texture = getSpaceSimulation().getAssetManager().getTexture(Asset.Bullet);
            this.length = length;
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

        @Override
        public void draw(Batch batch) {
            Vector2 drawPos = getBody().getPosition();
            batch.draw(this.texture,
                    drawPos.x, drawPos.y,
                    0, 0,
                    0.02f, length,
                    1,1,
                    getBody().getAngle() * MathUtils.radiansToDegrees,
                    0,0,
                    this.texture.getWidth(), this.texture.getHeight(),
                    false, false);
        }
    }
}
