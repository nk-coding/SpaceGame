package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.SpaceSimulation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Explosion extends Simulated {
    //the radius of this explosion
    private float startRadius, endRadius, currentRadius;

    //how much should it grow each step
    private float step;

    //the start damage
    private float damage;

    private float time;

    private Texture explosionTexture;

    private Fixture sensorFixture;

    public Explosion(SpaceSimulation spaceSimulation, float startRadius, float endRadius, float time,
                     Vector2 pos, Vector2 linearVelocity, float damage) {
        this(spaceSimulation, startRadius, endRadius, time, pos, linearVelocity, damage, spaceSimulation.getClientID(), spaceSimulation.getNewId());
    }

    private Explosion(SpaceSimulation spaceSimulation, float startRadius, float endRadius, float time,
                      Vector2 pos, Vector2 linearVelocity, float damage, short owner, int id) {
        super(SimulatedType.Explosion, spaceSimulation, BodyDef.BodyType.KinematicBody, 3, owner, id);
        setSyncPriority(SynchronizationPriority.LOW);

        this.startRadius = startRadius;
        this.currentRadius = startRadius;
        this.endRadius = endRadius;
        this.step = (endRadius - startRadius) / time;
        this.damage = damage;
        this.explosionTexture = getSpaceSimulation().getAssetManager().getTexture(Asset.VerySimpleExplosion);
        Body body = getBody();
        body.setTransform(pos, 0f);
        body.setLinearVelocity(linearVelocity);
        CircleShape shape = new CircleShape();
        shape.setRadius(startRadius);
        sensorFixture = body.createFixture(shape, 1);
        sensorFixture.setSensor(true);
        this.centerPosition = new Vector2(0, 0);
        this.radius = endRadius;
        this.time = time;
    }

    private Explosion(SpaceSimulation spaceSimulation, DataInputStream inputStream) throws IOException {
        super(SimulatedType.Explosion, spaceSimulation, BodyDef.BodyType.KinematicBody, 3, inputStream);

        this.startRadius = inputStream.readFloat();
        this.currentRadius = startRadius;
        this.endRadius = inputStream.readFloat();
        this.time = inputStream.readFloat();
        this.step = (endRadius - startRadius) / time;
        this.damage = inputStream.readFloat();
        this.explosionTexture = getSpaceSimulation().getAssetManager().getTexture(Asset.VerySimpleExplosion);

        CircleShape shape = new CircleShape();
        shape.setRadius(startRadius);
        sensorFixture = body.createFixture(shape, 1);
        sensorFixture.setSensor(true);
        this.centerPosition = new Vector2(0, 0);
        this.radius = endRadius;
    }

    /**
     * constructor only for mirror instance
     */
    public static Explosion deserialize(SpaceSimulation spaceSimulation, DataInputStream inputStream) throws IOException{
        return new Explosion(spaceSimulation, inputStream);
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeFloat(startRadius);
        outputStream.writeFloat(endRadius);
        outputStream.writeFloat(time);
        outputStream.writeFloat(damage);
        serializeBodyState(outputStream);
    }

    @Override
    public void act(float delta) {
        if (currentRadius >= endRadius) {
            //remove only if it is the owner
            //maybe change this later
            if (isOriginal()) {
                getSpaceSimulation().removeSimulated(this);
            }
        } else {
            currentRadius += step * delta;
            sensorFixture.getShape().setRadius(currentRadius);
        }
    }

    @Override
    public void draw(Batch batch) {
        Vector2 pos = getBody().getPosition().sub(currentRadius, currentRadius);
        batch.draw(explosionTexture, pos.x, pos.y, currentRadius * 2, currentRadius * 2);
    }

    @Override
    public void beginContact(Simulated other, Fixture f1, Fixture f2) {
        super.beginContact(other, f1, f2);
        Object userData = f2.getUserData();
        if (userData instanceof Damageable) {
            ((Damageable) userData).damageAt(f2, (int) (damage * (1 - ((currentRadius - startRadius) / (endRadius - startRadius)))));
        }
    }
}
