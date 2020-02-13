package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.Disposable;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.components.Component;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDefBase;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ShipDesigner extends ShipWidget implements Zoomable, Disposable {

    //helper
    private ShipDef.ShipDesignerHelper designerHelper;
    private List<ComponentDef> selectedComponents = new LinkedList<>();
    //endregion
    //Consumer for when the selection changed
    private BiConsumer<List<ComponentDef>, List<ComponentDef>> selectionChanged;

    private List<ComponentDef> componentClipboard = new LinkedList<>();
    private Clipboard clipboard;

    //constructor with a shipDef
    public ShipDesigner(ShipDef shipDef, ExtAssetManager assetManager, Texture noComponent, Texture selection,
                        BiConsumer<List<ComponentDef>, List<ComponentDef>> selectionChanged) {
        super(assetManager, selection, noComponent);
        //ShipDef that contains all ComponentDefs
        this.selectionChanged = selectionChanged;
        this.designerHelper = shipDef.getShipDesignerHelper();
        clipboard = Gdx.app.getClipboard();

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
                    ComponentDef def = getComponentAt(x, y);
                    if (def != null && !selectedComponents.contains(def)) {
                        clearSelectedComponents();
                        addSelectedComponent(def);
                    } else if (def == null) {
                        clearSelectedComponents();
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
            case Input.Keys.C:
                clipboard.setContents("");
                componentClipboard = selectedComponents.stream().map(ComponentDef::new).collect(Collectors.toList());
                return true;
            case Input.Keys.V:
                if (clipboard.getContents().equals("") && !componentClipboard.isEmpty()) {
                    Vector2 pos = screenToLocalCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                    int posX = calculateXIndex(pos.x);
                    int posY = calculateYIndex(pos.y);
                    int offsetX = componentClipboard.stream().mapToInt(ComponentDefBase::getX).min().orElseThrow();
                    int offsetY = componentClipboard.stream().mapToInt(ComponentDefBase::getY).min().orElseThrow();

                    if (drag(componentClipboard, posX - offsetX, posY - offsetY )) {
                        drop(componentClipboard, posX - offsetX, posY - offsetY);
                        componentClipboard = selectedComponents.stream().map(ComponentDef::new).collect(Collectors.toList());
                    }
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    public List<ComponentDef> getSelectedComponents() {
        return selectedComponents;
    }

    public void addSelectedComponent(ComponentDef def) {
        List<ComponentDef> currentSelected = new ArrayList<>(selectedComponents);
        currentSelected.add(def);
        updateSelectedComponents(currentSelected);
    }

    private void removeSelectedComponent(ComponentDef def) {
        List<ComponentDef> currentSelected = new ArrayList<>(selectedComponents);
        currentSelected.remove(def);
        updateSelectedComponents(currentSelected);
    }

    public void clearSelectedComponents() {
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
    public boolean drag(List<ComponentDef> components, int offsetX, int offsetY) {
        return components.stream().allMatch(
                def -> designerHelper.tryMoveComponent(def,
                        def.getX() + offsetX, def.getY() + offsetY, def.getRotation(), components)
        );
    }

    //drops at the position
    public void drop(List<ComponentDef> components, int offsetX, int offsetY) {
        for (ComponentDef def : components) {
            designerHelper.moveComponent(def, def.getX() + offsetX, def.getY() + offsetY, def.getRotation());
        }
    }

    //removes a component
    public void removeComponent(ComponentDef def) {
        designerHelper.removeComponent(def);
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
    protected DrawMode getDrawMode(ComponentDef def, int x, int y) {
        if (((def.getX() == x && def.getY() == y) ||        //if it is set at the current position or
                ((x == startDrawX && y == startDrawY) ||        //it is in the bottom left corner or
                        (x == startDrawX && def.getY() == y) ||         //it is the left line and the y pos fits or
                        (y == startDrawY && def.getX() == x)))) {
            return selectedComponents.contains(def) ? DrawMode.SELECTED : DrawMode.NORMAL;
        } else {
            return null;
        }
    }
}
