package com.nkcoding.spacegame.simulation.spaceship.properties;

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
    public final boolean supportsRead;

    /**
     * can the user get the value
     */
    public final boolean supportsWrite;

    /**
     * is the changedHandler used
     */
    public final boolean supportsChangedHandler;

    public String initData = "";

    public String handlerName = "";

    public final  String getterName;

    public final String setterName;

    /**
     * the default constructor
     */
    public ExternalPropertyData(String name, DataType type, boolean supportsRead, boolean supportsWrite, boolean supportsChangedHandler, String getterName, String setterName) {
        //check if type is ok
        if (type.equals(DataType.VOID)) throw new IllegalArgumentException("type cannot be " + DataType.VOID_KW);
        this.name = name;
        this.type = type;
        this.supportsRead = supportsRead;
        this.supportsWrite = supportsWrite;
        this.supportsChangedHandler = supportsChangedHandler;
        this.getterName = getterName;
        this.setterName = setterName;
    }

    /**
     * wrapper that sets getter- and setterName to the default get / set
     */
    public ExternalPropertyData(String name, DataType type, boolean supportsWrite, boolean supportsRead, boolean supportsChangedHandler) {
        this(name, type, supportsRead, supportsWrite, supportsChangedHandler, "get" + name, "set" + name);
    }

    /**
     * wrapper that sets getter- and setterName to the default get / set
     * and supportsRead to true
     */
    public ExternalPropertyData(String name, DataType type, boolean supportsWrite, boolean supportsChangedHandler) {
        this(name, type, true, supportsWrite, supportsChangedHandler, "get" + name, "set" + name);
    }

    /**
     * wrapper that sets getter- and setterName to the default get / set
     * and supportsRead to true and supportsChangedHandler to true
     */
    public ExternalPropertyData(String name, DataType type, boolean supportsWrite) {
        this(name, type, supportsWrite, true);
    }

    /**
     * wrapper that sets getter- and setterName to the default get / set
     * and supportsRead, supportsWrite and supportsChangedHandler to true
     */
    public ExternalPropertyData(String name, DataType type) {
        this(name, type, true);
    }

    /**
     * copy constructor
     */
    public ExternalPropertyData(ExternalPropertyData toCopy) {
        this(toCopy.name, toCopy.type, toCopy.supportsRead, toCopy.supportsWrite, toCopy.supportsChangedHandler, toCopy.getterName, toCopy.setterName);
        this.handlerName = toCopy.handlerName;
        this.initData = toCopy.initData;
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
        return (!supportsWrite || verifyInit(this.initData)) && (!supportsChangedHandler || verifyHandler(this.handlerName, methods));
    }

    //adds the external method definitions the list
    public void addExternalMethodDefs(Collection<? super MethodDefinition> list) {
        if (supportsWrite) {
            list.add(createSetter());
        }
        if (supportsRead) {
            list.add(createGetter());
        }
    }

    //helper for addExternalMethodDef
    private MethodDefinition createGetter() {
        return new MethodDefinition(MethodType.External, getterName, type, new TypeNamePair("id", DataType.STRING));
    }

    private MethodDefinition createSetter() {
        return new MethodDefinition(MethodType.External, setterName, DataType.VOID,
                new TypeNamePair("id", DataType.STRING),
                new TypeNamePair("value", type));
    }

    public void toJson(Json json, String key) {
        json.writeObjectStart();
        json.writeValue("key", key);
        if (supportsWrite) json.writeValue("initData", initData);
        if (supportsChangedHandler) json.writeValue("handlerName", handlerName);
        json.writeObjectEnd();
    }
}
