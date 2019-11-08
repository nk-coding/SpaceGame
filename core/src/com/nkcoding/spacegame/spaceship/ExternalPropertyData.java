package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.utils.Json;
import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.MethodType;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.util.Collection;
import java.util.Map;

public class ExternalPropertyData {
    /**
     * the name of the property
     */
    public final String name;

    /**
     * the type of the property
     */
    public final DataType type;

    /**
     * can the value be modified by the user
     */
    public final boolean readonly;

    /**
     * can the user get the value
     */
    public final boolean writeonly;

    public String initData = "";

    public String handlerName = "";

    /**
     * the default constructor
     *
     * @param type     the type of the property
     * @param readonly can the value be modified by the user
     */
    public ExternalPropertyData(String name, DataType type, boolean readonly, boolean writeonly) {
        //check if type is ok
        if (type.equals(DataType.VOID)) throw new IllegalArgumentException("type cannot be " + DataType.VOID_KW);
        this.name = name;
        this.type = type;
        this.readonly = readonly;
        this.writeonly = writeonly;
    }

    /**
     * wrapper which sets writeonly to false
     *
     * @param name     the name of the property
     * @param type     the type of the property
     * @param readonly can the value be modified by the user
     */
    public ExternalPropertyData(String name, DataType type, boolean readonly) {
        this(name, type, readonly, false);
    }

    /**
     * wrapper which sets readonly to true and writeonly to false
     *
     * @param type the type of the property
     */
    public ExternalPropertyData(String name, DataType type) {
        this(name, type, true, false);
    }

    public boolean verifyInit(String init) {
        if (init.equals("")) return true;
        switch (type.name) {
            case DataType.BOOLEAN_KW:
                return init.equalsIgnoreCase("true") || init.equalsIgnoreCase("false");
            case DataType.FLOAT_KW:
                try {
                    if (init.isBlank()) return true;
                    Float.parseFloat(init);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            case DataType.INTEGER_KW:
                try {
                    if (init.isBlank()) return true;
                    Integer.parseInt(init);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            case DataType.STRING_KW:
                return true;
            default:
                throw new RuntimeException("not implemented");

        }
    }

    /**
     * checks if the method exists and has the correct signature
     *
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public boolean verifyHandler(String handlerName, Map<String, ? extends MethodDefinition> methods) {
        boolean correctHandler = false;
        MethodDefinition def = methods.get(handlerName);
        if (def != null) {
            if (def.getParameters().length == 1 && def.getParameters()[0].getType().equals(type)) {
                correctHandler = true;
            }
        }

        return handlerName.equals("") || correctHandler;
    }

    /**
     * checks for correct handler and init
     *
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public boolean verify(Map<String, ? extends MethodDefinition> methods) {
        return (readonly || verifyInit(this.initData)) && verifyHandler(this.handlerName, methods);
    }


    //adds the external method definitions the list
    public void addExternalMethodDefs(Collection<? super MethodDefinition> list) {
        if (!readonly) {
            list.add(createExternalMethodDef(false));
        }
        if (!writeonly) {
            list.add(createExternalMethodDef(true));
        }
    }

    //helper for addExternalMethodDef
    private MethodDefinition createExternalMethodDef(boolean get) {
        if (get) {
            return new MethodDefinition(MethodType.External, "get" + name, type, new TypeNamePair("id", DataType.STRING));
        } else {
            return new MethodDefinition(MethodType.External, "set" + name, DataType.VOID,
                    new TypeNamePair("id", DataType.STRING),
                    new TypeNamePair("value", type));
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
        return new ExternalPropertyData(name, type, readonly, writeonly);
    }

    /**
     * wrapper for new ExternalPropertyData(name, type, readonly = true)
     */
    public static ExternalPropertyData of(String name, DataType type) {
        return of(name, type, true);
    }

    /**
     * wrapper for new ExternalPropertyData(name, type, readonly)
     */
    public static ExternalPropertyData of(String name, DataType type, boolean readonly) {
        return new ExternalPropertyData(name, type, readonly);
    }

    /**
     * wrapper for new ExternalPropertyData(name, type, readonly, writeonly)
     */
    public static ExternalPropertyData of(String name, DataType type, boolean readonly, boolean writeonly) {
        return new ExternalPropertyData(name, type, readonly, writeonly);
    }
}
