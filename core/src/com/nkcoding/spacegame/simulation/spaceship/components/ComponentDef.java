package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ExternalPropertySpecification;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalProperty;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * subclass which contains all the stuff that is necessary to design a ship but not emulate it<br>
 * this class is Serializable, BUT DO NOT DO IT
 */
public class ComponentDef extends ComponentDefBase {

    /**
     * HashMap with all the ExternalPropertyData
     */
    public final LinkedHashMap<String, ExternalPropertyData> properties = new LinkedHashMap<>();
    //name for the component
    private String name = "";

    /**
     * constructor with a ComponentInfo instead of a ComponentType
     *
     * @param type contains the type of the ComponentDef
     */
    public ComponentDef(ComponentType type) {
        super(type);
        //add all ExternalPropertyDefs
        for (ExternalPropertySpecification data : type.propertyDefs) {
            properties.put(data.name, new ExternalPropertyData(data));
        }
    }

    /**
     * copy constructor
     * @param toCopy ComponentDef to copy
     */
    public ComponentDef(ComponentDef toCopy) {
        super(toCopy);
        name = toCopy.name;
        for (Map.Entry<String, ExternalPropertyData> entry : toCopy.properties.entrySet()) {
            properties.put(entry.getKey(), new ExternalPropertyData(entry.getValue()));
        }
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
            if (data.supportsWrite()) data.initData = propertyValue.getString("initData");
            if (data.supportsChangedHandler()) data.handlerName = propertyValue.getString("handlerName");
        }

        return comDef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * creates a component from the constructor in ComponentType
     * sets height and width correctly
     *
     * @param ship the Ship of which the Component will be part of
     * @return the new Component
     */
    public Component createComponent(Ship ship, Ship.ShipModel shipModel) {
        return componentType.constructor.apply(this, ship, shipModel);
    }

    public void initExternalProperty(ExternalProperty property) {
        ExternalPropertyData data = properties.get(property.name);
        property.setInitValue(data.initData);
        //TODO implementation of changedMethodStatement probably with the SpaceSimulation's list of methods
    }

    /**
     * verifies all ExternalPropertyDatas
     *
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

}
