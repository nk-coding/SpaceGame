package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.utils.Json;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.MethodType;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.util.Collection;
import java.util.Map;

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

    /**
     * checks if the method exists and has the correct signature
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public boolean verifyHandler(String handlerName, Map<String, ? extends MethodDefinition> methods) {
        //TODO check for correct type
        return handlerName.equals("") || methods.containsKey(handlerName);
    }

    /**
     * checks for correct handler and init
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public boolean verify(Map<String, ? extends MethodDefinition> methods) {
        return (readonly || verifyInit(this.initData)) && verifyHandler(this.handlerName, methods);
    }



    //adds the external method definitions the list
    public void addExternalMethodDefs(Collection<? super MethodDefinition> list) {
        list.add(createExternalMethodDef(true));
        if (!readonly) list.add(createExternalMethodDef(false));
    }

    //helper for addExternalMethodDef
    private MethodDefinition createExternalMethodDef(boolean get) {
        if (get) {
            return new MethodDefinition(MethodType.External, "get" + name, type, new TypeNamePair("id", DataTypes.String));
        }
        else {
            return new MethodDefinition(MethodType.External, "set" + name, DataTypes.Void, new TypeNamePair("value", type), new TypeNamePair("id", DataTypes.String));
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
