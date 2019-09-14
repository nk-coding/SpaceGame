package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.compiler.DataTypes;

import java.util.HashMap;

//subclass which contains all the stuff that is necessary to design a ship but not emulate it
public class ComponentDef {

    //region names for the ExternalProperties
    public static final String HealthKey = "Health";
    public static final String PowerRequestedKey = "PowerRequested";
    public static final String RequestLevelKey = "RequestLevel";
    public static final String HasFullPowerKey = "HasFullPower";
    public static final String PowerReceivedKey = "PowerReceived";
    //endregion

    public final String previewTexture = "";

    //the width of the component, it should be set in the constructor
    private int width;

    public int getWidth(){
        return width;
    }

    protected void setWidth(int width){
        this.width = width;
    }

    //the height of the component, it should be set in the constructor
    private int height;

    public int getHeight(){
        return height;
    }

    protected void setHeight(int height){
        this.height = height;
    }

    //0 = not rotated, 1 = 90°, 2 = 180°, 3 = 270°, everything different will be normalised, negative values are not allowed
    private int rotation;

    public int getRotation(){
        return rotation;
    }

    public void setRotation(int rotation){
        if (rotation < 0) throw new IllegalArgumentException();
        this.rotation = rotation % 4;
    }

    //the position, this is necessary for a ship to locate the component
    private int x;

    public int getX(){
        return x;
    }

    public void setX(int x){
        this.x = x;
    }

    //the position, this is necessary for a ship to locate the component
    private int y;

    public int getY(){
        return y;
    }

    public void setY(int y){
        this.y = y;
    }

    //start health can be set by a subclass, but is only mutable in Component
    //this should never be a negative value or zero
    //the component will be destroyed if health reaches zero
    protected int health = 100;

    public int getHealth(){
        return health;
    }

    //function to get width, includes rotation
    public int getRealWidth(){
        return ((rotation % 2) == 0) ? width : height;
    }

    //function to get height, includes rotation
    public int getRealHeight(){
        return ((rotation % 2) == 0) ? height : width;
    }

    //type
    private final ComponentType type;

    public ComponentType getType(){
        return type;
    }

    //name for the component
    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**HashMap with all the ExternalPropertyData*/
    public final HashMap<String, ExternalPropertyData> properties = new HashMap<>();

    /**
     * default constructor
     * subclasses have to add all <code>ExternalPropertyData</code>
     * @param type the type of the ComponentDefinition
     */
    public ComponentDef(ComponentType type){
        this.type = type;
        //add all ExternalPropertyDefs
        properties.put(HealthKey, new ExternalPropertyData(DataTypes.Float));
        properties.put(PowerRequestedKey, new ExternalPropertyData(DataTypes.Float));
        properties.put(RequestLevelKey, new ExternalPropertyData(DataTypes.Integer));
        properties.put(HasFullPowerKey, new ExternalPropertyData(DataTypes.Boolean));
        properties.put(PowerReceivedKey, new ExternalPropertyData(DataTypes.Float));
    }

    public void initExternalProperty (ExternalProperty property) {
        ExternalPropertyData data = properties.get(property.name);
        property.setInitValue(data.initData);
        //TODO implementation of changedMethodStatement probably with the SpaceSimulation's list of methods
    }

}
