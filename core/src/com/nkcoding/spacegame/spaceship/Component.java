package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.interpreter.ExternalMethodFuture;

import java.util.ArrayList;

public abstract class Component {
    
    //region names for the ExternalProperties
    public static final String HealthKey = "Health";
    public static final String PowerRequestedKey = "PowerRequested";
    public static final String RequestLevelKey = "RequestLevel";
    public static final String HasFullPowerKey = "HasFullPower";
    public static final String PowerReceivedKey = "PowerReceived";
    //endregion

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

    public ComponentDef getComponentDef() {
        return componentDef;
    }

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
    protected final ArrayList<Fixture> fixtures = new ArrayList<>();

    //List with all the components
    private final ArrayList<ExternalProperty> properties = new ArrayList<>();

    //registers a property to properties
    protected <T extends ExternalProperty> T register(T property) {
        properties.add(property);
        return property;
    }

    //health has to be stored again, because it changes during simulation
    //if it reaches zero, the component should be destroyed TODO implementation
    //should be initialized in constructor out of componentDef
    public final IntProperty health = register(new IntProperty(true, true, HealthKey) {
        @Override
        public void set(int value) {
            super.set(Math.max(value, 0));
            if (get() == 0){
                //destroy this component
                destroy();
            }
        }
    });

    private void destroy() {
        ship.destroyComponent(this);
    }


    //the power system is complicated
    //it could be set ingame, but also uses automatic stuff

    //power that component requests
    public final FloatProperty powerRequested = register(new FloatProperty(true, true, PowerRequestedKey) {
        @Override
        public void set(float value) {
            if (get() != value) ship.invalidatePowerDelivery();
            super.set(value);
        }
    });

    //how important is it to get the power
    public final IntProperty requestLevel = register(new IntProperty(false, true, RequestLevelKey) {
        @Override
        public void set(int value) {
            if (get() != value) ship.invalidatePowerLevelOrder();
            super.set(value);
        }
    });

    //shows if the component get the full power (used to prevent issues with float rounding)
    public final BooleanProperty hasFullPower = register(new BooleanProperty(true, true, HasFullPowerKey));


    //how much power does it actually get
    public final FloatProperty powerReceived = register(new FloatProperty(true, true, PowerReceivedKey) {
        @Override
        public void set(float value) {
            super.set(value);
            hasFullPower.set(powerRequested.get() == powerReceived.get());
        }
    });

    //constructor to force subclasses to implement important stuff
    protected Component(ComponentDef componentDef, Ship ship){
        //set the final variables
        this.type = componentDef.getType();
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



//    /**
//     * creates an array with all existing external methods
//     * @return the Array with the external method definitions
//     */
//    public static MethodDefinition[] getExternalMethods() {
//        //TODO add others
//        //register all external methods
//        List<MethodDefinition> externalMethods = new ArrayList<>();
//        //this class
//        createExternalMethodDefs(externalMethods);
//
//        return externalMethods.toArray(MethodDefinition[]::new);
//    }

//    public static void createExternalMethodDefs(List<MethodDefinition> externalMethods) {
//        //health
//        externalMethods.add(Component.createExternalMethodDef("getHealth", DataTypes.Integer, true));
//        //powerRequested
//        externalMethods.add(Component.createExternalMethodDef("getPowerRequested", DataTypes.Float, true));
//        //requestLevel
//        externalMethods.add(Component.createExternalMethodDef("getRequestLevel", DataTypes.Integer, true));
//        externalMethods.add(Component.createExternalMethodDef("setRequestLevel", DataTypes.Integer, false));
//        //hasFullPower
//        externalMethods.add(Component.createExternalMethodDef("getHasFullPower", DataTypes.Boolean, true));
//        //powerReceived
//        externalMethods.add(Component.createExternalMethodDef("getPowerReceived", DataTypes.Float, true));
//    }

    public boolean handleExternalMethod(ExternalMethodFuture future) {
        ExternalProperty property = properties.stream().filter(prop -> future.getName().substring(3).equals(prop.name)).findFirst().orElse(null);
        if (property != null) {
            if (future.getName().charAt(0)=='g') {
                future.complete(property.get2());
            }
            else {
                property.set(future.getParameters()[0]);
                future.complete(null);
            }
            return true;
        }
        else return false;
    }
}

