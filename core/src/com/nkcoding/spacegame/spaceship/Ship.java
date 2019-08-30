package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.nkcoding.spacegame.Simulated;

import java.util.ArrayList;
import java.util.List;

public class Ship implements Simulated {

    //the body which represents the Ship in box2d
    private Body body;

    public  Body getBody(){
        return body;
    }

    //the list of components which compose the ship
    List<Component> components;

    //construct Ship out of ShipDef (public constructor)
    public Ship(ShipDef def){
        //init new list with all the components
        components = new ArrayList<>(def.componentDefs.size());
        for (Component.ComponentDef comDef : def.componentDefs) {
            Component component = null;
            switch (comDef.getType()) {
                case TestType:
                    component = new TestImp(comDef, this);
                    break;
                default:
                    throw new UnsupportedOperationException(comDef.getType().toString());
            }
            components.add(component);
        }
    }

    //package-private constructor to construct Ship out of components (used to split up a ship)
    Ship(List<Component> components) {
        //set the components
        this.components = components;
    }

    //destroy a component (called by a component if it health 0)
    //check structural integrity afterwards
    void destroyComponent(Component component){
        components.remove(component);
        checkStructure();
    }

    private void checkStructure(){
        //TODO implementation
    }

    @Override
    public void act(float time) {

    }

    @Override
    public void draw(SpriteBatch batch) {

    }
}
