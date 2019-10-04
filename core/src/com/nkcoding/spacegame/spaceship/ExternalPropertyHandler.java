package com.nkcoding.spacegame.spaceship;

import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.MethodStatement;

import java.util.Collection;
import java.util.Map;

public interface ExternalPropertyHandler {
    String getName();

    Map<String, ExternalProperty> getProperties();

    //init ExternalProperties with the given ExternalPropertyData
    default void initProperties(Collection<ExternalPropertyData> datas, Map<String, MethodStatement> methods) {
        Map<String, ExternalProperty> properties = getProperties();
        for (ExternalPropertyData data : datas) {
            ExternalProperty property = properties.get(data.name);
            if (property != null) {
                if(!property.readonly) property.setInitValue(data.initData);
                if (!data.handlerName.equals(""))
                    property.setChangedMethodStatement(methods.get(data.handlerName));
            }
        }
    }

    //init the ExternalProperties with the ExternalProperties
    default void initProperties(Collection<ExternalProperty> toClone) {
        Map<String, ExternalProperty> properties = getProperties();
        for (ExternalProperty clone : toClone) {
            ExternalProperty property = properties.get(clone.name);
            if (property != null) {
                property.set(clone.get2());
                property.setChangedMethodStatement(clone.getChangedMethodStatement());
            }
        }
    }

    //registers a property to properties
    default <T extends ExternalProperty> T register(T property) {
        getProperties().put(property.name, property);
        return property;
    }

    default boolean handleExternalMethod(ExternalMethodFuture future) {
        ExternalProperty property = getProperties().get(future.getName().substring(3));
        if (property != null) {
            if (future.getParameters().length == 2) {
                //it is a setter
                property.set(future.getParameters()[1]);
                future.complete(null);
            }
            else {
                //it is a getter
                future.complete(property.get2());
            }
            return true;
        }
        else return false;
    }


}
