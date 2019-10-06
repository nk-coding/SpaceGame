package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.MethodDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.nkcoding.spacegame.spaceship.ExternalPropertyData.of;
import static com.nkcoding.spacegame.spaceship.Ship.KeyDownKey;
import static com.nkcoding.spacegame.spaceship.Ship.KeyUpKey;

public class ShipDef {
    public class ShipDesignerHelper{
        //default constructor
        protected  ShipDesignerHelper() {
            //init the componentsMap
            for (ComponentDef componentDef : componentDefs) {
                for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++){
                    for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++){
                        componentsMap[_x][_y] = componentDef;
                    }
                }
            }
        }

        private ComponentDef[][] componentsMap = new ComponentDef[MAX_SIZE][MAX_SIZE];

        //locate component if there is one
        public ComponentDef getComponent(int x, int y){
            return componentsMap[x][y];
        }

        //Add a component to componentsMap and componentsDefs
        //this requires that TryMoveComponent was called immediately before
        //otherwise this might lead to unexpected behaviour
        public void addComponent(ComponentDef componentDef, int x, int y, int rotation){
            componentDefs.add(componentDef);
            //update component
            componentDef.setRotation(rotation);
            componentDef.setX(x);
            componentDef.setY(y);
            //add new references
            for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++){
                for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++){
                    componentsMap[_x][_y] = componentDef;
                }
            }
        }

        public void removeComponent(ComponentDef componentDef) {
            //delete the old references
            if (componentDefs.contains(componentDef)){
                componentDefs.remove(componentDef);
                for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++){
                    for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++){
                        componentsMap[_x][_y] = null;
                    }
                }
            }
        }

        //is it possible to move a component to a specific position
        public boolean tryMoveComponent(ComponentDef componentDef, int x, int y, int rotation){
            //calculate resulting width and height
            int width = (rotation % 2 == 0) ? componentDef.getWidth() : componentDef.getHeight();
            int height = (rotation % 2 == 0) ? componentDef.getHeight() : componentDef.getWidth();
            boolean result = true;
            for (int _x = x; _x < (x + width); _x++){
                for (int _y = y; _y < (y + height); _y++){
                    //check if it is in range
                    //check if there is no component or (the not moved) same
                    result = result && (_x < MAX_SIZE && _x >= 0 && _y >= 0 && _y < MAX_SIZE && (componentsMap[_x][_y] == null || componentsMap[_x][_y] == componentDef));
                }
            }
            return result;
        }

        //tries to rotate a component
        public void rotateComponent(ComponentDef def) {
            int x = def.getX();
            int y = def.getY();
            int rotation = def.getRotation();

            for (int i = 0; i < 3; i++) {
                rotation++;
                rotation %= 4;
                switch (rotation) {
                    case 0:
                        y += def.getWidth() - 1;
                        break;
                    case 1:
                        x -= def.getHeight() - 1;
                        break;
                    case 2:
                        x += def.getHeight() - 1;
                        x -= def.getWidth() - 1;
                        y -= def.getHeight() - 1;
                        break;
                    case 3:
                        x += def.getWidth() - 1;
                        y += def.getHeight() - 1;
                        y -= def.getWidth() - 1;
                        break;
                }
                if (tryMoveComponent(def, x ,y, rotation)) {
                    moveComponent(def, x, y, rotation);
                    return;
                }
            }
        }

        //actually move component
        //this requires that TryMoveComponent was called immediately before
        //otherwise this might lead to unexpected behaviour
        public void moveComponent(ComponentDef componentDef, int x, int y, int rotation){
            //delete the old references
            removeComponent(componentDef);
            //add new references
            addComponent(componentDef, x, y, rotation);
        }
    }

    //the max width / height of a ship
    public static final int MAX_SIZE = 25;

    //the size of one unit in box2d
    public static final float UNIT_SIZE = 0.1f;

    public final ArrayList<ComponentDef> componentDefs = new ArrayList<>();

    //code for the script
    public String code = "//write your own code here";

    //every Ship needs a name
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //is everything ok  -> is the ship usable
    private boolean validated = false;

    public boolean getValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    //ShipDesignerHelper if one is attached
    private ShipDesignerHelper designerHelper = null;

    public ShipDesignerHelper getShipDesignerHelper() {
        if (designerHelper == null) designerHelper = new ShipDesignerHelper();
        return designerHelper;
    }

    /**HashMap with all the ExternalPropertyData*/
    public final LinkedHashMap<String, ExternalPropertyData> properties = new LinkedHashMap<>();

    //the main Constructor
    public ShipDef() {
        //add all the properties
        properties.put(KeyDownKey, of(KeyDownKey, DataTypes.String));
        properties.put(KeyUpKey, of(KeyUpKey, DataTypes.String));
    }

    /**
     * get the component with the name name
     * @param name case sensitive name of the component
     * @return the ComponentDef or null if it does not exist
     */
    public ComponentDef getComponent(String name) {
        return componentDefs.stream().filter(def -> def.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * checks if two components have the same name
     * @return true if everything is ok
     */
    public boolean verifyNames() {
        long noDuplicates = componentDefs.stream().map(ComponentDef::getName).filter(e -> !e.equals("")).distinct().count();
        long duplicates = componentDefs.stream().map(ComponentDef::getName).filter(e -> !e.equals("")).count();
        boolean shipName = getComponent(this.name) == null;
        return noDuplicates == duplicates && shipName;
    }

    /**
     * checks if the name already exists, it is ok if the same ComponentDef already uses the name
     * @param def the ComponentDef that will use the name
     * @param name the name to check
     * @return true if the name is not used or used by def
     */
    public boolean verifyComponentName(ComponentDef def, String name) {
        if (!name.equals(this.name)) {
            if (name.equals("")) return true;
            ComponentDef nameDef = getComponent(name);
            return nameDef == null || nameDef == def;
        }
        else return def == null;
    }

    /**
     * checks the properties for all components
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public boolean verifyComponentProperties(Map<String, ? extends MethodDefinition> methods) {
        boolean result = true;
        for (ComponentDef def : componentDefs) {
            result &= def.verifyProperties(methods);
        }
        return result;
    }

    public void toJson(Json json) {
        json.writeObjectStart(this.getClass().getSimpleName());

        //write all the components
        json.writeArrayStart("comDefs");
        for (ComponentDef comDef : componentDefs) {
            comDef.toJson(json);
        }
        json.writeArrayEnd();

        //write the code
        json.writeValue("code", code);
        json.writeValue("name", name);
        json.writeValue("validated", validated);

        ///write all the properties
        json.writeArrayStart("properties");
        for (Map.Entry<String, ExternalPropertyData> entry : properties.entrySet()) {
            entry.getValue().toJson(json, entry.getKey());
        }
        json.writeArrayEnd();

        json.writeObjectEnd();
    }

    public static ShipDef fromJson(JsonValue value) {
        ShipDef shipDef = new ShipDef();
        //load ShipDefs
        for (JsonValue componentValue : value.get("comDefs")) {
            shipDef.componentDefs.add(ComponentDef.fromJson(componentValue));
        }
        //load code
        shipDef.code = value.getString("code");
        shipDef.name = value.getString("name");
        shipDef.validated = value.getBoolean("validated");

        //init all properties
        for (JsonValue propertyValue : value.get("properties")) {
            ExternalPropertyData data = shipDef.properties.get(propertyValue.getString("key"));
            if (!data.readonly) data.initData = propertyValue.getString("initData");
            data.handlerName = propertyValue.getString("handlerName");
        }

        return shipDef;
    }

}
