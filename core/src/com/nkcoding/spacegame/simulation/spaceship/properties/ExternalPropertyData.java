package com.nkcoding.spacegame.simulation.spaceship.properties;

import com.badlogic.gdx.utils.Json;
import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.interpreter.compiler.MethodDefinition;

import java.util.Map;

public class ExternalPropertyData {
    public String initData = "";

    public String handlerName = "";

    public final ExternalPropertySpecification specification;

    public ExternalPropertyData(ExternalPropertySpecification specification) {
        this.specification = specification;
    }


    /**
     * copy constructor
     */
    public ExternalPropertyData(ExternalPropertyData toCopy) {
        this(toCopy.specification);
        this.handlerName = toCopy.handlerName;
        this.initData = toCopy.initData;
    }


    /**
     * checks for correct handler and init
     *
     * @param methods map with all methods
     * @return true if everything is ok
     */
    public boolean verify(Map<String, ? extends MethodDefinition> methods) {
        return specification.verify(methods, initData, handlerName);
    }

    public void toJson(Json json, String key) {
        json.writeObjectStart();
        json.writeValue("key", key);
        if (specification.supportsWrite) json.writeValue("initData", initData);
        if (specification.supportsChangedHandler) json.writeValue("handlerName", handlerName);
        json.writeObjectEnd();
    }

    public DataType getType() {
        return specification.type;
    }

    public String getName() {
        return specification.name;
    }

    public boolean supportsWrite() {
        return specification.supportsWrite;
    }

    public boolean supportsRead() {
        return specification.supportsRead;
    }

    public boolean supportsChangedHandler() {
        return specification.supportsChangedHandler;
    }

    public String getGetterName() {
        return specification.getterName;
    }

    public String getSetterName() {
        return specification.setterName;
    }
}
