package com.nkcoding.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;
import com.nkcoding.spacegame.spaceship.ComponentDef;
import com.nkcoding.spacegame.spaceship.ComponentType;
import com.nkcoding.spacegame.spaceship.ShipDef;

import java.util.HashMap;

public class ShipDesigner extends Widget implements Zoomable, Disposable {

    //default size of a single component
    private static final float COMPONENT_SIZE = 50f;

    //ShipDef that contains all ComponentDefs
    private ShipDef shipDef;

    //AssetManager to load ComponentDef textures
    private final AssetManager assetManager;

    //helper
    private ShipDef.ShipDesignerHelper designerHelper;

    private int selectedComponentX = -1;
    private int selectedComponentY = -1;

    //region implementation of Zoomable
    //where should it start to draw
    private int startDrawX = 0;
    private int startDrawY = 0;

    public ComponentDef getSelectedComponent() {
        return (selectedComponentX >= 0 && selectedComponentY >= 0) ? designerHelper.getComponent(selectedComponentX, selectedComponentY) : null;
    }

    private void setSelectedComponent(int x, int y) {
        //TODO implementation
        System.out.println(String.format("x: %d, y: %d", x, y));
    }

    //where how much should it draw
    private int amountDrawX;
    private int amountDrawY;

    //the zoom
    private float zoom = 1f;
    //endregion

    //HashMap with all textures for the different components
    private final HashMap<ComponentType, Texture> componentTextureMap = new HashMap<>();

    private Texture getComponentTexture(ComponentDef def) {
        if (!componentTextureMap.containsKey(def.getType())) {
            componentTextureMap.put(def.getType(), assetManager.get(def.getPreviewImage()));
        }
        return componentTextureMap.get(def.getType());
    }

   //the Texture shown when no component would be at this place
   private Texture noComponent;


    //constructor with a shipDef
    public ShipDesigner(ShipDef shipDef, AssetManager assetManager, Texture noComponent) {
        this.shipDef = shipDef;
        this.assetManager = assetManager;
        this.noComponent = noComponent;

        this.designerHelper = shipDef.getShipDesignerHelper();

        //set width and height
        //setWidth(COMPONENT_SIZE * zoom * ShipDef.MAX_SIZE);
        //setHeight(COMPONENT_SIZE * zoom * ShipDef.MAX_SIZE);
        //it seems that it is not necessary to call invalidate because setWidth calls sizeChanged which calls invalidate

        //capture touch events
        addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                setSelectedComponent((int)(x / (COMPONENT_SIZE * zoom)), (int)(y / (COMPONENT_SIZE * zoom)));
                return true;
            }
        });
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
                        batch.draw(getComponentTexture(def), getX() + def.getX() * componentSize, getY() + getHeight() - (def.getY() + 1) * componentSize,
                                def.getWidth() * componentSize, def.getHeight() * componentSize);
                    }
                }
                else {
                    //draw the noComponent Texture
                    batch.draw(noComponent, getX() + x * componentSize, getY() + getHeight() - (y + 1) * componentSize, componentSize, componentSize);
                }
            }
        }
    }

    @Override
    public void setCullingArea(Rectangle cullingArea, float zoom) {
        if (this.zoom != zoom) {
            this.zoom = zoom;
            invalidateHierarchy();
        }
        float componentSize = COMPONENT_SIZE * zoom;
        startDrawX = Math.max((int)(cullingArea.x / componentSize), 0);
        startDrawY = Math.max((int)((getHeight() - cullingArea.y - cullingArea.height) / componentSize), 0);
        amountDrawX = Math.min(ShipDef.MAX_SIZE - startDrawX, (int)(cullingArea.width / componentSize) + 2);
        amountDrawY = Math.min(ShipDef.MAX_SIZE - startDrawY, (int)(cullingArea.height / componentSize) + 2);
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
