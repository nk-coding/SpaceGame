package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Transform;
import com.nkcoding.communication.ResetDataOutputStream;
import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.simulation.communication.TransmissionID;
import com.nkcoding.spacegame.simulation.communication.UpdateTransmission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Simulated {
    //the type of this object
    public final BodyDef.BodyType bodyType;
    //the simulation type
    public final SimulatedType type;
    public final int id;
    //the body which is used in Box3D
    protected final Body body;
    private int syncPriority = SynchronizationPriority.LOW;
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
    private short owner;
    private boolean isOwner;
    /**
     * the last update on this Simulated (is automatically set)
     */
    private int lastBodyUpdate = -1;

    /**
     * wrapper for the complex constructor
     *
     * @param type              the SimulatedType
     * @param spaceSimulation   the simulation this is for
     * @param bodyType          the BodyType which is used to create the default BodyDef
     * @param collisionPriority a higher priority means that a contact is handled by this instance, but not on this client
     * @param owner             owner id, used to calculate isOwner
     */
    protected Simulated(SimulatedType type, SpaceSimulation spaceSimulation, BodyDef.BodyType bodyType, int collisionPriority, short owner, int id) {
        this(type, spaceSimulation, createBodyDef(bodyType), collisionPriority, owner, id);
    }

    /**
     * constructor to create a Simulated
     *
     * @param type              the SimulatedType
     * @param spaceSimulation   the simulation this is for
     * @param bodyDef           the Definition for the box2D body for extended control
     * @param collisionPriority a higher priority means that a contact is handled by this instance, but not on this client
     * @param owner             owner id, used to calculate isOwner
     */
    protected Simulated(SimulatedType type, SpaceSimulation spaceSimulation, BodyDef bodyDef, int collisionPriority, short owner, int id) {
        this.type = type;
        this.spaceSimulation = spaceSimulation;
        this.bodyType = bodyDef.type;
        this.collisionPriority = collisionPriority;
        this.body = spaceSimulation.getWorld().createBody(bodyDef);
        this.body.setUserData(this);
        this.owner = owner;
        this.isOwner = owner == spaceSimulation.getClientID();
        this.id = id;
    }

    protected Simulated(SimulatedType type, SpaceSimulation spaceSimulation, BodyDef bodyDef, int collisionPriority, DataInputStream inputStream) throws IOException {
        this(type, spaceSimulation, bodyDef, collisionPriority, inputStream.readShort(), inputStream.readInt());
    }

    protected Simulated(SimulatedType type, SpaceSimulation spaceSimulation, BodyDef.BodyType bodyType, int collisionPriority, DataInputStream inputStream) throws IOException {
        this(type, spaceSimulation, bodyType, collisionPriority, inputStream.readShort(), inputStream.readInt());
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
     * @return body.getCenterPosition()
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

    public void setSyncPriority(int syncPriority) {
        this.syncPriority = syncPriority;
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
            ResetDataOutputStream outputStream = spaceSimulation.getOutputStream(true);
            try {
                outputStream.writeInt(TransmissionID.UPDATE);
                transmission.serialize(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            spaceSimulation.sendTo(outputStream, owner);
        }
    }

    /**
     * send a transmission to all mirrors
     * @param transmission the transmission to send
     */
    public void post(UpdateTransmission transmission) {
        receiveTransmission(transmission);
        ResetDataOutputStream outputStream = spaceSimulation.getOutputStream(true);
        try {
            outputStream.writeInt(TransmissionID.UPDATE);
            transmission.serialize(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        spaceSimulation.sendToAll(outputStream);
    }

    /**
     * subclasses should receive update transmissions
     *
     * @param updateTransmission the update transmission
     */
    public void receiveTransmission(UpdateTransmission updateTransmission) {
    }

    /**
     * subclasses should overwrite this if they want to register additional UpdateTransmissions
     */
    public UpdateTransmission deserializeTransmission(DataInputStream inputStream, short updateID) throws IOException {
        return null;
    }

    /**
     * get the data to construct a mirror
     * should be overwritten by subclasses
     */
    public void serialize(DataOutputStream outputStream) throws IOException {
        type.serialize(outputStream);
        outputStream.writeShort(owner);
        outputStream.writeInt(id);
    }

    /**
     * update the Simulated based on the body state
     */
    public void deserializeBodyState(DataInputStream inputStream, int updateID) throws IOException {
        if (this.lastBodyUpdate <= updateID) {
            body.setTransform(inputStream.readFloat(), inputStream.readFloat(), inputStream.readFloat());
            body.setLinearVelocity(inputStream.readFloat(), inputStream.readFloat());
            body.setAngularVelocity(inputStream.readFloat());
            this.lastBodyUpdate = updateID;
        }

    }

    public void serializeBodyState(DataOutputStream outputStream) throws IOException {
        Transform transform = body.getTransform();
        outputStream.writeFloat(transform.getPosition().x);
        outputStream.writeFloat(transform.getPosition().y);
        outputStream.writeFloat(transform.getRotation());
        outputStream.writeFloat(body.getLinearVelocity().x);
        outputStream.writeFloat(body.getLinearVelocity().y);
        outputStream.writeFloat(body.getAngularVelocity());
    }
}
