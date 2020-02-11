package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.simulation.Ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ComponentDefBase {
    //ComponentInfo with all necessary information
    protected final ComponentType componentType;
    //0 = not rotated, 1 = 90°, 2 = 180°, 3 = 270°, everything different will be normalised, negative values are not allowed
    protected int rotation;
    //the position, this is necessary for a ship to locate the component
    protected int x;
    //the position, this is necessary for a ship to locate the component
    protected int y;

    /**
     * constructor with a ComponentInfo instead of a ComponentType
     *
     * @param type contains the type of the ComponentDef
     */
    public ComponentDefBase(ComponentType type) {
        this.componentType = type;
    }

    public ComponentDefBase(ComponentDefBase toCopy) {
        this.componentType = toCopy.componentType;
        this.rotation = toCopy.rotation;
        this.x = toCopy.x;
        this.y = toCopy.y;
    }

    public int getWidth() {
        return componentType.width;
    }

    public int getHeight() {
        return componentType.height;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        if (rotation < 0) throw new IllegalArgumentException();
        this.rotation = rotation % 4;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHealth() {
        return componentType.health;
    }

    //function to get width, includes rotation
    public int getRealWidth() {
        return ((rotation % 2) == 0) ? componentType.width : componentType.height;
    }

    //function to get height, includes rotation
    public int getRealHeight() {
        return ((rotation % 2) == 0) ? componentType.height : componentType.width;
    }

    //get the type
    public ComponentType getType() {
        return componentType;
    }

    //get the preview image file
    public Asset getDefaultTexture() {
        return componentType.defaultTexture;
    }

    /**
     * helper method to generate the shape
     *
     * @param posX the x pos for the shape
     * @param posY the y pos for the shape
     * @return the shape
     */
    public PolygonShape getShape(int posX, int posY) {
        return componentType.getShape(rotation % 2 == 1, posX, posY);
    }

    public Component deserializeComponent(Ship ship, DataInputStream inputStream) throws IOException{
        return componentType.deserializer.apply(this, inputStream, ship);
    }

    public void serialize(DataOutputStream outputStream) throws IOException {
        componentType.serialize(outputStream);
        outputStream.writeInt(x);
        outputStream.writeInt(y);
        outputStream.writeInt(rotation);
    }

    public static ComponentDefBase deserialize(DataInputStream inputStream) throws IOException {
        ComponentDefBase defBase = new ComponentDefBase(ComponentType.deserialize(inputStream));
        defBase.setX(inputStream.readInt());
        defBase.setY(inputStream.readInt());
        defBase.setRotation(inputStream.readInt());
        return defBase;
    }
}
