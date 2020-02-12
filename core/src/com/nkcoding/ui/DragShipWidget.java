package com.nkcoding.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDefBase;

import java.util.Arrays;
import java.util.List;

public class DragShipWidget extends ShipWidget {

    ComponentDef[][] componentsMap;
    
    public final int offsetX, offsetY;

    public Vector2 dragOffset;

    public DragShipWidget(List<ComponentDef> components, ExtAssetManager assetManager, float zoom) {
        super(assetManager, null, null);
        this.zoom = zoom;

        offsetX = components.stream().mapToInt(ComponentDefBase::getX).min().orElseThrow();
        int maxX = components.stream().mapToInt(component -> (component.getX() + component.getRealWidth())).max().orElseThrow();
        offsetY = components.stream().mapToInt(ComponentDefBase::getY).min().orElseThrow();
        int maxY = components.stream().mapToInt(component -> (component.getY() + component.getRealHeight())).max().orElseThrow();

        amountDrawX = maxX - offsetX + 1;
        amountDrawY = maxY - offsetY + 1;

        componentsMap = new ComponentDef[amountDrawX][amountDrawY];
        for (ComponentDef def : components) {
            componentsMap[def.getX() - offsetX][def.getY() - offsetY] = def;
        }
        startDrawX = 0;
        startDrawY = 0;
        setSize(amountDrawX * zoom * ShipWidget.COMPONENT_SIZE,
                amountDrawY * zoom * ShipWidget.COMPONENT_SIZE);
    }

    @Override
    protected float getDrawX() {
        return super.getDrawX() - offsetX * zoom * COMPONENT_SIZE;
    }

    @Override
    protected float getDrawY() {
        return super.getDrawY() - offsetY * zoom * COMPONENT_SIZE;
    }

    @Override
    protected ComponentDef getComponentAt(int x, int y) {
        return componentsMap[x][y];
    }

    @Override
    protected DrawMode getDrawMode(ComponentDef componentDef, int x, int y) {
        return DrawMode.NORMAL;
    }
}
