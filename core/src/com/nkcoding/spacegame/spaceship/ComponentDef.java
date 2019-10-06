package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.spacegame.Asset;

import java.util.LinkedHashMap;
import java.util.Map;

//subclass which contains all the stuff that is necessary to design a ship but not emulate it
public class ComponentDef {

    public int getWidth(){
        return componentType.width;
    }

    public int getHeight(){
        return componentType.height;
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
        return componentType.health;
    }

    //function to get width, includes rotation
    public int getRealWidth(){
        return ((rotation % 2) == 0) ? componentType.width : componentType.height;
    }

    //function to get height, includes rotation
    public int getRealHeight(){
        return ((rotation % 2) == 0) ? componentType.height : componentType.width;
    }

    //ComponentInfo with all necessary information
    private final ComponentType componentType;

    //get the type
    public ComponentType getType(){
        return componentType;
    }

    //get the preview image file
    public Asset getPreviewImage() {
        return componentType.previewImg;
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

    /**
     * constructor with a ComponentInfo instead of a ComponentType
     * @param type contains the type of the ComponentDef
     */
    public ComponentDef(ComponentType type) {
        this.componentType = type;
        //add all ExternalPropertyDefs
        for (ExternalPropertyData data : type.propertyDefs) {
            properties.put(data.name, (ExternalPropertyData)data.clone());
        }
    }

    /**
     * creates a component from the constructor in ComponentType
     * sets height and width correctly
     * @param ship the Ship of which the Component will be part of
     * @return the new Component
     */
    public Component createComponent(Ship ship) {
        Component component = componentType.constructor.apply(this, ship);
        component.setWidth(getWidth() * ShipDef.UNIT_SIZE);
        component.setHeight(getHeight() * ShipDef.UNIT_SIZE);
        return component;
    }

    public void initExternalProperty (ExternalProperty property) {
        ExternalPropertyData data = properties.get(property.name);
        property.setInitValue(data.initData);
        //TODO implementation of changedMethodStatement probably with the SpaceSimulation's list of methods
    }

    public PolygonShape getShape() {
        return componentType.getShape(rotation % 2 == 1);
    }

    /**
     * verifies all ExternalPropertyDatas
     * @return true if everything is ok
     */
    public boolean verifyProperties(Map<String, ? extends MethodDefinition> methods) {
        boolean result = true;
        for (ExternalPropertyData data : properties.values()) {
            result &= data.verify(methods);
        }
        return result;
    }

    public void toJson(Json json) {
        json.writeObjectStart();
        //write all the basic values
        json.writeValue("type", getType());
        json.writeValue("name", name);
        json.writeValue("x", x);
        json.writeValue("y", y);
        json.writeValue("rotation", rotation);

        ///write all the properties
        json.writeArrayStart("properties");
        for (Map.Entry<String, ExternalPropertyData> entry : properties.entrySet()) {
            entry.getValue().toJson(json, entry.getKey());
        }
        json.writeArrayEnd();

        json.writeObjectEnd();
    }

    public static ComponentDef fromJson(JsonValue value) {
        ComponentDef comDef = new ComponentDef(ComponentType.valueOf(value.getString("type")));
        //set the basic values
        comDef.setName(value.getString("name"));
        comDef.setX(value.getInt("x"));
        comDef.setY(value.getInt("y"));
        comDef.setRotation(value.getInt("rotation"));

        //init all properties
        for (JsonValue propertyValue : value.get("properties")) {
            ExternalPropertyData data = comDef.properties.get(propertyValue.getString("key"));
            if (!data.readonly) data.initData = propertyValue.getString("initData");
            data.handlerName = propertyValue.getString("handlerName");
        }

        return comDef;
    }

}
