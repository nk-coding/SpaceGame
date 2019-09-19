package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.utils.Json;
import com.nkcoding.interpreter.compiler.DataTypes;

public class ExternalPropertyData {
    /**the type of the property*/
    public final String type;

    /**can the value be modified by the user*/
    public final boolean readonly;

    public String initData = "";

    public String handlerName = "";

    /**
     * the default constructor
     * @param type the type of the property
     * @param readonly can the value be modified by the user
     */
    public ExternalPropertyData(String type, boolean readonly) {
        //check if type is ok
        if (type.equals(DataTypes.Void)) throw new IllegalArgumentException("type cannot be " + DataTypes.Void);
        this.type = type;
        this.readonly = readonly;
    }

    /**
     * wrapper which sets readonly to true
     * @param type the type of the property
     */
    public ExternalPropertyData(String type) {
        this(type, true);
    }

    public boolean verifyInit() {
        switch (type) {
            case DataTypes.Boolean:
                return initData.equalsIgnoreCase("true") || initData.equalsIgnoreCase("false");
            case DataTypes.Float:
                try {
                    Float.parseFloat(initData);
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
            case DataTypes.Integer:
                try {
                    Integer.parseInt(initData);
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
            case DataTypes.String:
                return true;
            default: throw new RuntimeException("not implemented");

        }
    }

    public void toJson(Json json, String key) {
        json.writeObjectStart();
        json.writeValue("key", key);
        if (!readonly) json.writeValue("initData", initData);
        json.writeValue("handlerName", handlerName);
        json.writeObjectEnd();
    }
}
