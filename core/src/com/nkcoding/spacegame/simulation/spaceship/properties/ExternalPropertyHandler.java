package com.nkcoding.spacegame.simulation.spaceship.properties;

import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.MethodStatement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ExternalPropertyHandler {
    public abstract String getName();

    //map with all properties
    private HashMap<String, ExternalProperty> properties = new HashMap<>();

    public Map<String, ExternalProperty> getProperties() {
        return properties;
    }

    //init ExternalProperties with the given ExternalPropertyData
    public void initProperties(Collection<ExternalPropertyData> datas, Map<String, MethodStatement> methods) {
        Map<String, ExternalProperty> properties = getProperties();
        for (ExternalPropertyData data : datas) {
            ExternalProperty property = properties.get(data.name);
            if (property != null) {
                property.init(data, methods);
            }
        }
    }

    //registers a property to properties
    protected <T extends ExternalProperty> T register(T property) {
        getProperties().put(property.name, property);
        return property;
    }

    /**
     * must be overwritten if there is a property with a custom name
     * //TODO: move this feature to a map
     */
    public boolean handleExternalMethod(ExternalMethodFuture future) {
        ExternalProperty property = getProperties().get(future.getName().substring(3));
        if (property != null) {
            if (future.getParameters().length == 2) {
                //it is a setter
                property.set(future.getParameters()[1]);
                future.complete(null);
            } else {
                //it is a getter
                future.complete(property.get2());
            }
            return true;
        } else return false;
    }


}
