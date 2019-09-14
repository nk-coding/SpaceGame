package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.MethodType;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {

    //Type if the Component
    private final ComponentType type;

    public ComponentType getType(){
        return type;
    }

    //get the name
    public String getName() {
        return componentDef.getName();
    }

    //ComponentDef with which this was created, a reference is stored to reduce duplicate variables
    private final ComponentDef componentDef;

    //Ship which has references to box2d and drawing stuff
    //setter includes update stuff
    private Ship ship;

    public Ship getShip(){
        return ship;
    }

    void setShip(Ship ship){
        removeFixtures();
        this.ship = ship;
        addFixtures();
        //TODO other implementation stuff (probably graphical stuff)
    }

    //helper to check structural integrity
    boolean structureHelper = false;

    //it stores the shapes, so it can resit it if necessary and also remove it from the Ship
    protected final ArrayList<Fixture> fixtures = new ArrayList<Fixture>();

    //health has to be stored again, because it changes during simulation
    //if it reaches zero, the component should be destroyed TODO implementation
    //should be initialized in constructor out of componentDef
    public final IntProperty health = new IntProperty(true, true, ComponentDef.HealthKey) {
        @Override
        public void set(int value) {
            super.set(Math.max(value, 0));
            if (get() == 0){
                //destroy this component
                destroy();
            }
        }
    };

    private void destroy() {
        ship.destroyComponent(this);
    }


    //the power system is complicated
    //it could be set ingame, but also uses automatic stuff

    //power that component requests
    public final FloatProperty powerRequested = new FloatProperty(true, true, ComponentDef.PowerRequestedKey) {
        @Override
        public void set(float value) {
            if (get() != value) ship.invalidatePowerDelivery();
            super.set(value);
        }
    };

    //how important is it to get the power
    public final IntProperty requestLevel = new IntProperty(false, true, ComponentDef.RequestLevelKey) {
        @Override
        public void set(int value) {
            if (get() != value) ship.invalidatePowerLevelOrder();
            super.set(value);
        }
    };

    //shows if the component get the full power (used to prevent issues with float rounding)
    public final BooleanProperty hasFullPower = new BooleanProperty(true, true, ComponentDef.HasFullPowerKey);


    //how much power does it actually get
    public final FloatProperty powerReceived = new FloatProperty(true, true, ComponentDef.PowerReceivedKey) {
        @Override
        public void set(float value) {
            super.set(value);
            hasFullPower.set(powerRequested.get() == powerReceived.get());
        }
    };

    //constructor to force subclasses to implement important stuff
    protected Component(ComponentType type, ComponentDef componentDef, Ship ship){
        //check if the correct component is generated
        if (type != componentDef.getType()) throw new IllegalArgumentException();

        //set the final variables
        this.type = type;
        this.componentDef = componentDef;

        //set health
        health.set(componentDef.getHealth());
    }

    //the physics system

    //add the shapes
    //don't remove the old ones
    public abstract void addFixtures();

    //remove the fixtures
    public void removeFixtures(){
        Body body = getShip().getBody();
        for (Fixture f : fixtures){
            body.destroyFixture(f);
        }
        fixtures.clear();
    }

    void act (float time) {
        //TODO implementation
    }

    private static MethodDefinition createExternalMethodDef(String name, String type, boolean get) {
        if (get) {
            return new MethodDefinition(MethodType.External, name, type, new TypeNamePair("id", DataTypes.String));
        }
        else {
            return new MethodDefinition(MethodType.External, name, DataTypes.Void, new TypeNamePair("value", type), new TypeNamePair("id", DataTypes.String));
        }
    }

    /**
     * creates an array with all existing external methods
     * @return the Array with the external method definitions
     */
    public static MethodDefinition[] getExternalMethods() {
        //TODO add others
        //register all external methods
        List<MethodDefinition> externalMethods = new ArrayList<>();
        //this class
        createExternalMethodDefs(externalMethods);

        return externalMethods.toArray(MethodDefinition[]::new);
    }

    public static void createExternalMethodDefs(List<MethodDefinition> externalMethods) {
        //health
        externalMethods.add(Component.createExternalMethodDef("getHealth", DataTypes.Integer, true));
        //powerRequested
        externalMethods.add(Component.createExternalMethodDef("getPowerRequested", DataTypes.Float, true));
        //requestLevel
        externalMethods.add(Component.createExternalMethodDef("getRequestLevel", DataTypes.Integer, true));
        externalMethods.add(Component.createExternalMethodDef("setRequestLevel", DataTypes.Integer, false));
        //hasFullPower
        externalMethods.add(Component.createExternalMethodDef("getHasFullPower", DataTypes.Boolean, true));
        //powerReceived
        externalMethods.add(Component.createExternalMethodDef("getPowerReceived", DataTypes.Float, true));
    }

    public void handleExternalMethod(ExternalMethodFuture future) {
        switch (future.getName()) {
            case "getHealth":
                future.complete(health.get());
                break;
            case "getPowerRequested":
                future.complete(powerRequested.get());
                break;
            case "getRequestLevel":
                future.complete(requestLevel.get());
                break;
            case "setRequestLevel":
                requestLevel.set((int)future.getParameters()[0]);
                future.complete(null);
                break;
            case "getHasFullPower":
                future.complete(hasFullPower.get());
                break;
            case "getPowerReceived":
                future.complete(powerReceived.get());
                break;
            default:
                throw new IllegalArgumentException("can't handle " + future.getName());
        }
    }
}

