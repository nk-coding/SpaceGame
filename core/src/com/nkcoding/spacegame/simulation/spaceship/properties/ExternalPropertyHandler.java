package com.nkcoding.spacegame.simulation.spaceship.properties;

import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ExternalMethodHandler;
import com.nkcoding.interpreter.MethodStatement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ExternalPropertyHandler implements ExternalMethodHandler {
    public abstract String getName();

    //map with all properties
    private HashMap<String, ExternalProperty> properties = new HashMap<>();

    private HashMap<String, ExternalProperty> methodNames = new HashMap<>();

    public Map<String, ExternalProperty> getProperties() {
        return properties;
    }

    //init ExternalProperties with the given ExternalPropertyData
    public void initProperties(Collection<ExternalPropertyData> datas, Map<String, MethodStatement> methods) {
        Map<String, ExternalProperty> properties = getProperties();
        for (ExternalPropertyData data : datas) {
            ExternalProperty property = properties.get(data.getName());
            if (property != null) {
                property.init(data, methods);
                if (property.supportsWrite) {
                    methodNames.put(data.getSetterName(), property);
                }
                if (property.supportsRead) {
                    methodNames.put(data.getGetterName(), property);
                }
            }
        }
    }

    //registers a property to properties
    protected <T extends ExternalProperty> T register(T property) {
        getProperties().put(property.name, property);
        return property;
    }

    /**
     * find automatically the correct ExternalProperty
     */
    public void handleExternalMethod(ExternalMethodFuture future) {
        ExternalProperty property = methodNames.get(future.getName());
        if (property != null) {
            if (future.getParameters().length == 2) {
                //it is a setter
                property.set(future.getParameters()[1]);
                future.complete(null);
            } else {
                //it is a getter
                future.complete(property.get2());
            }
        }
    }


}
