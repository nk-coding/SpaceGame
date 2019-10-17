package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.SpaceSimulation;

public class Explosion extends Simulated {
    //the radius of this explosion
    private float startRadius, endRadius;

    private Fixture sensorFixture;

    protected Explosion(SpaceSimulation spaceSimulation, float startRadius, float endRadius,
                        Vector2 pos, Vector2 linearVelocity) {
        super(spaceSimulation, BodyDef.BodyType.KinematicBody, 2);
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        Body body = getBody();
        CircleShape shape = new CircleShape();
        shape.setRadius(startRadius);
        sensorFixture = body.createFixture(shape, 1);
        sensorFixture.setSensor(true);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        //TODO implementation
    }

    @Override
    public void beginContact(Simulated other, Fixture f1, Fixture f2) {
        super.beginContact(other, f1, f2);
    }
}
