package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.nkcoding.interpreter.ExternalMethodFuture;

import java.util.ArrayList;

public class PropertyActor extends Actor implements ExternalPropertyHandler {
    //List with all the properties
    protected final ArrayList<ExternalProperty> properties = new ArrayList<>();

    //registers a property to properties
    protected <T extends ExternalProperty> T register(T property) {
        properties.add(property);
        return property;
    }

    @Override
    public boolean handleExternalMethod(ExternalMethodFuture future) {
        ExternalProperty property = properties.stream().filter(prop -> future.getName().substring(3).equals(prop.name)).findFirst().orElse(null);
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
