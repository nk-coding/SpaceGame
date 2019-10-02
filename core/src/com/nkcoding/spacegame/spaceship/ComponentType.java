package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.interpreter.compiler.TypeNamePair;
import com.nkcoding.spacegame.Asset;

import java.util.function.BiFunction;

import static com.nkcoding.spacegame.spaceship.ExternalPropertyData.of;
import static com.nkcoding.spacegame.spaceship.Component.*;

public enum ComponentType {
    TestType(TestImp::new, 2, 1, 100, 100, Asset.Badlogic),
    BasicHull(TestImp::new, Asset.BasicHull);

    //the width of the component
    public final int width;

    //the height of the component
    public final int height;

    private PolygonShape normalShape;
    private PolygonShape rotatedShape;

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
        System.arraycopy(propertyDefs, 0, newPropertyDefs,4, propertyDefs.length);
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
    public PolygonShape getShape(boolean rotated) {
        if (rotated) {
            if (rotatedShape == null) {
                rotatedShape = new PolygonShape();
                rotatedShape.setAsBox(ShipDef.UNIT_SIZE * height, ShipDef.UNIT_SIZE * width);
            }
            return rotatedShape;
        }
        else {
            if (normalShape == null) {
                normalShape = new PolygonShape();
                normalShape.setAsBox(ShipDef.UNIT_SIZE * width, ShipDef.UNIT_SIZE * height);
            }
            return normalShape;
        }
    }
}
