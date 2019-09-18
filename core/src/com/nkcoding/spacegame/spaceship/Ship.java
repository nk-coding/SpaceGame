package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.spacegame.Simulated;
import com.nkcoding.spacegame.SpaceSimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Ship implements Simulated {

    //the body which represents the Ship in box2d
    private Body body;

    public  Body getBody(){
        return body;
    }

    //the list of components which compose the ship
    List<Component> components;

    //the map of the components
    Component[][] componentsMap;

    //corresponds the order of the components to the order of the PowerLevel?
    private boolean isPowerLevelOrderCorrect = false;

    //did the power request change?
    private boolean isPowerRequestDifferent = true;

    //is a structure check necessary
    private boolean isStructureCheckNecessary = true;

    //the simulation which handles this ship
    private SpaceSimulation spaceSimulation;

    public SpaceSimulation getSpaceSimulation() {
        return spaceSimulation;
    }

    //construct Ship out of ShipDef (public constructor)
    public Ship(ShipDef def, SpaceSimulation spaceSimulation) {
        //set simulation
        this.spaceSimulation = spaceSimulation;
        //init new list with all the components
        components = new ArrayList<>(def.componentDefs.size());
        //init the map
        componentsMap = new Component[ShipDef.MAX_SIZE][ShipDef.MAX_SIZE];
        for (ComponentDef comDef : def.componentDefs) {
            Component component = comDef.createComponent(this);
            components.add(component);
            //add to map
            for (int _x = comDef.getX(); _x < (comDef.getX() + comDef.getRealWidth()); _x++){
                for (int _y = comDef.getY(); _y < (comDef.getY() + comDef.getRealHeight()); _y++){
                    componentsMap[_x][_y] = component;
                }
            }
        }
    }

    //package-private constructor to construct Ship out of components (used to split up a ship)
    //pass other ship to copy important stuff (external method stuff etc.)
    Ship(Ship oldShip, List<Component> components) {
        //TODO implementation
        //set space simulation
        this.spaceSimulation = oldShip.getSpaceSimulation();
        //set the components
        this.components = components;
        //init the map
        componentsMap = new Component[ShipDef.MAX_SIZE][ShipDef.MAX_SIZE];
        for (Component component : components) {
            ComponentDef comDef = component.getComponentDef();
            //add to map
            for (int _x = comDef.getX(); _x < (comDef.getX() + comDef.getRealWidth()); _x++){
                for (int _y = comDef.getY(); _y < (comDef.getY() + comDef.getRealHeight()); _y++){
                    componentsMap[_x][_y] = component;
                }
            }
        }
    }

    //destroy a component (called by a component if it health 0)
    //check structural integrity afterwards
    void destroyComponent(Component component){
        components.remove(component);
        isStructureCheckNecessary = true;
    }

    //checks if the ship structure is intact or constructs partial ships otherwise
    private void checkStructure(){
        checkStructureRec(components.get(0));
        List<Component> otherComponents = components.stream().filter(com -> !com.structureHelper).collect(Collectors.toList());
        //remove other components
        components.removeAll(otherComponents);
       for (int x = 0; x < ShipDef.MAX_SIZE; x++) {
           for (int y = 0; y < ShipDef.MAX_SIZE; y++) {
               if (componentsMap[x][y] != null && !componentsMap[x][y].structureHelper) componentsMap[x][y] = null;
           }
       }
       //reset remaining
       components.forEach(component -> component.structureHelper = false);
       //create new ship
       spaceSimulation.addShip(new Ship(this, otherComponents));
    }

    //checks the structure recursive (helper for checkStructure())
    private void checkStructureRec(Component component) {
        component.structureHelper = true;
        ComponentDef comDef = component.getComponentDef();
        //go around component
        //check left side
        if (comDef.getX() > 0) {
            //there is a left side
            for (int y = comDef.getY(); y < (comDef.getY() + comDef.getRealHeight()); y++) {
                Component nextComponent = componentsMap[comDef.getX()][y - 1];
                if (nextComponent != null && !nextComponent.structureHelper) checkStructureRec(nextComponent);
            }
        }
        //check top side
        if (comDef.getY() > 0) {
            //there is a top side
            for (int x = comDef.getX(); x < (comDef.getX() + comDef.getRealWidth()); x++) {
                Component nextComponent = componentsMap[x - 1][comDef.getY()];
                if (nextComponent != null && !nextComponent.structureHelper) checkStructureRec(nextComponent);
            }
        }
        //check right side
        if (comDef.getX() < (ShipDef.MAX_SIZE - 1)) {
            //there is a right side
            for (int y = comDef.getY(); y < (comDef.getY() + comDef.getRealHeight()); y++) {
                Component nextComponent = componentsMap[comDef.getX()][y + 1];
                if (nextComponent != null && !nextComponent.structureHelper) checkStructureRec(nextComponent);
            }
        }
        //check bottom side
        if (comDef.getY() < (ShipDef.MAX_SIZE - 1)) {
            //there is a bottom side
            for (int x = comDef.getX(); x < (comDef.getX() + comDef.getRealWidth()); x++) {
                Component nextComponent = componentsMap[x + 1][comDef.getY()];
                if (nextComponent != null && !nextComponent.structureHelper) checkStructureRec(nextComponent);
            }
        }
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
        //check structure if necessary
        if (isStructureCheckNecessary) {
            checkStructure();
        }
        //check if power level order is correct
        if (!isPowerLevelOrderCorrect) {
            //change the order
            components.sort(Comparator.comparingInt(com -> com.requestLevel.get()));
            isPowerLevelOrderCorrect = true;
        }
        //update the power stuff
        if (isPowerRequestDifferent) {
            //update how much power each component
            //check how much power is available
            float availablePower = 0f;
            for (Component component : components) {
                //check if it consumes or delivers power
                if (component.powerRequested.get() < 0) {
                    availablePower -= component.powerReceived.get();
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
                    int level = components.get(i).requestLevel.get();
                    float levelPowerRequest = 0f;
                    //check how much power is necessary for this level
                    while (i < components.size() && components.get(i).requestLevel.get() == level) {
                        levelPowerRequest += components.get(i).powerRequested.get();
                        i++;
                    }
                    //is enough power available
                    if (levelPowerRequest <= availablePower) {
                        //enough power is available
                        availablePower -= levelPowerRequest;
                        for (int x = i0; x < i; x++) {
                            components.get(x).powerReceived.set(components.get(x).powerRequested.get());
                        }
                    }
                    else {
                        //not enough power is available
                        float fac = levelPowerRequest / availablePower;
                        availablePower = 0f;
                        for (int x = i0; x < i; x++) {
                            components.get(x).powerReceived.set(components.get(x).powerRequested.get() * fac);
                        }
                    }
                }
                else {
                    //no power is available, set all to 0
                    while (i < components.size()) {
                        components.get(i).powerReceived.set(0f);
                        i++;
                    }
                }

            }
            //update the components which deliverPower
            float fac = (startAvailablePower - availablePower) / availablePower;
            for (Component component : components) {
                //check if it consumes or delivers power
                if (component.powerRequested.get() < 0) {
                    component.powerReceived.set(component.powerRequested.get() * fac);
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

    /**
     * get or set the value
     * @param future the ExternalMethodFuture which contains name and parameters
     */
    public void handleExternalMethod(ExternalMethodFuture future) {
        Component component = components.stream().filter(com -> com.getName().equals(future.getParameters()[future.getParameters().length - 1])).findFirst().orElse(null);
        if (component != null) component.handleExternalMethod(future);
    }
}
