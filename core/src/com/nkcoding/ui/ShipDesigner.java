package com.nkcoding.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Disposable;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class ShipDesigner extends ShipWidget implements Zoomable, Disposable {

    //helper
    private ShipDef.ShipDesignerHelper designerHelper;
    private List<ComponentDef> selectedComponents;
    //endregion
    //Consumer for when the selection changed
    private BiConsumer<List<ComponentDef>, List<ComponentDef>> selectionChanged;

    //constructor with a shipDef
    public ShipDesigner(ShipDef shipDef, ExtAssetManager assetManager, Texture noComponent, Texture selection,
                        BiConsumer<List<ComponentDef>, List<ComponentDef>> selectionChanged) {
        super(assetManager, selection, noComponent);
        //ShipDef that contains all ComponentDefs
        this.selectionChanged = selectionChanged;

        this.designerHelper = shipDef.getShipDesignerHelper();


        //capture touch events
        addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (UIUtils.shift()) {
                    ComponentDef def = getComponentAt(x, y);
                    if (def != null) {
                        toggleSelectedComponent(getComponentAt(x, y));
                    }
                } else {
                    clearSelectedComponents();
                    ComponentDef def = getComponentAt(x, y);
                    if (def != null) {
                        addSelectedComponent(def);
                    }
                }
                getStage().setKeyboardFocus(ShipDesigner.this);
                return true;
            }
        });

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return ShipDesigner.this.keyDown(keycode);
            }
        });


    }

    private boolean keyDown(int keyCode) {
        switch (keyCode) {
            case Input.Keys.DEL:
            case Input.Keys.FORWARD_DEL:

                if (getSelectedComponents().size() > 0) {
                    for (ComponentDef def : getSelectedComponents()) {
                        removeComponent(def);
                    }
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public List<ComponentDef> getSelectedComponents() {
        return selectedComponents;
    }

    private void addSelectedComponent(ComponentDef def) {
        List<ComponentDef> currentSelected = new ArrayList<>(selectedComponents);
        currentSelected.add(def);
        updateSelectedComponents(currentSelected);
    }

    private void removeSelectedComponent(ComponentDef def) {
        List<ComponentDef> currentSelected = new ArrayList<>(selectedComponents);
        currentSelected.remove(def);
        updateSelectedComponents(currentSelected);
    }

    private void clearSelectedComponents() {
        updateSelectedComponents(new LinkedList<>());
    }

    private void toggleSelectedComponent(ComponentDef def) {
        if (getSelectedComponents().contains(def)) {
            removeSelectedComponent(def);
        } else {
            addSelectedComponent(def);
        }
    }

    private void updateSelectedComponents(List<ComponentDef> newSelected) {
        List<ComponentDef> oldSelected = getSelectedComponents();
        this.selectedComponents = newSelected;
        selectionChanged.accept(newSelected, oldSelected);
    }

    private ComponentDef getComponentAt(float x, float y) {
        return designerHelper.getComponent(calculateXIndex(x), calculateYIndex(y));
    }

    /**
     * tries to rotate the selected component
     */
    public void rotateSelectedComponent() {
        //TODO support for multiple Components
        if (getSelectedComponents().size() == 1) {
            designerHelper.rotateComponent(getSelectedComponents().get(0));
        }
    }

    //checks if def could be placed there
    public boolean drag(ComponentDef def, float x, float y) {
        return designerHelper.tryMoveComponent(def, calculateXIndex(x), calculateYIndex(y), def.getRotation());
    }

    //drops at the position
    public void drop(ComponentDef def, float x, float y) {
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

    @Override
    protected ComponentDef getComponentAt(int x, int y) {
        return designerHelper.getComponent(x, y);
    }

    @Override
    protected DrawMode getDrawMode(ComponentDef componentDef) {
        return selectedComponents.contains(componentDef) ? DrawMode.SELECTED : DrawMode.NORMAL;
    }
}
