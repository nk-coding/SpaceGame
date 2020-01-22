package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.nkcoding.spacegame.simulation.CoreUnit;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.properties.FloatProperty;
import com.nkcoding.spacegame.simulation.spaceship.properties.NotifyProperty;
import com.nkcoding.spacegame.simulation.spaceship.properties.VirtualProperty;

import java.io.DataInputStream;

public class ComputeCore extends Component implements CoreUnit {
    //region keys for the properties
    public static final String KEY_DOWN_KEY = "KeyDown";
    public static final String KEY_UP_KEY = "KeyUp";
    public static final String ANGULAR_VELOCITY_KEY = "AngularVelocity";
    public static final String VELOCITY_KEY = "Velocity";
    public static final String CAMERA_FOCUS_KEY = "CameraFocus";
    //endregion

    private ComputeCoreModel model;

    protected ComputeCore(ComponentDefBase defBase, DataInputStream inputStream, Ship ship) {
        super(defBase, ship);
        this.model = (ComputeCoreModel) super.model;
    }

    protected ComputeCore(ComponentDef componentDef, Ship ship, Ship.ShipModel shipModel) {
        super(componentDef, ship, shipModel);
        this.model = (ComputeCoreModel) super.model;
    }

    @Override
    protected ComponentModel generateModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
        return new ComputeCoreModel(shipModel, componentDef);
    }

    @Override
    public Vector2 getCenterPosition() {
        return getShip().getCenterPosition();
    }

    @Override
    public float getRadius() {
        return getShip().getRadius();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (isOriginal()) {
            model.keyDown(keycode);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (isOriginal()) {
            model.keyUp(keycode);
        }
        return false;
    }

    private class ComputeCoreModel extends ComponentModel {

        //region properties
        //virtual property when a key is pressed
        public final NotifyProperty<String> keyDown = register(new NotifyProperty<>(KEY_DOWN_KEY));
        //virtual property when key is released
        public final NotifyProperty<String> keyUp = register(new NotifyProperty<>(KEY_UP_KEY));
        //wrapper for the angularRotation from Body
        public final FloatProperty angularVelocity = register(new FloatProperty(true, true, ANGULAR_VELOCITY_KEY));
        //wrapper for the velocity from Body
        public final FloatProperty velocity = register(new FloatProperty(true, true, VELOCITY_KEY));
        //focus from SpaceSimulation
        public final VirtualProperty<Boolean> cameraFocus = register(new VirtualProperty<>(true, true, CAMERA_FOCUS_KEY) {
            @Override
            public void set(Boolean value) {
                super.set(value);
                if (value) getSpaceSimulation().setCameraSimulated(Ship.this);
            }

            @Override
            public Boolean get2() {
                return getSpaceSimulation().getCameraSimulated() == Ship.this;
            }
        });

        //endregion

        public ComputeCoreModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef);
        }

        //region key input

        private boolean keyDown(int keycode) {
            keyDown.set(Input.Keys.toString(keycode));
            return true;
        }

        private boolean keyUp(int keycode) {
            keyUp.set(Input.Keys.toString(keycode));
            return true;
        }

        public void setCameraFocus(boolean cameraFocus) {
            System.out.println("set focus: " + name + ", " + cameraFocus);
            this.cameraFocus.set(cameraFocus);
        }

        //endregion
    }
}
