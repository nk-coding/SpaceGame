package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.ComponentUpdateID;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.RadiusTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.ShieldTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.UpdateComponentTransmission;
import com.nkcoding.spacegame.simulation.spaceship.properties.FloatProperty;
import com.nkcoding.spacegame.simulation.spaceship.properties.VirtualProperty;

public class ShieldGenerator extends Buffer {
    public static final String IS_ENABLED_KEY = "IsEnabled";
    public static final String RADIUS_KEY = "Radius";

    private static final int DAMAGE_SHIELD = 1;

    private Fixture shieldFixture;

    /**
     * is the shield set by the user to be activated
     */
    protected boolean isEnabled = false;

    private float radius = -1;

    /**
     * mirror constructor
     */
    protected ShieldGenerator(ComponentDefBase defBase, Ship ship) {
        super(defBase, ship);
        ShieldComponentDef shieldComponentDef = (ShieldComponentDef) defBase;
        this.radius = shieldComponentDef.radius;
        this.isEnabled = shieldComponentDef.isEnabled;
    }

    /**
     * original constructor
     */
    protected ShieldGenerator(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }

    @Override
    public void draw(Batch batch, boolean isOriginal) {
        super.draw(batch, isOriginal);

        float currentRadius = radius;
        if (isEnabled)
            drawTexture(batch, getAssetManager().getTexture(Asset.VerySimpleExplosion),
                    new Vector2(0.1f - currentRadius * 0.01f, 0.1f - currentRadius * 0.01f), 2 * currentRadius * 0.01f, 2 * currentRadius * 0.01f, 0f);
    }

    @Override
    public void addFixtures() {
        super.addFixtures();
        //generate shield fixture
        CircleShape circleShape = new CircleShape();
        if (radius != -1) circleShape.setRadius(radius * 0.01f);
        circleShape.setPosition(new Vector2((getComponentDef().getX() + 1) * 0.1f, (getComponentDef().getY() + 1) * 0.1f));
        shieldFixture = getShip().getBody().createFixture(circleShape, 0f);
        shieldFixture.setSensor(true);
        shieldFixture.setUserData(this);
    }

    @Override
    public void removeFixtures() {
        super.removeFixtures();
        removeFixture(shieldFixture);
        shieldFixture = null;
    }

    @Override
    public void receiveTransmission(UpdateComponentTransmission transmission) {
        switch (transmission.updateID) {
            case ComponentUpdateID.RADIUS:
                RadiusTransmission radiusTransmission = (RadiusTransmission)transmission;
                radius = radiusTransmission.radius;
                if (shieldFixture != null) {
                    shieldFixture.getShape().setRadius(radius * 0.01f);
                }
                break;
            case ComponentUpdateID.SHIELD:
                ShieldTransmission shieldTransmission = (ShieldTransmission)transmission;
                isEnabled = shieldTransmission.shieldEnabled;
                break;
            default:
                super.receiveTransmission(transmission);
                break;
        }
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new ShieldGeneratorModel(shipModel, componentDef);
    }

    @Override
    public ComponentDefBase getMirrorData() {
        return new ShieldComponentDef(defBase, radius, isEnabled);
    }

    @Override
    protected int getDamageID(Fixture fixture) {
        if (fixture == shieldFixture) return DAMAGE_SHIELD;
        else return super.getDamageID(fixture);
    }

    public class ShieldGeneratorModel extends BufferModel {

        /**
         * is the shield internally set to be enabled
         */
        private boolean isInternalEnabled = false;

        /**
         * is set by the user to activated
         */
        private boolean isSetEnabled = false;

        /**
         * property to check / set if the shield is enabled
         * virtual, because the shield also must have enough power to activate
         */
        public final VirtualProperty<Boolean> isEnabled = register(new VirtualProperty<>(true, false, IS_ENABLED_KEY) {
            @Override
            public void set(Boolean value) {
                isSetEnabled = value;
            }

            @Override
            public Boolean get2() {
                return isInternalEnabled && isSetEnabled;
            }

            @Override
            public void setInitValue(String value) {
                set(value.equals("true"));
            }
        });

        //check if the radius changed
        private boolean radiusChanged = true;

        public final FloatProperty radius = register(new FloatProperty(false, true, RADIUS_KEY) {
            @Override
            public void set(float value) {
                super.set(value < 0 ? 0 : value > 100 ? 100 : value);
                radiusChanged = true;
            }
        });

        public ShieldGeneratorModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef, 300, 30);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (radiusChanged) {
                post(new RadiusTransmission(ShieldGenerator.this, radius.get()));
                radiusChanged = false;
            }

            //check if it is still activated
            float buffer = bufferLevel.get();
            if (buffer < 20 && isInternalEnabled) {
                isInternalEnabled = false;
            } else if (buffer > 20 && !isInternalEnabled && isSetEnabled) {
                isInternalEnabled = true;
            }

            if ((isInternalEnabled && isSetEnabled) != ShieldGenerator.this.isEnabled) {
                post(new ShieldTransmission(ShieldGenerator.this, isInternalEnabled && isSetEnabled));
            }
        }

        @Override
        public boolean damageAt(int damageID, int damage) {
            if (damageID == DAMAGE_SHIELD) {
                if (isInternalEnabled && isSetEnabled) {
                    bufferLevel.set(bufferLevel.get() - damage);
                    return true;
                } else {
                    return false;
                }
            } else {
                return super.damageAt(damageID, damage);
            }
        }
    }

    private static class ShieldComponentDef extends ComponentDefBase {
        public final float radius;
        public final boolean isEnabled;

        public ShieldComponentDef(ComponentDefBase toCopy, float radius, boolean isEnabled) {
            super(toCopy);
            this.radius = radius;
            this.isEnabled = isEnabled;
        }
    }

}
