package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
    protected final Body body;

    public Body getBody() {
        return body;
    }

    //the type of this object
    public final BodyDef.BodyType bodyType;

    public BodyDef.BodyType getBodyType() {
        return bodyType;
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

    //center position, width and height for camera adjustment
    protected Vector2 centerPosition;

    public Vector2 getCenterPosition() {
        return  centerPosition;
    }

    protected float width;

    public float getWidth() {
        return width;
    }

    protected float height;

    public float getHeight(){
        return height;
    }

    //the default constructor
    protected Simulated(SpaceSimulation spaceSimulation, BodyDef.BodyType bodyType) {
        this.spaceSimulation = spaceSimulation;
        this.bodyType = bodyType;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(0, 0);
        this.body = spaceSimulation.getWorld().createBody(bodyDef);
        this.body.setUserData(this);
    }

    //the bigger constructor
    protected Simulated(SpaceSimulation spaceSimulation, BodyDef bodyDef) {
        this.spaceSimulation = spaceSimulation;
        this.bodyType = bodyDef.type;
        this.body = spaceSimulation.getWorld().createBody(bodyDef);
        this.body.setUserData(this);
    }

    /**
     * the default act method
     * a subclass must call this, otherwise positioning will not work
     */
    public void act(float delta) {

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

    public void setCameraFocus(boolean cameraFocus) {}

    /**
     * transforms local coordinates to world coordinates
     * @param local the Vector2 that is modified
     * @return the modified Vector2
     */
    public Vector2 localToWorldCoordinates(Vector2 local) {
        return body.getWorldPoint(local);
    }

    /**
     * transforms world coordinates to local coordinates
     * @param world the Vector2 that is modified
     * @return the modified Vector2
     */
    public Vector2 worldToLocalCoordinates(Vector2 world) {
        return body.getLocalPoint(world);
    }


}
