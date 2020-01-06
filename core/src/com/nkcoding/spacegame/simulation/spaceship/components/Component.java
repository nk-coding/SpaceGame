package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.simulation.Damageable;
import com.nkcoding.spacegame.simulation.Explosion;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.DamageTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.UpdateComponentTransmission;
import com.nkcoding.spacegame.simulation.spaceship.properties.*;

import java.util.HashMap;
import java.util.Map;

public abstract class Component implements Damageable {

    //region names for the ExternalProperties
    public static final String HEALTH_KEY = "Health";
    public static final String POWER_REQUESTED_KEY = "PowerRequested";
    public static final String REQUEST_LEVEL_KEY = "RequestLevel";
    public static final String HAS_FULL_POWER_KEY = "HasFullPower";
    public static final String POWER_RECEIVED_KEY = "PowerReceived";
    //endregion

    //region sides
    public static final int TOP_SIDE = 0;
    public static final int LEFT_SIDE = 1;
    public static final int BOTTOM_SIDE = 2;
    public static final int RIGHT_SIDE = 3;

    public ComponentModel model = null;


    /**
     * the type of the Component
     */
    public final ComponentType type;

    /**
     * the def of this component
     */
    protected final ComponentDefBase defBase;

    public ComponentDefBase getComponentDef() {
        return defBase;
    }

    public int getX() {
        return defBase.getX();
    }

    public int getY() {
        return defBase.getY();
    }

    //Ship which has references to box2d and drawing stuff
    //setter includes update stuff
    private Ship ship;

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
        if (model != null) {
            model.shipModel = ship.model;
        }
        addFixtures();
    }

    /**C
     * helper method to get the asset manager
     * @return the ship's asset manager
     */
    protected ExtAssetManager getAssetManager() {
        return ship.getSpaceSimulation().getAssetManager();
    }

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

    /**
     * constructor that just creates the mirror version
     */
    protected Component(ComponentDefBase defBase, Ship ship) {
        this.defBase = defBase;
        this.type = defBase.componentType;
        setShip(ship);
        defaultTexture = getAssetManager().getTexture(defBase.getDefaultTexture());
    }

    /**
     * constructor that creates the original version
     */
    protected Component(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        this(componentDef, ship);
        this.model = generateModel(shipModel);
    }

    protected abstract ComponentModel generateModel(Ship.ShipModel shipModel);

    //helper methods

    /**
     * @return getShip().getSpaceSimulation()
     */
    public SpaceSimulation getSpaceSimulation() {
        return getShip().getSpaceSimulation();
    }

    /**
     * @return the width including UNIT_SIZE
     */
    public float getWidth() {
        return type.width * ShipDef.UNIT_SIZE;
    }

    /**
     * @return the height including UNIT_SIZE
     */
    public float getHeight() {
        return type.height * ShipDef.UNIT_SIZE;
    }

    //the physics system

    /**
     * add the fixtures
     * this should be overwritten if a subclass defines more fixtures
     * do NOT remove the old fixtures
     */
    public void addFixtures() {
        this.borderFixture = ship.getBody().createFixture(defBase.getShape(defBase.getX(), defBase.getY()),
                type.mass * ShipDef.UNIT_SIZE * ShipDef.UNIT_SIZE / type.width / type.height);
        borderFixture.setUserData(this);
    }

    /**
     * removes the borderFixture
     * should be overwritten, if other fixtures have to be removed
     */
    public void removeFixtures() {
        removeFixture(borderFixture);
    }

    /**
     * removes a single fixture from the ship
     *
     * @param fixture the Fixture that should be removed
     */
    protected void removeFixture(Fixture fixture) {
        if (fixture != null)
            getShip().getBody().destroyFixture(fixture);
        else System.out.println("this fixture should not be null");
    }

    /**
     * destroy a component
     * synchronization is done by the ship
     */
    private void destroy() {
        ship.destroyComponent(this);
    }

    public void act(float delta, boolean original) {
        if (original) {
            model.act(delta);
        }
    }

    /**
     * a simple draw implementation that draws the defaultTexture
     *
     * @param batch the Batch to draw on
     */
    public void draw(Batch batch, boolean original) {
        drawTexture(batch, defaultTexture, new Vector2(0, 0),
                getWidth(), getHeight(), 0);
    }

    /**
     * draw a texture on the Component
     * implements NO clipping
     *
     * @param batch   the Batch to draw on
     * @param texture the Texture to draw
     * @param pos     the not-normalized position where the Texture should be drawn
     * @param width   the non-normalized width
     * @param height  the non-normalized height
     * @param degrees the angle in degrees
     */
    public void drawTexture(Batch batch, Texture texture, Vector2 pos, float width, float height, float degrees) {
        Vector2 drawPos = localToWorld(pos);
        batch.draw(texture,
                drawPos.x, drawPos.y,
                0, 0,
                width, height,
                1, 1,
                degrees + ship.getRotation() * MathUtils.radiansToDegrees + 90 * defBase.getRotation(),
                0, 0,
                texture.getWidth(), texture.getHeight(),
                false, false);
    }

    /**
     * transforms a local point to world point
     *
     * @param local Vector2 that is modified
     * @return the modified Vector2
     */
    public Vector2 localToWorld(Vector2 local) {
        final float x = local.x;
        final float y = local.y;
        switch (defBase.getRotation()) {
            case 0:
                local.x = ShipDef.UNIT_SIZE * defBase.getX() + x;
                local.y = ShipDef.UNIT_SIZE * defBase.getY() + y;
                break;
            case 1:
                local.x = ShipDef.UNIT_SIZE * (defBase.getX() + defBase.getRealWidth()) - y;
                local.y = ShipDef.UNIT_SIZE * defBase.getY() + x;
                break;
            case 2:
                local.x = ShipDef.UNIT_SIZE * (defBase.getX() + defBase.getRealWidth()) - x;
                local.y = ShipDef.UNIT_SIZE * (defBase.getY() + defBase.getRealHeight()) - y;
                break;
            case 3:
                local.x = ShipDef.UNIT_SIZE * defBase.getX() + y;
                local.y = ShipDef.UNIT_SIZE * (defBase.getY() + defBase.getRealHeight()) - x;
                break;
        }
        return ship.localToWorldCoordinates(local);
    }

    /**
     * append some damage on the Component
     *
     * @param fixture the Fixture that was hit
     * @param damage  the amount of damage
     */
    @Override
    public boolean damageAt(Fixture fixture, int damage) {
        sendToOriginal(new DamageTransmission(this, damage, getDamageID(fixture)));
        return true;
    }

    /**
     * get the damageID for a specific hit Fixture
     * used for transmission purposes
     * zero is the default value, if several IDs are necessary, this method must be overwritten
     * @param fixture the Fixture that was hit
     * @return the id
     */
    protected int getDamageID(Fixture fixture) {
        return 0;
    }

    /**
     * send a transmission to the original
     * uses a shortcut if it is the original
     * @param transmission the transmission to send
     */
    protected void sendToOriginal(UpdateComponentTransmission transmission) {
        if (ship.isOriginal()) {
            receiveTransmission(transmission);
        } else {
            ship.sendToOriginal(transmission);
        }
    }

    /**
     * send a transmission to all mirrors
     * @param transmission the transmission to send
     */
    protected void post(UpdateComponentTransmission transmission) {
        ship.post(transmission);
    }

    /**
     * subclasses should overwrite this method if they want to receive update transmissions
     * @param transmission the update transmission
     */
    public void receiveTransmission(UpdateComponentTransmission transmission) {
    }

    public class ComponentModel implements ExternalPropertyHandler {

        //ComponentDef with which this was created, a reference is stored to reduce duplicate variables
        private final ComponentDef componentDef;

        public ComponentDef getComponentDef() {
            return componentDef;
        }

        private Ship.ShipModel shipModel;

        //map with all properties
        private HashMap<String, ExternalProperty> properties = new HashMap<>();

        //helper to check structural integrity
        public boolean structureHelper = false;

        @Override
        public Map<String, ExternalProperty> getProperties() {
            return properties;
        }

        /**
         * health has to be stored again, because it changes during simulation
         * if it reaches zero, the component should be destroyed
         * this check is done in act because of library issues
         * should be initialized in constructor out of componentDef
         */
        public final IntProperty health = register(new IntProperty(true, true, HEALTH_KEY));

        /**
         * power that component requests
         */
        public final FloatProperty powerRequested = register(new FloatProperty(true, true, POWER_REQUESTED_KEY) {
            @Override
            public void set(float value) {
                if (get() != value) shipModel.invalidatePowerDelivery();
                super.set(value);
            }
        });

        /**
         * how important is it to get the power
         */
        public final IntProperty requestLevel = register(new IntProperty(false, true, REQUEST_LEVEL_KEY) {
            @Override
            public void set(int value) {
                if (get() != value) shipModel.invalidatePowerLevelOrder();
                super.set(value);
            }
        });

        /**
         * shows if the component get the full power (used to prevent issues with float rounding)
         */
        public final BooleanProperty hasFullPower = register(new BooleanProperty(true, true, HAS_FULL_POWER_KEY));


        /**
         * how much power does it actually get
         */
        public final FloatProperty powerReceived = register(new FloatProperty(true, true, POWER_RECEIVED_KEY) {
            @Override
            public void set(float value) {
                super.set(value);
                hasFullPower.set(powerRequested.get() == powerReceived.get());
            }
        });


        public ComponentModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            this.componentDef = componentDef;
            this.shipModel = shipModel;
            //set health
            health.set(getComponentDef().getHealth());
        }


        @Override
        public String getName() {
            return componentDef.getName();
        }

        public void act(float delta) {
            if (health.get() <= 0) destroy();
        }

        /**
         * append some damage on the Component
         *
         * @param fixture the Fixture that was hit
         * @param damage  the amount of damage
         */
        public boolean damageAt(Fixture fixture, int damage) {
            health.set(health.get() - damage);
            return true;
        }

        /**
         * spawn an explosion with the initial radius of Math.min(getWidth() / 2 * 0.9f, getHeight() / 2 * 0.9f)
         * @param endRadius the final radius of the explosion
         * @param damage how much damage does the explosion make
         * @param time how long lasts the explosion
         */
        protected final void spawnExplosion(float endRadius, float damage, float time) {
            Vector2 centerPos = localToWorld(new Vector2(getWidth() / 2, getHeight() / 2));
            Explosion explosion = new Explosion(getSpaceSimulation(),
                    Math.min(getWidth() / 2 * 0.9f, getHeight() / 2 * 0.9f), endRadius, time,
                    centerPos, getShip().getBody().getLinearVelocityFromWorldPoint(centerPos), damage);
            getSpaceSimulation().addSimulated(explosion);
        }

        /**
         * tries to attach another Component at the specified position
         * returns true, but can be overwritten by subclasses to implement new behavior
         * do NOT call this directly
         *
         * @param x    x pos of the attachment
         * @param y    y pos of the attachment
         * @param side align of the attachment
         * @return true if the component is allowed to attach
         */
        protected boolean attachComponentAt(int x, int y, int side) {
            return true;
        }

        /**
         * tries to attach another Component at the specified position
         * calls attachComponentAt and calculates rotation
         *
         * @param x    x pos of the attachment
         * @param y    y pos of the attachment
         * @param side align of the attachment
         * @return true if the component is allowed to attach
         */
        public final boolean attachComponentAtRaw(int x, int y, int side) {
            final ComponentDef def = getComponentDef();
            x -= def.getX();
            y -= def.getY();
            switch (def.getRotation()) {
                case 0:
                    return attachComponentAt(x, y, side);
                case 1:
                    return attachComponentAt(y, def.getHeight() - x - 1, (side + 3) % 4);
                case 2:
                    return attachComponentAt(def.getWidth() - x - 1, def.getHeight() - y - 1, (side + 2) % 4);
                case 3:
                    return attachComponentAt(def.getHeight() - y - 1, x, (side + 1) % 4);
                default:
                    throw new IllegalArgumentException("side must be between 0 and 3");
            }
        }

        public Component getComponent() {
            return Component.this;
        }
    }
}

