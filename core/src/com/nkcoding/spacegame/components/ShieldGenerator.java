package com.nkcoding.spacegame.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.spaceship.ComponentDef;
import com.nkcoding.spacegame.spaceship.FloatProperty;
import com.nkcoding.spacegame.spaceship.Ship;
import com.nkcoding.spacegame.spaceship.VirtualProperty;

public class ShieldGenerator extends Buffer {
    public static final String IS_ENABLED_KEY = "IsEnabled";
    public static final String RADIUS_KEY = "Radius";

    private Fixture shieldFixture;

    /**
     * is the shield set by the user to be activated
     */
    private boolean isSetEnabled = true;

    /**
     * is the shield internally set to be enabled
     */
    private boolean isInternalEnabled = false;

    /**
     * property to check / set if the shield is enabled
     * virtual, because the shield also must have enough power to activate
     */
    public final VirtualProperty<Boolean> isEnabled = register(new VirtualProperty<>(true, IS_ENABLED_KEY) {
        @Override
        public void set(Boolean value) {
            isSetEnabled = value;
        }

        @Override
        public Boolean get2() {
            return isInternalEnabled && isSetEnabled;
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

    /**
     * creates a new BufferComponent
     *
     * @param componentDef the definition for this Component
     * @param ship         the Ship for this Component
     */
    public ShieldGenerator(ComponentDef componentDef, Ship ship) {
        super(componentDef, ship, 300, 30);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (radiusChanged && shieldFixture != null) {
            shieldFixture.getShape().setRadius(radius.get() * 0.01f);
            radiusChanged = false;
        }

        //check if it is still activated
        float buffer = bufferLevel.get();
        if (buffer < 20 && isInternalEnabled) {
            isInternalEnabled = false;
        } else if (buffer > 20 && !isInternalEnabled && isSetEnabled) {
            isInternalEnabled = true;
        }
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);

        float currentRadius = radius.get();
        if (isSetEnabled && isInternalEnabled)
            drawTexture(batch, getAssetManager().getTexture(Asset.VerySimpleExplosion),
                    new Vector2(0.1f - currentRadius * 0.01f, 0.1f - currentRadius * 0.01f), 2 * currentRadius * 0.01f, 2 * currentRadius * 0.01f, 0f);
    }

    @Override
    public void addFixtures() {
        super.addFixtures();
        //generate shield fixture
        CircleShape circleShape = new CircleShape();
        if (radius != null) circleShape.setRadius(radius.get() * 0.01f);
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
    public boolean damageAt(Fixture fixture, int damage) {
        if (fixture == shieldFixture) {
            if (isInternalEnabled && isSetEnabled) {
                bufferLevel.set(bufferLevel.get() - damage);
                return true;
            } else {
                return false;
            }
        } else {
            return super.damageAt(fixture, damage);
        }
    }

}
