package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.Serializable;

public class BodyState implements Serializable {
    public final float angle;
    public final float angularVelocity;
    public final float positionX;
    public final float positionY;
    public final float linearVelocityX;
    public final float linearVelocityY;
    public final int id;

    public BodyState(Vector2 position, float angle, Vector2 linearVelocity, float angularVelocity, int id) {
        this.angle = angle;
        this.angularVelocity = angularVelocity;
        this.positionX = position.x;
        this.positionY = position.y;
        this.linearVelocityX = linearVelocity.x;
        this.linearVelocityY = linearVelocity.y;
        this.id = id;
    }

    public BodyState(Body body, int id) {
        this(body.getPosition(), body.getAngle(), body.getLinearVelocity(), body.getAngularVelocity(), id);
    }

    public Vector2 position() {
        return new Vector2(positionX, positionY);
    }

    public Vector2 linearVelocity() {
        return new Vector2(linearVelocityX, linearVelocityY);
    }

    @Override
    public String toString() {
        return String.format("%d: (%f, %f), %f | (%f, %f), %f", id, positionX, positionY, angle, linearVelocityX, linearVelocityY, angularVelocity);
    }
}
