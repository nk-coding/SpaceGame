package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.simulation.communication.CreateTransmission;
import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;

public class Simulated {
    //the type of this object
    public final BodyDef.BodyType bodyType;
    //the simulation type
    public final SimulatedType type;
    public final int id;
    //the body which is used in Box3D
    protected final Body body;
    private final int syncPriority;
    //is it active for simulation or not
    protected boolean active = true;
    //center position, width and height for camera adjustment
    protected Vector2 centerPosition;
    protected float width;
    protected float height;
    //radius for drawing
    protected float radius;
    //the SpaceSimulation of which this is part of
    private SpaceSimulation spaceSimulation;
    private boolean receivesKeyInput = false;
    private int collisionPriority;
    private int owner;
    private boolean isOwner;

    /**
     * wrapper for the complex constructor
     *
     * @param type              the SimulatedType
     * @param spaceSimulation   the simulation this is for
     * @param bodyType          the BodyType which is used to create the default BodyDef
     * @param collisionPriority a higher priority means that a contact is handled by this instance, but not on this client
     * @param owner             owner id, used to calculate isOwner
     * @param syncPriority      how often is it synced (between 0 and 2)
     */
    protected Simulated(SimulatedType type, SpaceSimulation spaceSimulation, BodyDef.BodyType bodyType, int collisionPriority, int owner, int syncPriority, int id) {
        this(type, spaceSimulation, createBodyDef(bodyType), collisionPriority, owner, syncPriority, id);
    }

    /**
     * constructor to create a Simulated
     *
     * @param type              the SimulatedType
     * @param spaceSimulation   the simulation this is for
     * @param bodyDef           the Definition for the box2D body for extended control
     * @param collisionPriority a higher priority means that a contact is handled by this instance, but not on this client
     * @param owner             owner id, used to calculate isOwner
     * @param syncPriority      how often is it synced (between 0 and 2)
     */
    protected Simulated(SimulatedType type, SpaceSimulation spaceSimulation, BodyDef bodyDef, int collisionPriority, int owner, int syncPriority, int id) {
        this.type = type;
        this.spaceSimulation = spaceSimulation;
        this.bodyType = bodyDef.type;
        this.collisionPriority = collisionPriority;
        this.body = spaceSimulation.getWorld().createBody(bodyDef);
        this.body.setUserData(this);
        this.owner = owner;
        this.isOwner = owner == spaceSimulation.getClientID();
        this.syncPriority = syncPriority;
        this.id = id;
    }

    //helper method to create bodyDef
    private static BodyDef createBodyDef(BodyDef.BodyType bodyType) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(0, 0);
        return bodyDef;
    }

    public SpaceSimulation getSpaceSimulation() {
        return spaceSimulation;
    }

    public boolean getActive() {
        return active;
    }

    public Body getBody() {
        return body;
    }

    public BodyDef.BodyType getBodyType() {
        return bodyType;
    }

    public int getCollisionPriority() {
        return collisionPriority;
    }

    /**
     * does this Simulated receive key input?
     * default is false
     */
    public boolean isReceivesKeyInput() {
        return receivesKeyInput && active;
    }

    /**
     * does this Simulated receive key input?
     * default is false
     */
    public void setReceivesKeyInput(boolean receivesKeyInput) {
        boolean old = this.receivesKeyInput;
        this.receivesKeyInput = receivesKeyInput;
        if (old != receivesKeyInput) {
            spaceSimulation.updateReceivesKeyInput(this);
        }
    }

    /**
     * get the position in box2D
     *
     * @return body.getPosition()
     */
    public Vector2 getPosition() {
        return body.getPosition();
    }

    /**
     * get the rotation in radians
     *
     * @return body.getAngle()
     */
    public float getRotation() {
        return body.getAngle();
    }

    public Vector2 getCenterPosition() {
        return centerPosition;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRadius() {
        return radius;
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
     *
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

    public void setCameraFocus(boolean cameraFocus) {
    }

    /**
     * transforms local coordinates to world coordinates
     *
     * @param local the Vector2 that is modified
     * @return the modified Vector2
     */
    public Vector2 localToWorldCoordinates(Vector2 local) {
        return body.getWorldPoint(local);
    }

    /**
     * transforms world coordinates to local coordinates
     *
     * @param world the Vector2 that is modified
     * @return the modified Vector2
     */
    public Vector2 worldToLocalCoordinates(Vector2 world) {
        return body.getLocalPoint(world);
    }

    /**
     * called when a contact happens
     * Will only be called if it's the client of other's owner
     * must handle own synchronization on its own
     *
     * @param other the simulated the collision happened with
     * @param f1    Fixture 1 of the collision
     * @param f2    Fixture 2 of the collision
     */
    public void beginContact(Simulated other, Fixture f1, Fixture f2) {

    }

    public int getOwner() {
        return owner;
    }

    public boolean isOriginal() {
        return isOwner;
    }

    public int getSyncPriority() {
        return syncPriority;
    }

    /**
     * send a transmission to the original
     *
     * @param transmission the transmission to send
     */
    public void sendToOriginal(UpdateTransmission transmission) {
        if (isOwner) {
            receiveTransmission(transmission);
        } else {
            spaceSimulation.sendTo(transmission, owner);
        }
    }

    /**
     * send a transmission to all mirrors
     * @param transmission the transmission to send
     */
    public void post(UpdateTransmission transmission) {
        receiveTransmission(transmission);
        spaceSimulation.sendToAll(transmission);
    }

    /**
     * subclasses should overwrite this method if they want to receive update transmissions
     *
     * @param transmission the update transmission
     */
    protected void receiveTransmission(UpdateTransmission transmission) {
    }

    /**
     * get the data to construct a mirror
     * should be overwritten by subclasses
     */
    public CreateTransmission getMirrorData() {
        return null;
    }

    /**
     * get the BodyState of this Simulated
     */
    public BodyState getBodyState() {
        return new BodyState(body);
    }
}
