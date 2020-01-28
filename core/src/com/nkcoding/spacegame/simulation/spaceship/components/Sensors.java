package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.simulation.CoreUnit;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.properties.BooleanProperty;

import java.io.DataInputStream;

public class Sensors extends Component {

    public static final String IS_SCANNER_ENABLED = "IsScannerEnabled";

    /**
     * mirror constructor
     */
    protected Sensors(ComponentDefBase componentDef, DataInputStream inputStream, Ship ship) {
        super(componentDef, ship);
    }

    /**
     * original constructor
     */
    protected Sensors(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new SensorsModel(shipModel, componentDef);
    }

    private class SensorsModel extends ComponentModel {

        /**
         * property to check / set if the shield is enabled
         * virtual, because the shield also must have enough power to activate
         */
        public final BooleanProperty isScannerEnabled = register(new BooleanProperty(IS_SCANNER_ENABLED));

        private Texture arrow;

        public SensorsModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef);
            arrow = getAssetManager().getTexture(Asset.Arrow);
        }

        @Override
        public void draw(Batch batch) {
            if (isScannerEnabled.get()) {
                Vector2 shipCenter = getShip().localToWorldCoordinates(getShip().getCenterPosition());
                float shipRadius = getShip().getRadius();
                for (CoreUnit unit : getSpaceSimulation().getCoreUnits()) {
                    Vector2 unitCenter = unit.getWorldCenterPosition();
                    float deltaX = unitCenter.x - shipCenter.x;
                    float deltaY = unitCenter.y - shipCenter.y;
                    if (deltaX * deltaX + deltaY * deltaY < shipRadius * shipRadius) continue;
                    float phi = (float) Math.atan2(deltaY, deltaX);
                    batch.draw(this.arrow,
                            shipCenter.x + MathUtils.cos(phi) * shipRadius, shipCenter.y + MathUtils.sin(phi) * shipRadius,
                            ShipDef.UNIT_SIZE, ShipDef.UNIT_SIZE,
                            ShipDef.UNIT_SIZE * 2, ShipDef.UNIT_SIZE * 2,
                            1, 1,
                            phi * MathUtils.radiansToDegrees - 90,
                            0, 0,
                            this.arrow.getWidth(), this.arrow.getHeight(),
                            false, false);
                }
            }
        }
    }
}
