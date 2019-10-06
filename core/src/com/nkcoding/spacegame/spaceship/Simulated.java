package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.nkcoding.spacegame.SpaceSimulation;

public class Simulated {
    //the SpaceSimulation of which this is part of
    private SpaceSimulation spaceSimulation;

    public SpaceSimulation getSpaceSimulation() {
        return spaceSimulation;
    }

    //is it active for simulation or not
    protected boolean active = true;

    public boolean getActive() {
        return active;
    }

    //the body which is used in Box3D
    protected Body body;

    public Body getBody() {
        return body;
    }

    private boolean receivesKeyInput = false;

    /**
     * does this Simulated receive key input?
     * default is false
     * */
    public boolean isReceivesKeyInput() {
        return receivesKeyInput && active;
    }

    /**
     * does this Simulated receive key input?
     * default is false
     * */
    public void setReceivesKeyInput(boolean receivesKeyInput) {
        boolean old = this.receivesKeyInput;
        this.receivesKeyInput = receivesKeyInput;
        if (old != receivesKeyInput) {
            spaceSimulation.updateReceivesKeyInput(this);
        }
    }

    //used to not-recalculate sin and cos multiple times
    boolean sinCosCalculated = false;
    float sin, cos;

    /**
     * get the position in box2D
     * @return body.getPosition()
     */
    public Vector2 getPosition() {
        return body.getPosition();
    }

    /**
     * get the rotation in radians
     * @return body.getAngle()
     */
    public float getRotation() {
        return body.getAngle();
    }

    //the default constructor
    protected Simulated(SpaceSimulation spaceSimulation) {
        this.spaceSimulation = spaceSimulation;
    }

    /**
     * the default act method
     * a subclass must call this, otherwise positioning will not work
     * @param delta
     */
    public void act(float delta) {
        sinCosCalculated = false;
    }

    /**
     * the default draw method
     * a subclass must overwrite this, it it wants to be drawn
     * @param batch the Batch to draw on
     */
    public void draw(Batch batch) {

    }

    public boolean keyDown(int keycode) {
        return false;
    }

    public boolean keyUp(int keycode) {
        return false;
    }

    /**
     * updates sin and cos
     */
    protected void updateSinCos() {
        sinCosCalculated = true;
        sin = (float)Math.cos(getRotation());
        cos = (float)Math.sin(getRotation());
    }

    /**
     * transforms local coordinates to world coordinates
     * @param localCoordinates the Vector2 that is modified
     * @return the modified Vector2
     */
    public Vector2 localToWorldCoordinates(Vector2 localCoordinates) {
        final float rotation = getRotation();
        final Vector2 position = getPosition();
        final float x = position.x;
        final float y = position.y;
        if (rotation == 0) {
            localCoordinates.x += x;
            localCoordinates.y += y;
        }
        else {
            final float toX = localCoordinates.x;
            final float toY = localCoordinates.y;
            localCoordinates.x = x + (toX * cos + toY * sin);
            localCoordinates.y = y + (toX * -sin + toY * cos);
        }

        return localCoordinates;
    }

    /**
     * transforms world coordinates to local coordinates
     * @param worldCoordinates the Vector2 that is modified
     * @return the modified Vector2
     */
    public Vector2 worldToLocalCoordinates(Vector2 worldCoordinates) {
        final float rotation = getRotation();
        final Vector2 position = getPosition();
        final float x = position.x;
        final float y = position.y;
        if (rotation == 0) {
            worldCoordinates.x -= x;
            worldCoordinates.y -= y;
        }
        else {
            final float toX = worldCoordinates.x - x;
            final float toY = worldCoordinates.y - y;
            worldCoordinates.x = (toX * cos + toY * sin);
            worldCoordinates.y = (toX * -sin + toY * cos);
        }

        return worldCoordinates;
    }


}
