package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.SpaceSimulation;

public class Explosion extends Simulated {
    //the radius of this explosion
    private float startRadius, endRadius, currentRadius;

    //how much should it grow each step
    private float step;

    //the start damage
    private float damage;

    private Texture explosionTexture;

    private Fixture sensorFixture;


    protected Explosion(SpaceSimulation spaceSimulation, float startRadius, float endRadius, float time,
                        Vector2 pos, Vector2 linearVelocity, float damage) {
        super(spaceSimulation, BodyDef.BodyType.KinematicBody, 2);
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
        this.centerPosition = pos;
        this.radius = endRadius;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (currentRadius >= endRadius) {
            getSpaceSimulation().removeSimulated(this);
        } else {
            currentRadius += step * delta;
            sensorFixture.getShape().setRadius(currentRadius);
        }
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
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
}
