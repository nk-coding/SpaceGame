package com.nkcoding.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentType;

import java.util.HashMap;

abstract class ShipWidget extends Widget {
    //default size of a single component
    public static final float COMPONENT_SIZE = 40f;
    //AssetManager to load ComponentDef textures
    protected final ExtAssetManager assetManager;
    //HashMap with all textures for the different components
    protected final HashMap<ComponentType, Texture> componentTextureMap = new HashMap<>();
    //the zoom
    protected float zoom = 1f;
    //the Texture drawn when selecting something or if a component is selected
    protected Texture selection;
    //region implementation of Zoomable
    //where should it start to draw
    protected int startDrawX = 0;
    protected int startDrawY = 0;
    //where how much should it draw
    protected int amountDrawX;
    protected int amountDrawY;
    //the Texture shown when no component would be at this place
    protected Texture noComponent;

    public ShipWidget(ExtAssetManager assetManager, Texture selection, Texture noComponent) {
        this.assetManager = assetManager;
        this.selection = selection;
        this.noComponent = noComponent;
    }

    private Texture getComponentTexture(ComponentDef def) {
        if (!componentTextureMap.containsKey(def.getType())) {
            componentTextureMap.put(def.getType(), assetManager.getTexture(def.getDefaultTexture()));
        }
        return componentTextureMap.get(def.getType());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        //draw all components
        float componentSize = COMPONENT_SIZE * zoom;
        for (int x = startDrawX; x < (startDrawX + amountDrawX); x++) {
            for (int y = startDrawY; y < (startDrawY + amountDrawY); y++) {
                ComponentDef def = getComponentAt(x, y);

                if (def != null) {
                    if (((def.getX() == x && def.getY() == y) ||        //if it is set at the current position or
                            ((x == startDrawX && y == startDrawY) ||        //it is in the bottom left corner or
                                    (x == startDrawX && def.getY() == y) ||         //it is the left line and the y pos fits or
                                    (y == startDrawY && def.getX() == x)))) {       //it is bottom line and the x pos fits
                        Texture texture = getComponentTexture(def);
                        if (getDrawMode(def) == DrawMode.SELECTED) {
                            //draw selection and a smaller component
                            batch.draw(selection, getX() + def.getX() * componentSize, getY() + def.getY() * componentSize,
                                    def.getRealWidth() * componentSize, def.getRealHeight() * componentSize);
                            switch (def.getRotation()) {
                                case 0:
                                    batch.draw(texture,
                                            getX() + def.getX() * componentSize + 0.1f * componentSize,
                                            getY() + def.getY() * componentSize + 0.1f * componentSize,
                                            0f, 0f,
                                            (def.getWidth() - 0.2f) * componentSize, (def.getHeight() - 0.2f) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                                case 1:
                                    batch.draw(texture,
                                            getX() + (def.getX() + def.getRealWidth()) * componentSize - 0.1f * componentSize,
                                            getY() + def.getY() * componentSize + 0.1f * componentSize,
                                            0f, 0f,
                                            (def.getWidth() - 0.2f) * componentSize, (def.getHeight() - 0.2f) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                                case 2:
                                    batch.draw(texture,
                                            getX() + (def.getX() + def.getRealWidth()) * componentSize - 0.1f * componentSize,
                                            getY() + (def.getY() + def.getRealHeight()) * componentSize - 0.1f * componentSize,
                                            0f, 0f,
                                            (def.getWidth() - 0.2f) * componentSize, (def.getHeight() - 0.2f) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                                case 3:
                                    batch.draw(texture,
                                            getX() + def.getX() * componentSize + 0.1f * componentSize,
                                            getY() + (def.getY() + def.getRealHeight()) * componentSize - 0.1f * componentSize,
                                            0f, 0f,
                                            (def.getWidth() - 0.2f) * componentSize, (def.getHeight() - 0.2f) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                            }

                        } else {
                            //draw component normal
                            switch (def.getRotation()) {
                                case 0:
                                    batch.draw(texture,
                                            getX() + def.getX() * componentSize,
                                            getY() + def.getY() * componentSize,
                                            0f, 0f,
                                            (def.getWidth()) * componentSize, (def.getHeight()) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                                case 1:
                                    batch.draw(texture,
                                            getX() + (def.getX() + def.getRealWidth()) * componentSize,
                                            getY() + def.getY() * componentSize,
                                            0f, 0f,
                                            (def.getWidth()) * componentSize, (def.getHeight()) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                                case 2:
                                    batch.draw(texture,
                                            getX() + (def.getX() + def.getRealWidth()) * componentSize,
                                            getY() + (def.getY() + def.getRealHeight()) * componentSize,
                                            0f, 0f,
                                            (def.getWidth()) * componentSize, (def.getHeight()) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                                case 3:
                                    batch.draw(texture,
                                            getX() + def.getX() * componentSize,
                                            getY() + (def.getY() + def.getRealHeight()) * componentSize,
                                            0f, 0f,
                                            (def.getWidth()) * componentSize, (def.getHeight()) * componentSize,
                                            1f, 1f, def.getRotation() * 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
                                    break;
                            }
                        }
                    }
                } else if (noComponent != null) {
                    //draw the noComponent Texture
                    batch.draw(noComponent, getX() + x * componentSize, getY() + y * componentSize, componentSize, componentSize);
                }
            }
        }
    }

    protected abstract ComponentDef getComponentAt(int x, int y);

    protected abstract DrawMode getDrawMode(ComponentDef componentDef);

    enum DrawMode {NORMAL, SELECTED};

}
