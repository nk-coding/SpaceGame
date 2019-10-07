package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.spaceship.Engine;

import java.util.function.BiFunction;

import static com.nkcoding.spacegame.spaceship.Component.*;
import static com.nkcoding.spacegame.spaceship.ExternalPropertyData.*;

public enum ComponentType {
    TestType(TestImp::new, 2, 3, 100, 100, Asset.Badlogic),
    Engine(Engine::new, 1, 2, 100, 100, Asset.ErrorSymbol,
            of(com.nkcoding.spacegame.spaceship.Engine.EnginePowerKey, DataTypes.Integer, false)),
    BasicHull(TestImp::new, Asset.BasicHull);

    //the width of the component
    public final int width;

    //the height of the component
    public final int height;

    private PolygonShape shape;

    //the start health
    //this should never be a negative value or zero
    //the component will be destroyed if health reaches zero
    public final int health;

    //constructor to create a new instance
    public final BiFunction<ComponentDef, Ship, ? extends Component> constructor;

    //file position of the preview image
    public final Asset previewImg;

    //array with all the keys for the ExternalProperties
    public final ExternalPropertyData[] propertyDefs;

    //the mass of the Component
    public final float mass;

    ComponentType(BiFunction<ComponentDef, Ship, ? extends Component> constructor, int width, int height, int health, float mass, Asset previewImg,
                  ExternalPropertyData... propertyDefs) {
        ExternalPropertyData[] newPropertyDefs = new ExternalPropertyData[propertyDefs.length + 5];
        System.arraycopy(propertyDefs, 0, newPropertyDefs,5, propertyDefs.length);
        newPropertyDefs[0] = new ExternalPropertyData(HealthKey, DataTypes.Float);
        newPropertyDefs[1] = new ExternalPropertyData(PowerRequestedKey, DataTypes.Float);
        newPropertyDefs[2] = new ExternalPropertyData(RequestLevelKey, DataTypes.Integer, false);
        newPropertyDefs[3] = new ExternalPropertyData(HasFullPowerKey, DataTypes.Boolean);
        newPropertyDefs[4] = new ExternalPropertyData(PowerReceivedKey, DataTypes.Float);
        this.propertyDefs = newPropertyDefs;
        this.constructor = constructor;
        this.previewImg = previewImg;
        this.width = width;
        this.height = height;
        this.health = health;
        this.mass = mass;
    }

    //sets width and height to 1
    ComponentType(BiFunction<ComponentDef, Ship, ? extends Component> constructor, Asset previewImg, ExternalPropertyData... propertyDefs) {
        this(constructor, 1, 1, 100, 100, previewImg, propertyDefs);
    }

    /**
     * returns the correct Shape for the Component
     * @param rotated true if rotation == 90° || 270°
     * @return the PolygonShape representing this Component
     */
    public PolygonShape getShape(boolean rotated, int posX, int posY) {
        if (shape == null) shape = new PolygonShape();
        float w, h;
        if (rotated) {
            w = height;
            h = width;
        }
        else {
            w = width;
            h = height;
        }
        shape.setAsBox(w * ShipDef.UNIT_SIZE / 2, h * ShipDef.UNIT_SIZE / 2,
                new Vector2(ShipDef.UNIT_SIZE * (w / 2 + posX), ShipDef.UNIT_SIZE * (h / 2 + posY)),0);
        return shape;
    }
}
