package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.Serializable;

public class BodyState implements Serializable {
    public final float angle;
    public final float angularVelocity;
    public final Vector2 position;
    public final Vector2 linearVelocity;
    public final int id;

    public BodyState(Vector2 position, float angle, Vector2 linearVelocity, float angularVelocity, int id) {
        this.angle = angle;
        this.angularVelocity = angularVelocity;
        this.position = position;
        this.linearVelocity = linearVelocity;
        this.id = id;
    }

    public BodyState(Body body, int id) {
        this(body.getPosition(), body.getAngle(), body.getLinearVelocity(), body.getAngularVelocity(), id);
    }

    @Override
    public String toString() {
        return String.format("%d: %s, %f | %s, %f", id, position, angle, linearVelocity, angularVelocity);
    }
}
