package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.nkcoding.spacegame.Simulated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Ship implements Simulated {

    //the body which represents the Ship in box2d
    private Body body;

    public  Body getBody(){
        return body;
    }

    //the list of components which compose the ship
    List<Component> components;

    //corresponds the order of the components to the order of the PowerLevel?
    private boolean isPowerLevelOrderCorrect = false;

    //did the power request change?
    private boolean isPowerRequestDifferent = true;

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

    //checks if the ship structure is intact or constructs partial ships otherwise
    private void checkStructure(){
        //TODO implementation
    }

    //region power system
    void invalidatePowerLevelOrder() {
        isPowerLevelOrderCorrect = false;
        invalidatePowerDelivery();
    }

    void invalidatePowerDelivery() {
        isPowerRequestDifferent = true;
    }

    //endregion


    @Override
    public void act(float time) {
        //check if power level order is correct
        if (!isPowerLevelOrderCorrect) {
            //change the order
            components.sort(Comparator.comparingInt(Component::getRequestLevel));
            isPowerLevelOrderCorrect = true;
        }
        //update the power stuff
        if (isPowerRequestDifferent) {
            //update how much power each component
            //check how much power is available
            float availablePower = 0f;
            for (Component component : components) {
                //check if it consumes or delivers power
                if (component.getPowerRequested() < 0) {
                    availablePower -= component.getPowerRequested();
                }
            }
            float startAvailablePower = availablePower;
            //now check for every level how much power each individual component gets
            int i = 0;
            while (i < components.size()) {
                //is power available?
                if (availablePower > 0) {
                    //there is power available
                    int i0 = i;
                    int level = components.get(i).getRequestLevel();
                    float levelPowerRequest = 0f;
                    //check how much power is necessary for this level
                    while (i < components.size() && components.get(i).getRequestLevel() == level) {
                        levelPowerRequest += components.get(i).getPowerRequested();
                        i++;
                    }
                    //is enough power available
                    if (levelPowerRequest <= availablePower) {
                        //enough power is available
                        availablePower -= levelPowerRequest;
                        for (int x = i0; x < i; x++) {
                            components.get(x).setPowerReceived(components.get(x).getPowerRequested());
                        }
                    }
                    else {
                        //not enough power is available
                        float fac = levelPowerRequest / availablePower;
                        availablePower = 0f;
                        for (int x = i0; x < i; x++) {
                            components.get(x).setPowerReceived(components.get(x).getPowerRequested() * fac);
                        }
                    }
                }
                else {
                    //no power is available, set all to 0
                    while (i < components.size()) {
                        components.get(i).setPowerReceived(0f);
                        i++;
                    }
                }

            }
            //update the components which deliverPower
            float fac = (startAvailablePower - availablePower) / availablePower;
            for (Component component : components) {
                //check if it consumes or delivers power
                if (component.getPowerRequested() < 0) {
                    component.setPowerReceived(component.getPowerRequested() * fac);
                }
            }
            isPowerRequestDifferent = false;
        }
        //call act on all components
        for (Component component : components) {
            component.act(time);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {

    }
}
