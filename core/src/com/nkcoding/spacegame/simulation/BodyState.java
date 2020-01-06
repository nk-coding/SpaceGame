package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.Serializable;

public class BodyState implements Serializable {
    public final float angle;
    public final float angularVelocity;
    public final Vector2 position;
    public final Vector2 linearVelocity;

    public BodyState(Vector2 position, float angle, Vector2 linearVelocity, float angularVelocity) {
        this.angle = angle;
        this.angularVelocity = angularVelocity;
        this.position = position;
        this.linearVelocity = linearVelocity;
    }

    public BodyState(Vector2 position, float angle) {
        this(position, angle, new Vector2(), 0);
    }

    public BodyState(Body body) {
        this(body.getPosition(), body.getAngle(), body.getLinearVelocity(), body.getAngularVelocity());
    }

    @Override
    public String toString() {
        return String.format("%s, %f | %s, %f", position, angle, linearVelocity, angularVelocity);
    }
}
