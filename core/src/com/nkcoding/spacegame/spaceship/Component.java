package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.MethodType;
import com.nkcoding.interpreter.compiler.TypeNamePair;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {
    //subclass which contains all the stuff that is necessary to design a ship but not emulate it
    public static abstract class ComponentDef{

        //the width of the component, it should be set in the constructor
        private int width;

        public int getWidth(){
            return width;
        }

        protected void setWidth(int width){
            this.width = width;
        }

        //the height of the component, it should be set in the constructor
        private int height;

        public int getHeight(){
            return height;
        }

        protected void setHeight(int height){
            this.height = height;
        }

        //0 = not rotated, 1 = 90°, 2 = 180°, 3 = 270°, everything different will be normalised, negative values are not allowed
        private int rotation;

        public int getRotation(){
            return rotation;
        }

        public void setRotation(int rotation){
            if (rotation < 0) throw new IllegalArgumentException();
            this.rotation = rotation % 4;
        }

        //the position, this is necessary for a ship to locate the component
        private int x;

        public int getX(){
            return x;
        }

        public void setX(int x){
            this.x = x;
        }

        //the position, this is necessary for a ship to locate the component
        private int y;

        public int getY(){
            return y;
        }

        public void setY(int y){
            this.y = y;
        }

        //start health can be set by a subclass, but is only mutable in Component
        //this should never be a negative value or zero
        //the component will be destroyed if health reaches zero
        protected int health = 100;

        public int getHealth(){
            return health;
        }

        //function to get width, includes rotation
        public int getRealWidth(){
            return ((rotation % 2) == 0) ? width : height;
        }

        //function to get height, includes rotation
        public int getRealHeight(){
            return ((rotation % 2) == 0) ? height : width;
        }

        //type
        private final ComponentType type;

        public ComponentType getType(){
            return type;
        }

        //name for the component
        private String name = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        //constructor
        //force subclasses to implement a type
        public ComponentDef(ComponentType type){
            this.type = type;
        }

    }

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

    public ComponentDef getComponentDef(){
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
    protected final ArrayList<Fixture> fixtures = new ArrayList<Fixture>();

    //health has to be stored again, because it changes during simulation
    //if it reaches zero, the component should be destroyed TODO implementation
    //should be initialized in constructor out of componentDef
    @ExternalProperty(key = "Health", readonly = true)
    private int health;

    public int getHealth(){
        return health;
    }

    public void setHealth(int health){
        if (health < 0) health = 0;
        this.health = health;
        if (health == 0){
            //destroy this component
            ship.destroyComponent(this);
        }
    }

    //the power system is complicated
    //it could be set ingame, but also uses automatic stuff

    //power that component requests
    @ExternalProperty(key = "PowerRequested", readonly = true)
    private float powerRequested;

    public float getPowerRequested(){
        return powerRequested;
    }

    void setPowerRequested(float powerRequested){
        //submit change to Ship
        if (powerRequested != this.powerRequested) ship.invalidatePowerDelivery();
        this.powerRequested = powerRequested;
    }

    //how important is it to get the power
    @ExternalProperty(key = "RequestLevel")
    private int requestLevel;

    public int getRequestLevel(){
        return this.requestLevel;
    }

    void setRequestLevel(int requestLevel){
        //submit change to ship
        if (requestLevel != this.requestLevel) ship.invalidatePowerLevelOrder();
        this.requestLevel = requestLevel;
    }

    //shows if the component get the full power (used to prevent issues with float rounding)
    @ExternalProperty(key = "HasFullPower", readonly = true)
    private boolean hasFullPower;

    public boolean isHasFullPower() {
        return hasFullPower;
    }

    void setHasFullPower(boolean hasFullPower){
        //TODO implementation
        //propably has to do nothing if everything is done in setReceivedPower
        this.hasFullPower = hasFullPower;
    }

    //how much power does it actually get
    @ExternalProperty(key = "PowerReceived", readonly = true)
    private float powerReceived;

    public float getPowerReceived(){
        return powerReceived;
    }

    void setPowerReceived(float powerReceived){
        //TODO implementation
        //call a callback that the power level has changed
        this.powerReceived = powerReceived;
        //update has full power
        setHasFullPower(powerRequested == powerReceived);
    }

    //constructor to force subclasses to implement important stuff
    protected Component(ComponentType type, ComponentDef componentDef, Ship ship){
        //check if the correct component is generated
        if (type != componentDef.getType()) throw new IllegalArgumentException();

        //set the final variables
        this.type = type;
        this.componentDef = componentDef;

        //set health
        setHealth(componentDef.getHealth());
    }

    //get the instance of a definition, this is necessary to avoid bugs
    /*
    public static ComponentDef createDefinition(){
        return null;
    }
     */



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
            return new MethodDefinition(MethodType.External, name, type);
        }
        else {
            return new MethodDefinition(MethodType.External, name, DataTypes.Void, new TypeNamePair("value", type));
        }
    }

    /**
     * creates an array with all existing external methods
     * @return the Array with the external method definitions
     */
    public static MethodDefinition[] createExternalMethodDefs() {
        //TODO add others
        //register all external methods
        List<MethodDefinition> externalMethods = new ArrayList<>();
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
        return externalMethods.toArray(MethodDefinition[]::new);
    }
}

