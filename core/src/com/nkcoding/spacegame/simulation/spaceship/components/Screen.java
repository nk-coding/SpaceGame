package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.ColorTransmission;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.ComponentUpdateID;
import com.nkcoding.spacegame.simulation.spaceship.components.communication.UpdateComponentTransmission;
import com.nkcoding.spacegame.simulation.spaceship.properties.IntProperty;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Screen extends Component {
    public static final String COLOR_KEY = "Color";

    private int color = 0;
    private Color textureColor = new Color();

    private final Texture screenTexture;
    private final Texture borderTexture;

    /**
     * mirror constructor
     */
    protected Screen(ComponentDefBase componentDef, DataInputStream inputStream, Ship ship) throws IOException {
        super(componentDef, ship);
        setColor(inputStream.readInt());
        screenTexture = getAssetManager().getTexture(Asset.Screen);
        borderTexture = getAssetManager().getTexture(Asset.ComponentBorder);
    }

    /**
     * original constructor
     */
    protected Screen(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
        screenTexture = getAssetManager().getTexture(Asset.Screen);
        borderTexture = getAssetManager().getTexture(Asset.ComponentBorder);
    }

    @Override
    public void receiveTransmission(UpdateComponentTransmission transmission) {
        if (transmission.componentUpdateID == ComponentUpdateID.COLOR) {
            setColor(((ColorTransmission)transmission).color);
        } else {
            super.receiveTransmission(transmission);
        }
    }

    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        super.serialize(outputStream);
        outputStream.writeInt(color);
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new ScreenComponentModel(shipModel, componentDef);
    }

    @Override
    public void draw(Batch batch, boolean original) {
        drawComponentTexture(batch, borderTexture);
        batch.setColor(textureColor);
        drawComponentTexture(batch, screenTexture);
        batch.setColor(Color.WHITE);
    }

    private void setColor(int color) {
        this.color = color;
        textureColor.set(color << 8 | 0x000000ff);
    }

    public class ScreenComponentModel extends ComponentModel{
        private boolean colorChanged = false;

        public IntProperty color = register(new IntProperty(COLOR_KEY) {
            @Override
            public void set(int value) {
                int oldValue = this.get();
                value = MathUtils.clamp(0, value, 0xffffff);
                super.set(value);
                if (oldValue != value) {
                    colorChanged = true;
                }
            }
        });

        public ScreenComponentModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (colorChanged) {
                post(new ColorTransmission(Screen.this, color.get()));
                colorChanged = false;
            }
        }
    }
}
