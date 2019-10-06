package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.ExtAssetManager;

import java.util.HashMap;
import java.util.Map;

public abstract class Component implements ExternalPropertyHandler {
    
    //region names for the ExternalProperties
    public static final String HealthKey = "Health";
    public static final String PowerRequestedKey = "PowerRequested";
    public static final String RequestLevelKey = "RequestLevel";
    public static final String HasFullPowerKey = "HasFullPower";
    public static final String PowerReceivedKey = "PowerReceived";
    //endregion


    private HashMap<String, ExternalProperty> properties = new HashMap<>();

    @Override
    public Map<String, ExternalProperty> getProperties() {
        return properties;
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
        if (ship != null) removeFixtures();
        this.ship = ship;
        addFixtures();
    }

    //get the ExtAssetManager
    protected ExtAssetManager getAssetManager() {
        return ship.getSpaceSimulation().getAssetManager();
    }

    //helper to check structural integrity
    boolean structureHelper = false;

    /**
     * the fixture that represents the box
     * other fixtures could be added as fields if necessary by sublasses
     */
    protected Fixture borderFixture;

    /**
     * the default texture that will be drawn if draw is not overwritten
     * uses the preview textures
     */
    protected Texture defaultTexture;

    //region properties

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
        setShip(ship);
        defaultTexture = getAssetManager().getTexture(componentDef.getPreviewImage());
        //set health
        health.set(componentDef.getHealth());
    }

    //the physics system

    /**
     * add the fixtures
     * this should be overwritten if a subclass defines more fixtures
     * do NOT remove the old fixtures
     */
    public void addFixtures() {
        ComponentType type = componentDef.getType();
        this.borderFixture = ship.getBody().createFixture(componentDef.getShape(),
                type.mass * ShipDef.UNIT_SIZE * ShipDef.UNIT_SIZE / type.width / type.height);
    }

    /**
     * removes the borderFixture
     * should be overwritten, if other fixtures have to be removed
     */
    public void removeFixtures(){
        removeFixture(borderFixture);
    }

    /**
     * removes a single fixture from the ship
     * @param fixture the Fixture that should be removed
     */
    protected void removeFixture(Fixture fixture) {
        getShip().getBody().destroyFixture(fixture);
    }

    private void destroy() {
        ship.destroyComponent(this);
    }


    public void act(float delta) {
        for (ExternalProperty property : getProperties().values()) {
            property.startChangedHandler(ship.getSpaceSimulation().getScriptingEngine());
        }
    }

    //just a simple default draw implementation
    public void draw(Batch batch) {
        ComponentDef def = getComponentDef();
        float rotation = 90 * def.getRotation();
        float x = 0, y = 0;
        switch(def.getRotation()) {
            case 0:
                x = ShipDef.UNIT_SIZE * def.getX();
                y = ShipDef.UNIT_SIZE * def.getY();
                break;
            case 1:
                x = ShipDef.UNIT_SIZE * (def.getX() + def.getRealWidth());
                y = ShipDef.UNIT_SIZE * def.getY();
                break;
            case 2:
                x = ShipDef.UNIT_SIZE * (def.getX() + def.getRealWidth());
                y = ShipDef.UNIT_SIZE * (def.getY() + def.getRealHeight());
                break;
            case 3:
                x = ShipDef.UNIT_SIZE * def.getX();
                y = ShipDef.UNIT_SIZE * (def.getY() + def.getRealHeight());
                break;
        }
        Vector2 drawPos = ship.localToWorldCoordinates(new Vector2(x, y));
        batch.draw(defaultTexture,
                drawPos.x, drawPos.y,
                0, 0,
                ShipDef.UNIT_SIZE * def.getWidth(), ShipDef.UNIT_SIZE * def.getHeight(),
                1,1,
                rotation + ship.getRotation() * MathUtils.radiansToDegrees,
                0,0,
                defaultTexture.getWidth(), defaultTexture.getHeight(),
                false, false);
    }
}

