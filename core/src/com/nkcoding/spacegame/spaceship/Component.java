package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

import java.util.ArrayList;

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

    //it stores the shapes, so it can resit it if necessary and also remove it from the Ship
    protected final ArrayList<Fixture> fixtures = new ArrayList<Fixture>();

    //health has to be stored again, because it changes during simulation
    //if it reaches zero, the component should be destroyed TODO implementation
    //should be initialized in constructor out of componentDef
    private int health;

    public int getHealth(){
        return health;
    }

    public void setHealth(int health){
        if (health < 0) health = 0;
        this.health = health;
        if (health == 0){
            //TODO
        }
    }

    //the power system is complicated
    //it could be set ingame, but also uses automatic stuff

    //power that component requests
    private float powerRequest;

    public float getPowerRequest(){
        return powerRequest;
    }

    void setPowerRequest(float powerRequest){
        //TODO implementation
        //submit change to Ship
        this.powerRequest = powerRequest;
    }

    //how important is it to get the power
    private int requestLevel;

    public int getRequestLevel(){
        return this.requestLevel;
    }

    void setRequestLevel(int requestLevel){
        //TODO implementation
        //submit change to ship
        this.requestLevel = requestLevel;
    }

    //shows if the component get the full power (used to prevent issues with float rounding)
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
    private float receivedPower;

    public float getReceivedPower(){
        return receivedPower;
    }

    void setReceivedPower(float receivedPower){
        //TODO implementation
        //call a callback that the power level has changed
        this.receivedPower = receivedPower;
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
    public static ComponentDef createDefinition(){
        return null;
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
}

