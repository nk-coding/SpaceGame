package com.nkcoding.ui;

import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;

import java.util.List;

public class DragShipWidget extends ShipWidget {
    public DragShipWidget(List<ComponentDef> components, ExtAssetManager assetManager) {
        super(assetManager, null, null);
    }

    @Override
    protected ComponentDef getComponentAt(int x, int y) {
        return null;
    }

    @Override
    protected DrawMode getDrawMode(ComponentDef componentDef) {
        return DrawMode.NORMAL;
    }
}
