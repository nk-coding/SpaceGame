package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertySpecification;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.util.IOTriFunction;
import com.nkcoding.util.TriFunction;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.nkcoding.spacegame.simulation.spaceship.components.Buffer.BUFFER_LEVEL_KEY;
import static com.nkcoding.spacegame.simulation.spaceship.components.Cannon.IS_SHOOTING_KEY;
import static com.nkcoding.spacegame.simulation.spaceship.components.Component.*;
import static com.nkcoding.spacegame.simulation.spaceship.components.ComputeCore.*;
import static com.nkcoding.spacegame.simulation.spaceship.components.Engine.ENGINE_POWER_KEY;
import static com.nkcoding.spacegame.simulation.spaceship.components.ExplosiveCanister.EXPLODE_KEY;
import static com.nkcoding.spacegame.simulation.spaceship.components.Sensors.IS_SCANNER_ENABLED;
import static com.nkcoding.spacegame.simulation.spaceship.components.ShieldGenerator.IS_ENABLED_KEY;
import static com.nkcoding.spacegame.simulation.spaceship.components.ShieldGenerator.RADIUS_KEY;
import static com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertySpecification.builder;

public enum ComponentType {
    Engine((short)0, Engine::new, Engine::new, 1, 2, 100, 100, 100, Asset.Engine,
            builder(ENGINE_POWER_KEY, DataType.INTEGER).read().write().changedHandler().concurrent().build()),
    Cannon((short) 1, Cannon::new, Cannon::new, 1, 2, 100, 100, 50, Asset.Cannon,
            builder(IS_SHOOTING_KEY, DataType.BOOLEAN).read().write().changedHandler().concurrent().build(),
            builder(BUFFER_LEVEL_KEY, DataType.FLOAT).read().build()),
    PowerCore((short) 2, PowerCore::new, PowerCore::new, 2, 2, 200, 500, -100, Asset.PowerCore),
    BasicHull((short) 3, BasicHull::new, BasicHull::new, Asset.BasicHull),
    ExplosiveCanister((short) 4, ExplosiveCanister::new, ExplosiveCanister::new, 1, 1, 50, 50, 0, Asset.ExplosiveCanister,
            builder(EXPLODE_KEY, DataType.BOOLEAN).read().write().changedHandler().concurrent().build()),
    ShieldGenerator((short) 5, ShieldGenerator::new, ShieldGenerator::new, 2, 2, 200, 100, 30, Asset.ShieldGenerator,
            builder(RADIUS_KEY, DataType.FLOAT).read().write().changedHandler().concurrent().build(),
            builder(IS_ENABLED_KEY, DataType.BOOLEAN).read().write().changedHandler().concurrent().build(),
            builder(BUFFER_LEVEL_KEY, DataType.FLOAT).read().build()),
    ComputeCore((short) 6, ComputeCore::new, ComputeCore::new, 2, 2, 400, 800, 0, Asset.ComputeCore,
            builder(KEY_DOWN_KEY, DataType.STRING).changedHandler().build(),
            builder(KEY_UP_KEY, DataType.STRING).changedHandler().build(),
            builder(ANGULAR_VELOCITY_KEY, DataType.FLOAT).read().build(),
            builder(VELOCITY_KEY, DataType.FLOAT).read().build(),
            builder(CAMERA_FOCUS_KEY, DataType.BOOLEAN).read().write().changedHandler().concurrent().build(),
            builder(INIT_CALLBACK_KEY, DataType.STRING).changedHandler().build(),
            builder(POSITION_KEY, DataType.fromName("[float x, float y]")).read().build(),
            builder(ROTATION_KEY, DataType.FLOAT).read().build()),
    Sensors((short) 7, Sensors::new, Sensors::new, 2, 1, 100, 100, 0, Asset.Sensors,
            builder(IS_SCANNER_ENABLED, DataType.BOOLEAN).read().write().changedHandler().concurrent().build());

    //the width of the component
    public final int width;

    //the height of the component
    public final int height;
    //the start health
    //this should never be a negative value or zero
    //the component will be destroyed if health reaches zero
    public final int health;
    //constructor to create a new instance
    public final TriFunction<ComponentDef, Ship, Ship.ShipModel, ? extends Component> constructor;
    //constructor to create a new instance on another client
    public final IOTriFunction<ComponentDefBase, DataInputStream, Ship, ? extends Component> deserializer;
    //file position of the preview image
    public final Asset defaultTexture;
    //array with all the keys for the ExternalProperties
    public final ExternalPropertySpecification[] propertySpecifications;
    //the mass of the Component
    public final float mass;
    //the maximum power level for the component
    //negative means that it delivers power
    public final float maxPowerLevel;
    private PolygonShape shape;

    private final short index;

    ComponentType(short index, TriFunction<ComponentDef, Ship, Ship.ShipModel, ? extends Component> constructor,
                  IOTriFunction<ComponentDefBase, DataInputStream, Ship, ? extends Component> deserializer,
                  int width, int height,
                  int health, float mass,
                  float maxPowerLevel, Asset defaultTexture,
                  ExternalPropertySpecification... propertySpecifications) {
        ExternalPropertySpecification[] newPropertyDefs = new ExternalPropertySpecification[propertySpecifications.length + 5];
        System.arraycopy(propertySpecifications, 0, newPropertyDefs, 5, propertySpecifications.length);
        newPropertyDefs[0] = builder(HEALTH_KEY, DataType.FLOAT).read().changedHandler().build();
        newPropertyDefs[1] = builder(POWER_REQUESTED_KEY, DataType.FLOAT).read().changedHandler().build();
        newPropertyDefs[2] = builder(REQUEST_LEVEL_KEY, DataType.INTEGER).read().write().changedHandler().concurrent().build();
        newPropertyDefs[3] = builder(HAS_FULL_POWER_KEY, DataType.BOOLEAN).read().changedHandler().build();
        newPropertyDefs[4] = builder(POWER_RECEIVED_KEY, DataType.FLOAT).read().changedHandler().build();
        this.propertySpecifications = newPropertyDefs;
        this.constructor = constructor;
        this.deserializer = deserializer;
        this.defaultTexture = defaultTexture;
        this.width = width;
        this.height = height;
        this.health = health;
        this.mass = mass;
        this.maxPowerLevel = maxPowerLevel;
        this.index = index;
    }

    //sets width and height to 1
    ComponentType(short index, TriFunction<ComponentDef, Ship, Ship.ShipModel, ? extends Component> constructor,
                  IOTriFunction<ComponentDefBase, DataInputStream, Ship, ? extends Component> deserializer,
                  float powerLevel,
                  Asset previewImg, ExternalPropertySpecification... propertySpecifications) {
        this(index, constructor, deserializer, 1, 1, 100, 100, powerLevel, previewImg, propertySpecifications);
    }

    ComponentType(short index, TriFunction<ComponentDef, Ship, Ship.ShipModel, ? extends Component> constructor,
                  IOTriFunction<ComponentDefBase, DataInputStream, Ship, ? extends Component> deserializer,
                  Asset previewImg, ExternalPropertySpecification... propertySpecifications) {
        this(index, constructor, deserializer, 0, previewImg, propertySpecifications);
    }

    /**
     * returns the correct Shape for the Component
     *
     * @param rotated true if rotation == 90° || 270°
     * @return the PolygonShape representing this Component
     */
    public PolygonShape getShape(boolean rotated, int posX, int posY) {
        if (shape == null) shape = new PolygonShape();
        float w, h;
        if (rotated) {
            w = height;
            h = width;
        } else {
            w = width;
            h = height;
        }
        shape.setAsBox(w * ShipDef.UNIT_SIZE / 2, h * ShipDef.UNIT_SIZE / 2,
                new Vector2(ShipDef.UNIT_SIZE * (w / 2 + posX), ShipDef.UNIT_SIZE * (h / 2 + posY)), 0);
        return shape;
    }

    public static ComponentType deserialize(DataInputStream inputStream) throws IOException {
        switch (inputStream.readInt()) {
            case 0:
                return Engine;
            case 1:
                return Cannon;
            case 2:
                return PowerCore;
            case 3:
                return BasicHull;
            case 4:
                return ExplosiveCanister;
            case 5:
                return ShieldGenerator;
            case 6:
                return ComputeCore;
            case 7:
                return Sensors;
            default:
                throw new IllegalStateException();
        }
    }

    public void serialize(DataOutputStream outputStream) throws IOException{
        outputStream.writeInt(index);
    }
}
