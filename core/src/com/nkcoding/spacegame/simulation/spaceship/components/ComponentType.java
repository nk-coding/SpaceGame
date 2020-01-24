package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyData;
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
import static com.nkcoding.spacegame.simulation.spaceship.components.ShieldGenerator.IS_ENABLED_KEY;
import static com.nkcoding.spacegame.simulation.spaceship.components.ShieldGenerator.RADIUS_KEY;

public enum ComponentType {
    Engine((short)0, Engine::new, Engine::new, 1, 2, 100, 100, Asset.Engine,
            new ExternalPropertyData(ENGINE_POWER_KEY, DataType.INTEGER)),
    Cannon((short) 1, Cannon::new, Cannon::new, 1, 2, 100, 100, Asset.Cannon,
            new ExternalPropertyData(IS_SHOOTING_KEY, DataType.BOOLEAN),
            new ExternalPropertyData(BUFFER_LEVEL_KEY, DataType.FLOAT, false, true, false)),
    PowerCore((short) 2, PowerCore::new, PowerCore::new, 2, 2, 200, 500, Asset.PowerCore),
    BasicHull((short) 3, BasicHull::new, BasicHull::new, Asset.BasicHull),
    ExplosiveCanister((short) 4, ExplosiveCanister::new, ExplosiveCanister::new, 1, 1, 50, 50, Asset.ExplosiveCanister,
            new ExternalPropertyData(EXPLODE_KEY, DataType.BOOLEAN)),
    ShieldGenerator((short) 5, ShieldGenerator::new, ShieldGenerator::new, 2, 2, 200, 100, Asset.CloseSymbol,
            new ExternalPropertyData(RADIUS_KEY, DataType.FLOAT),
            new ExternalPropertyData(IS_ENABLED_KEY, DataType.BOOLEAN),
            new ExternalPropertyData(BUFFER_LEVEL_KEY, DataType.FLOAT, false, true, false)),
    ComputeCore((short) 6, ComputeCore::new, ComputeCore::new, 2, 2, 400, 800, Asset.ComputeCore,
            new ExternalPropertyData(KEY_DOWN_KEY, DataType.STRING, false, false, true),
            new ExternalPropertyData(KEY_UP_KEY, DataType.STRING, false, false, true),
            new ExternalPropertyData(ANGULAR_VELOCITY_KEY, DataType.FLOAT, false, true, false),
            new ExternalPropertyData(VELOCITY_KEY, DataType.FLOAT, false, true, false),
            new ExternalPropertyData(CAMERA_FOCUS_KEY, DataType.BOOLEAN));

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
    public final ExternalPropertyData[] propertyDefs;
    //the mass of the Component
    public final float mass;
    private PolygonShape shape;

    private final short index;

    ComponentType(short index, TriFunction<ComponentDef, Ship, Ship.ShipModel, ? extends Component> constructor,
                  IOTriFunction<ComponentDefBase, DataInputStream, Ship, ? extends Component> deserializer,
                  int width, int height,
                  int health, float mass, Asset defaultTexture,
                  ExternalPropertyData... propertyDefs) {
        ExternalPropertyData[] newPropertyDefs = new ExternalPropertyData[propertyDefs.length + 5];
        System.arraycopy(propertyDefs, 0, newPropertyDefs, 5, propertyDefs.length);
        newPropertyDefs[0] = new ExternalPropertyData(HEALTH_KEY, DataType.FLOAT, false);
        newPropertyDefs[1] = new ExternalPropertyData(POWER_REQUESTED_KEY, DataType.FLOAT, false);
        newPropertyDefs[2] = new ExternalPropertyData(REQUEST_LEVEL_KEY, DataType.INTEGER, true);
        newPropertyDefs[3] = new ExternalPropertyData(HAS_FULL_POWER_KEY, DataType.BOOLEAN, false);
        newPropertyDefs[4] = new ExternalPropertyData(POWER_RECEIVED_KEY, DataType.FLOAT, false);
        this.propertyDefs = newPropertyDefs;
        this.constructor = constructor;
        this.deserializer = deserializer;
        this.defaultTexture = defaultTexture;
        this.width = width;
        this.height = height;
        this.health = health;
        this.mass = mass;
        this.index = index;
    }

    //sets width and height to 1
    ComponentType(short index, TriFunction<ComponentDef, Ship, Ship.ShipModel, ? extends Component> constructor,
                  IOTriFunction<ComponentDefBase, DataInputStream, Ship, ? extends Component> deserializer,
                  Asset previewImg, ExternalPropertyData... propertyDefs) {
        this(index, constructor, deserializer, 1, 1, 100, 100, previewImg, propertyDefs);
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
            default:
                throw new IllegalStateException();
        }
    }

    public void serialize(DataOutputStream outputStream) throws IOException{
        outputStream.writeInt(index);
    }
}
