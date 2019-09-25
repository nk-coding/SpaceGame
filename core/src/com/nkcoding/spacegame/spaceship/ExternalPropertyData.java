package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.utils.Json;
import com.nkcoding.interpreter.compiler.DataTypes;

public class ExternalPropertyData {
    /**the name of the property*/
    public final String name;

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
    public ExternalPropertyData(String name, String type, boolean readonly) {
        //check if type is ok
        if (type.equals(DataTypes.Void)) throw new IllegalArgumentException("type cannot be " + DataTypes.Void);
        this.name = name;
        this.type = type;
        this.readonly = readonly;
    }

    /**
     * wrapper which sets readonly to true
     * @param type the type of the property
     */
    public ExternalPropertyData(String name, String type) {
        this(name, type, true);
    }

    public boolean verifyInit(String init) {
        switch (type) {
            case DataTypes.Boolean:
                return init.equalsIgnoreCase("true") || init.equalsIgnoreCase("false");
            case DataTypes.Float:
                try {
                    if (init.isBlank()) return true;
                    Float.parseFloat(init);
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
            case DataTypes.Integer:
                try {
                    if (init.isBlank()) return true;
                    Integer.parseInt(init);
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

    @Override
    protected Object clone() {
        return new ExternalPropertyData(name, type, readonly);
    }

    //readonly = true;
    public static ExternalPropertyData of(String name, String type) {
        return of(name, type, true);
    }

    public static ExternalPropertyData of(String name, String type, boolean readonly) {
        return new ExternalPropertyData(name, type, readonly);
    }
}
