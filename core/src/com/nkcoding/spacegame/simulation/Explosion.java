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
import com.nkcoding.spacegame.simulation.communication.CreateTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;

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
                      Vector2 pos, Vector2 linearVelocity, float damage, int owner, int id) {
        super(SimulatedType.Explosion, spaceSimulation, BodyDef.BodyType.KinematicBody, 3, owner, SynchronizationPriority.LOW, id);
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

    /**
     * constructor only for mirror instance
     */
    public static Explosion mirror(SpaceSimulation spaceSimulation, CreateTransmission transmission) {
        ExplosionCreateTransmission createTransmission = (ExplosionCreateTransmission) transmission;
        return new Explosion(spaceSimulation, createTransmission.startRadius, createTransmission.endRadius, createTransmission.time, createTransmission.bodyState.position,
                createTransmission.bodyState.linearVelocity, createTransmission.damage, createTransmission.owner, createTransmission.simulatedID);
    }

    @Override
    public CreateTransmission getMirrorData() {
        return new ExplosionCreateTransmission(id, getOwner(), getBodyState(), startRadius, endRadius, time, damage);
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
        if (userData instanceof Component) {
            ((Component) userData).damageAt(f2, (int) (damage * (1 - ((currentRadius - startRadius) / (endRadius - startRadius)))));
        }
    }

    private static class ExplosionCreateTransmission extends CreateTransmission {
        public final float startRadius;
        public final float endRadius;
        public final float time;
        public final float damage;

        public ExplosionCreateTransmission(int simulatedID, int owner, BodyState bodyState, float startRadius, float endRadius, float time, float damage) {
            super(SimulatedType.Explosion, simulatedID, owner, bodyState);
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.time = time;
            this.damage = damage;
        }
    }
}
