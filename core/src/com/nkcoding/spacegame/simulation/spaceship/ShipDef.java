package com.nkcoding.spacegame.simulation.spaceship;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentType;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipDef {
    //the max width / height of a ship
    public static final int MAX_SIZE = 25;
    //the size of one unit in box2d
    public static final float UNIT_SIZE = 0.1f;
    public final ArrayList<ComponentDef> componentDefs = new ArrayList<>();

    //code for the script
    public String code = "//write your own code here";

    //is everything ok  -> is the ship usable
    private boolean validated = false;
    //ShipDesignerHelper if one is attached
    private ShipDesignerHelper designerHelper = null;

    //the main Constructor
    public ShipDef() {

    }

    public static ShipDef fromJson(JsonValue value) {
        ShipDef shipDef = new ShipDef();
        //load ShipDefs
        for (JsonValue componentValue : value.get("comDefs")) {
            shipDef.componentDefs.add(ComponentDef.fromJson(componentValue));
        }
        //load code
        shipDef.code = value.getString("code");
        shipDef.validated = value.getBoolean("validated");

        return shipDef;
    }

    public boolean getValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public ShipDesignerHelper getShipDesignerHelper() {
        if (designerHelper == null) designerHelper = new ShipDesignerHelper();
        return designerHelper;
    }

    /**
     * get the component with the name name
     *
     * @param name case sensitive name of the component
     * @return the ComponentDef or null if it does not exist
     */
    public ComponentDef getComponent(String name) {
        if (name.equals("")) return null;
        else return componentDefs.stream().filter(def -> def.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * get the amount of components
     *
     * @return the amount of components (0 if there are none)
     */
    public int getComponentCount() {
        return componentDefs.size();
    }

    /**
     * checks if two components have the same name
     *
     * @return true if everything is ok
     */
    public boolean verifyNames() {
        long noDuplicates = componentDefs.stream().map(ComponentDef::getName).filter(e -> !e.equals("")).distinct().count();
        long duplicates = componentDefs.stream().map(ComponentDef::getName).filter(e -> !e.equals("")).count();
        return noDuplicates == duplicates;
    }

    /**
     * checks if the name already exists, it is ok if the same ComponentDef already uses the name
     *
     * @param def  the ComponentDef that will use the name
     * @param name the name to check
     * @return true if the name is not used or used by def
     */
    public boolean verifyComponentName(ComponentDef def, String name) {
        if (name.equals("")) return true;
        ComponentDef nameDef = getComponent(name);
        return nameDef == null || nameDef == def;
    }

    /**
     * checks the properties for all components
     *
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

    public Compiler createCompiler(String text) {
        //create the external method statements for the components
        HashMap<String, ExternalPropertyData> externalPropertyDatas = new HashMap<>();
        for (ComponentType com : ComponentType.values()) {
            for (ExternalPropertyData data : com.propertyDefs) {
                if (!externalPropertyDatas.containsKey(data.name)) {
                    externalPropertyDatas.put(data.name, data);
                }
            }
        }

        ArrayList<MethodDefinition> methodDefinitions = new ArrayList<>();
        for (ExternalPropertyData data : externalPropertyDatas.values()) {
            data.addExternalMethodDefs(methodDefinitions);
        }
        String[] lines = text.split("\\r?\\n");

        return new Compiler(lines, methodDefinitions.toArray(MethodDefinition[]::new));
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
        json.writeValue("validated", validated);

        json.writeObjectEnd();
    }

    public class ShipDesignerHelper {
        private ComponentDef[][] componentsMap = new ComponentDef[MAX_SIZE][MAX_SIZE];

        //default constructor
        protected ShipDesignerHelper() {
            //init the componentsMap
            for (ComponentDef componentDef : componentDefs) {
                for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++) {
                    for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++) {
                        componentsMap[_x][_y] = componentDef;
                    }
                }
            }
        }

        //locate component if there is one
        public ComponentDef getComponent(int x, int y) {
            if (x < 0 || y < 0 || x >= MAX_SIZE || y >= MAX_SIZE) {
                return null;
            } else {
                return componentsMap[x][y];
            }
        }

        //Add a component to componentsMap and componentsDefs
        //this requires that TryMoveComponent was called immediately before
        //otherwise this might lead to unexpected behaviour
        public void addComponent(ComponentDef componentDef, int x, int y, int rotation) {
            componentDefs.add(componentDef);
            //update component
            componentDef.setRotation(rotation);
            componentDef.setX(x);
            componentDef.setY(y);
            //add new references
            for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++) {
                for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++) {
                    componentsMap[_x][_y] = componentDef;
                }
            }
        }

        public void removeComponent(ComponentDef componentDef) {
            //delete the old references
            if (componentDefs.contains(componentDef)) {
                componentDefs.remove(componentDef);
                for (int _x = componentDef.getX(); _x < (componentDef.getX() + componentDef.getRealWidth()); _x++) {
                    for (int _y = componentDef.getY(); _y < (componentDef.getY() + componentDef.getRealHeight()); _y++) {
                        componentsMap[_x][_y] = null;
                    }
                }
            }
        }

        //is it possible to move a component to a specific position
        public boolean tryMoveComponent(ComponentDef componentDef, int x, int y, int rotation, List<ComponentDef> toIgnore) {
            //calculate resulting width and height
            int width = (rotation % 2 == 0) ? componentDef.getWidth() : componentDef.getHeight();
            int height = (rotation % 2 == 0) ? componentDef.getHeight() : componentDef.getWidth();
            boolean result = true;
            for (int _x = x; _x < (x + width); _x++) {
                for (int _y = y; _y < (y + height); _y++) {
                    //check if it is in range
                    //check if there is no component or (the not moved) same
                    result = result && (_x < MAX_SIZE && _x >= 0 && _y >= 0 && _y < MAX_SIZE
                            && (componentsMap[_x][_y] == null || toIgnore.contains(componentsMap[_x][_y])));
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
                if (tryMoveComponent(def, x, y, rotation, List.of(def))) {
                    moveComponent(def, x, y, rotation);
                    return;
                }
            }
        }

        //actually move component
        //this requires that TryMoveComponent was called immediately before
        //otherwise this might lead to unexpected behaviour
        public void moveComponent(ComponentDef componentDef, int x, int y, int rotation) {
            //delete the old references
            removeComponent(componentDef);
            //add new references
            addComponent(componentDef, x, y, rotation);
        }
    }

}
