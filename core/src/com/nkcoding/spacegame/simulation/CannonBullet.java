package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

/**
 * represents a bullet of this cannonS
 */
public class CannonBullet extends Simulated {

    //the amount of time this bullet exists
    private float movingSince = 0f;

    //the texture
    private Texture texture;

    //the length
    private float length;

    private boolean collided = false;

    public CannonBullet(SpaceSimulation spaceSimulation, Vector2 pos, float angle, float length, Vector2 velocity) {
        super(SimulatedType.CannonBullet, spaceSimulation, BodyDef.BodyType.KinematicBody, 2, spaceSimulation.getClientID());
        final Body body = getBody();
        body.setBullet(true);
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(new Vector2(0, 0), new Vector2(0, length));
        Fixture fixture = body.createFixture(edgeShape, 0f);
        fixture.setSensor(true);
        body.setTransform(pos, angle);
        body.setLinearVelocity(velocity);
        this.texture = getSpaceSimulation().getAssetManager().getTexture(Asset.Bullet);
        this.length = length;
        this.centerPosition = new Vector2(0, length / 2);
        this.radius = length / 2;
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
                1, 1,
                getBody().getAngle() * MathUtils.radiansToDegrees,
                0, 0,
                this.texture.getWidth(), this.texture.getHeight(),
                false, false);
    }

    @Override
    public void beginContact(Simulated other, Fixture f1, Fixture f2) {
        Object userData = f2.getUserData();
        if (userData instanceof Component && !collided) {
            collided = ((Component) userData).damageAt(f2, 100);
        }
    }
}