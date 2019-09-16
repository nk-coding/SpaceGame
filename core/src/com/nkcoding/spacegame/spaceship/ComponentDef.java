package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.compiler.DataTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//subclass which contains all the stuff that is necessary to design a ship but not emulate it
public class ComponentDef {

    //class which contains some basic information about a specific component
    public static final class ComponentInfo {
        //the type of this component
        public final ComponentType type;

        //the width of the component
        public final int width;

        //the height of the component
        public final int height;

        //file position of the preview image
        public final String previewImg;

        private ComponentInfo(ComponentType type, String previewImg, int width, int height) {
            this.type = type;
            this.previewImg = previewImg;
            this.width = width;
            this.height = height;
        }

        //sets width and height to 1
        private ComponentInfo(ComponentType type, String previewImg) {
            this(type, previewImg, 1, 1);
        }
    }

    public static final Map<ComponentType, ComponentInfo> componentInfos;

    //region names for the ExternalProperties
    public static final String HealthKey = "Health";
    public static final String PowerRequestedKey = "PowerRequested";
    public static final String RequestLevelKey = "RequestLevel";
    public static final String HasFullPowerKey = "HasFullPower";
    public static final String PowerReceivedKey = "PowerReceived";
    //endregion

    public int getWidth(){
        return componentInfo.width;
    }

    public int getHeight(){
        return componentInfo.height;
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
        return ((rotation % 2) == 0) ? componentInfo.width : componentInfo.height;
    }

    //function to get height, includes rotation
    public int getRealHeight(){
        return ((rotation % 2) == 0) ? componentInfo.height : componentInfo.width;
    }

    //ComponentInfo with all necessary information
    private final ComponentInfo componentInfo;

    //get the type
    public ComponentType getType(){
        return componentInfo.type;
    }

    //get the preview image file
    public String getPreviewImage() {
        return componentInfo.previewImg;
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

    static {
        HashMap<ComponentType, ComponentInfo> infos = new HashMap<>();
        infos.put(ComponentType.TestType, new ComponentInfo(ComponentType.TestType, "badlogic.jpg", 2, 1));
        componentInfos = Collections.unmodifiableMap(infos);
    }

    /**
     * default constructor
     * subclasses have to add all <code>ExternalPropertyData</code>
     * @param type the type of the ComponentDef
     */
    public ComponentDef(ComponentType type){
        this(componentInfos.get(type));
    }

    /**
     * constructor with a ComponentInfo instead of a ComponentType
     * @param info contains the type of the ComponentDef
     */
    public ComponentDef(ComponentInfo info) {
        this.componentInfo = info;
        //add all ExternalPropertyDefs
        properties.put(HealthKey, new ExternalPropertyData(DataTypes.Float));
        properties.put(PowerRequestedKey, new ExternalPropertyData(DataTypes.Float));
        properties.put(RequestLevelKey, new ExternalPropertyData(DataTypes.Integer));
        properties.put(HasFullPowerKey, new ExternalPropertyData(DataTypes.Boolean));
        properties.put(PowerReceivedKey, new ExternalPropertyData(DataTypes.Float));
        //TODO add all type specific properties
    }

    public void initExternalProperty (ExternalProperty property) {
        ExternalPropertyData data = properties.get(property.name);
        property.setInitValue(data.initData);
        //TODO implementation of changedMethodStatement probably with the SpaceSimulation's list of methods
    }





}
