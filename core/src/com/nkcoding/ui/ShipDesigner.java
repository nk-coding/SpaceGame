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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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

    private Vector2 selectionStart = new Vector2();
    private Vector2 selectionEnd = new Vector2();
    private int draggingPointer = -1;

    private ShipDesignerStyle style;

    //constructor with a shipDef
    public ShipDesigner(ShipDef shipDef, ExtAssetManager assetManager, ShipDesignerStyle style,
                        BiConsumer<List<ComponentDef>, List<ComponentDef>> selectionChanged) {
        super(assetManager, style.selection, style.noComponent);
        //ShipDef that contains all ComponentDefs
        this.selectionChanged = selectionChanged;
        this.designerHelper = shipDef.getShipDesignerHelper();
        this.style = style;
        clipboard = Gdx.app.getClipboard();

        //capture touch events
        addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
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
                } else if (button == Input.Buttons.RIGHT && draggingPointer == -1) {
                    System.out.println("x: " + x + ", y: " + y);
                    selectionStart = new Vector2(x, y);
                    selectionEnd.set(x, y);
                    draggingPointer = pointer;
                    return true;
                } else  {
                    return false;
                }
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (pointer == draggingPointer) {
                    selectionEnd.set(x, y);
                } else {
                    super.touchDragged(event, x, y, pointer);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == draggingPointer) {
                    draggingPointer = -1;
                    int xStart = calculateXIndex(Math.min(selectionStart.x, selectionEnd.x));
                    int xEnd = calculateXIndex(Math.max(selectionStart.x, selectionEnd.x));
                    int yStart = calculateYIndex(Math.min(selectionStart.y, selectionEnd.y));
                    int yEnd = calculateYIndex(Math.max(selectionStart.y, selectionEnd.y));
                    updateSelectedComponents(designerHelper.getComponents().stream()
                            .filter(c -> c.getX() > xStart && c.getX() + c.getRealWidth() <= xEnd
                                    && c.getY() > yStart && c.getY() + c.getRealHeight() <= yEnd)
                            .collect(Collectors.toList()));
                }
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
                if (UIUtils.ctrl()) {
                    clipboard.setContents("");
                    componentClipboard = selectedComponents.stream().map(ComponentDef::new).collect(Collectors.toList());
                    return true;
                }
                return false;
            case Input.Keys.V:
                if ("".equals(clipboard.getContents()) && !componentClipboard.isEmpty() && UIUtils.ctrl()) {
                    Vector2 pos = screenToLocalCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                    int posX = calculateXIndex(pos.x);
                    int posY = calculateYIndex(pos.y);
                    int offsetX = componentClipboard.stream().mapToInt(ComponentDefBase::getX).min().orElseThrow();
                    int offsetY = componentClipboard.stream().mapToInt(ComponentDefBase::getY).min().orElseThrow();

                    if (drag(componentClipboard, posX - offsetX, posY - offsetY )) {
                        drop(componentClipboard, posX - offsetX, posY - offsetY);
                        componentClipboard = componentClipboard.stream().map(ComponentDef::new).collect(Collectors.toList());
                    }
                    return true;
                } else {
                    return false;
                }
            case Input.Keys.A:
                if (UIUtils.ctrl()) {
                    updateSelectedComponents(new ArrayList<>(designerHelper.getComponents()));
                    return true;
                }
                return false;
            case Input.Keys.R:
                rotateSelectedComponent();
                return true;
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
        this.getStage().setKeyboardFocus(this);
        updateSelectedComponents(new ArrayList<>(components));
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
    public float getUnscaledWidth() {
        return COMPONENT_SIZE * ShipDef.MAX_SIZE;
    }

    @Override
    public float getUnscaledHeight() {
        return COMPONENT_SIZE * ShipDef.MAX_SIZE;
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (draggingPointer != -1) {
            style.boxSelection.draw(batch,
                    getDrawX() + selectionStart.x, getDrawY() + selectionStart.y,
                    selectionEnd.x - selectionStart.x, selectionEnd.y - selectionStart.y);
        }
    }

    public static class ShipDesignerStyle {
        public Texture noComponent;
        public Texture selection;
        public Drawable boxSelection;

        public ShipDesignerStyle() {}

        public ShipDesignerStyle(Texture noComponent, Texture selection, Drawable boxSelection) {
            this.noComponent = noComponent;
            this.selection = selection;
            this.boxSelection = boxSelection;
        }
    }
}
