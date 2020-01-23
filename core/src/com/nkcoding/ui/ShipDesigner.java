package com.nkcoding.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentType;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class ShipDesigner extends Widget implements Zoomable, Disposable {

    //default size of a single component
    public static final float COMPONENT_SIZE = 50f;
    //AssetManager to load ComponentDef textures
    private final ExtAssetManager assetManager;
    //HashMap with all textures for the different components
    private final HashMap<ComponentType, Texture> componentTextureMap = new HashMap<>();
    //ShipDef that contains all ComponentDefs
    private ShipDef shipDef;
    //helper
    private ShipDef.ShipDesignerHelper designerHelper;
    private ComponentDef selectedComponent;
    //region implementation of Zoomable
    //where should it start to draw
    private int startDrawX = 0;
    private int startDrawY = 0;
    //where how much should it draw
    private int amountDrawX;
    private int amountDrawY;
    //the zoom
    private float zoom = 1f;
    //the Texture shown when no component would be at this place
    private Texture noComponent;
    //the Texture drawn when selecting something or if a component is selected
    private Texture selection;
    //endregion
    //Consumer for when the selection changed
    private BiConsumer<ComponentDef, ComponentDef> selectionChanged;

    //constructor with a shipDef
    public ShipDesigner(ShipDef shipDef, ExtAssetManager assetManager, Texture noComponent, Texture selection, BiConsumer<ComponentDef, ComponentDef> selectionChanged) {
        this.shipDef = shipDef;
        this.assetManager = assetManager;
        this.noComponent = noComponent;
        this.selection = selection;
        this.selectionChanged = selectionChanged;

        this.designerHelper = shipDef.getShipDesignerHelper();


        //capture touch events
        addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                setSelectedComponent(calculateXIndex(x), calculateYIndex(y));
                getStage().setKeyboardFocus(ShipDesigner.this);
                return true;
            }
        });

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return ShipDesigner.this.keyDown(event, keycode);
            }
        });


    }

    private boolean keyDown(InputEvent inputEvent, int keyCode) {
        switch (keyCode) {
            case Input.Keys.DEL:
            case Input.Keys.FORWARD_DEL:
                ComponentDef component = getSelectedComponent();
                if (component != null) {
                    removeComponent(component);
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public ComponentDef getSelectedComponent() {
        return selectedComponent;
    }

    public void setSelectedComponent(ComponentDef def) {
        ComponentDef oldSelected = getSelectedComponent();
        this.selectedComponent = def;
        ComponentDef newSelected = getSelectedComponent();
        if (oldSelected != newSelected) selectionChanged.accept(newSelected, oldSelected);
    }

    private void setSelectedComponent(int x, int y) {
        setSelectedComponent(designerHelper.getComponent(x, y));
    }

    private Texture getComponentTexture(ComponentDef def) {
        if (!componentTextureMap.containsKey(def.getType())) {
            componentTextureMap.put(def.getType(), assetManager.getTexture(def.getDefaultTexture()));
        }
        return componentTextureMap.get(def.getType());
    }

    /**
     * tries to rotate the selected component
     */
    public void rotateSelectedComponent() {
        designerHelper.rotateComponent(getSelectedComponent());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        //TODO draw background
        //draw all components
        float componentSize = COMPONENT_SIZE * zoom;
        for (int x = startDrawX; x < (startDrawX + amountDrawX); x++) {
            for (int y = startDrawY; y < (startDrawY + amountDrawY); y++) {
                ComponentDef def = designerHelper.getComponent(x, y);

                if (def != null) {
                    if (((def.getX() == x && def.getY() == y) ||        //if it is set at the current position or
                            ((x == startDrawX && y == startDrawY) ||        //it is in the bottom left corner or
                                    (x == startDrawX && def.getY() == y) ||         //it is the left line and the y pos fits or
                                    (y == startDrawY && def.getX() == x)))) {       //it is bottom line and the x pos fits
                        Texture texture = getComponentTexture(def);
                        if (def == getSelectedComponent()) {
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
                } else {
                    //draw the noComponent Texture
                    batch.draw(noComponent, getX() + x * componentSize, getY() + y * componentSize, componentSize, componentSize);
                }
            }
        }
    }

    //checks if def could be placed there
    public boolean drag(ComponentDef def, float x, float y, int pointer) {
        return designerHelper.tryMoveComponent(def, calculateXIndex(x), calculateYIndex(y), def.getRotation());
    }

    //drops at the position
    public void drop(ComponentDef def, float x, float y, int pointer) {
        designerHelper.moveComponent(def, calculateXIndex(x), calculateYIndex(y), def.getRotation());
    }

    //removes a component
    public void removeComponent(ComponentDef def) {
        designerHelper.removeComponent(def);
    }

    //calculates the x index
    private int calculateXIndex(float x) {
        return (int) (x / (COMPONENT_SIZE * zoom));
    }

    //calculates the y index
    private int calculateYIndex(float y) {
        return (int) (y / (COMPONENT_SIZE * zoom));
    }

    @Override
    public void setCullingArea(Rectangle cullingArea, float zoom) {
        if (this.zoom != zoom) {
            this.zoom = zoom;
            invalidateHierarchy();
        }
        float componentSize = COMPONENT_SIZE * zoom;
        startDrawX = Math.max((int) (cullingArea.x / componentSize), 0);
        startDrawY = Math.max((int) (cullingArea.y / componentSize), 0);
        amountDrawX = Math.min(ShipDef.MAX_SIZE - startDrawX, (int) (cullingArea.width / componentSize) + 2);
        amountDrawY = Math.min(ShipDef.MAX_SIZE - startDrawY, (int) (cullingArea.height / componentSize) + 2);
        //it seems that it is not necessary to call invalidate because setWidth calls sizeChanged which calls invalidate
    }

    @Override
    public float getPrefWidth() {
        return zoom * COMPONENT_SIZE * ShipDef.MAX_SIZE;
    }

    @Override
    public float getPrefHeight() {
        return zoom * COMPONENT_SIZE * ShipDef.MAX_SIZE;
    }

    @Override
    public void dispose() {
        componentTextureMap.clear();
    }
}
