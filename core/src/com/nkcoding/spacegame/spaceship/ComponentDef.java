package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.spacegame.Asset;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

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

        //the start health
        //this should never be a negative value or zero
        //the component will be destroyed if health reaches zero
        public final int health = 100;

        //constructor to create a new instance
        public final BiFunction<ComponentDef, Ship, ? extends Component> constructor;

        //file position of the preview image
        public final Asset previewImg;

        private ComponentInfo(ComponentType type, BiFunction<ComponentDef, Ship, ? extends Component> constructor, int width, int height, int health, Asset previewImg) {
            this.type = type;
            this.constructor = constructor;
            this.previewImg = previewImg;
            this.width = width;
            this.height = height;
        }

        //sets width and height to 1
        private ComponentInfo(ComponentType type, BiFunction<ComponentDef, Ship, ? extends Component> constructor, Asset previewImg) {
            this(type, constructor, 1, 1, 100, previewImg);
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

    public int getHealth(){
        return componentInfo.health;
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
    public Asset getPreviewImage() {
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
    public final LinkedHashMap<String, ExternalPropertyData> properties = new LinkedHashMap<>();

    static {
        componentInfos = Map.of(
                ComponentType.TestType, new ComponentInfo(ComponentType.TestType, TestImp::new, 2, 1, 100, Asset.Badlogic),
                ComponentType.BasicHull, new ComponentInfo(ComponentType.BasicHull, TestImp::new, Asset.BasicHull));
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

    public Component createComponent(Ship ship) {
        return componentInfo.constructor.apply(this, ship);
    }

    public void initExternalProperty (ExternalProperty property) {
        ExternalPropertyData data = properties.get(property.name);
        property.setInitValue(data.initData);
        //TODO implementation of changedMethodStatement probably with the SpaceSimulation's list of methods
    }

}
