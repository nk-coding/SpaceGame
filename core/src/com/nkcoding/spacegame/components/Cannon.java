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
    protected final BooleanProperty isShootingProperty = register(new BooleanProperty(false, true, IsShoothingKey));

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
            float angle = getShip().getRotation() + getComponentDef().getRotation() * 90 * MathUtils.degreesToRadians;
            CannonBullet bullet = new CannonBullet(getSpaceSimulation(),
                    localToWorld(new Vector2(0.5f * ShipDef.UNIT_SIZE, 2.3f * ShipDef.UNIT_SIZE)),
                    angle,
                    0.1f,
                    getShip().getBody().getLinearVelocity().add(new Vector2(0, 2).rotateRad(angle)));
            getSpaceSimulation().addSimulated(bullet);
        }
    }

    @Override
    protected boolean attachComponentAt(int x, int y, int side) {
        //don't attach at the top
        return y == 0;
    }

    /**
     * represents a bullet of this cannonS
     */
    public static class CannonBullet extends Simulated {

        //the amount of time this bullet exists
        private float movingSince = 0f;

        //the texture
        private Texture texture;

        //the length
        private float length;

        private boolean collided = false;

        protected CannonBullet(SpaceSimulation spaceSimulation, Vector2 pos, float angle, float length, Vector2 velocity) {
            super(spaceSimulation, BodyDef.BodyType.KinematicBody, 2);
            final Body body = getBody();
            body.setBullet(true);
            EdgeShape edgeShape = new EdgeShape();
            edgeShape.set(new Vector2(0,0), new Vector2(0, length));
            Fixture fixture = body.createFixture(edgeShape, 0f);
            fixture.setSensor(true);
            body.setTransform(pos, angle);
            body.setLinearVelocity(velocity);
            this.texture = getSpaceSimulation().getAssetManager().getTexture(Asset.Bullet);
            this.length = length;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            //limit reach
            movingSince += delta;
            if (movingSince > 10f || collided) {
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

        @Override
        public void beginContact(Simulated other, Fixture f1, Fixture f2) {
            Object userData = f2.getUserData();
            if (userData instanceof Component && !collided) {
                collided = true;
                ((Component)userData).damageAt(f2, 100);
            }
        }
    }
}
