package com.nkcoding.spacegame.simulation.spaceship.components;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.nkcoding.interpreter.ListObject;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.StackItem;
import com.nkcoding.interpreter.compiler.DataType;
import com.nkcoding.spacegame.simulation.CoreUnit;
import com.nkcoding.spacegame.simulation.Ship;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyData;
import com.nkcoding.spacegame.simulation.spaceship.properties.NotifyProperty;
import com.nkcoding.spacegame.simulation.spaceship.properties.VirtualProperty;

import java.io.DataInputStream;
import java.util.Map;

public class ComputeCore extends Component implements CoreUnit {
    //region keys for the properties
    public static final String KEY_DOWN_KEY = "KeyDown";
    public static final String KEY_UP_KEY = "KeyUp";
    public static final String ANGULAR_VELOCITY_KEY = "AngularVelocity";
    public static final String VELOCITY_KEY = "Velocity";
    public static final String CAMERA_FOCUS_KEY = "CameraFocus";
    public static final String INIT_CALLBACK_KEY = "InitCallback";
    public static final String SHIP_POSITION_KEY = "ShipPosition";
    public static final String ROTATION_KEY = "Rotation";
    //endregion

    private ComputeCoreModel model;

    protected ComputeCore(ComponentDefBase defBase, DataInputStream inputStream, Ship ship) {
        super(defBase, ship);
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
    public Vector2 getWorldCenterPosition() {
        return getShip().localToWorldCoordinates(getShip().getCenterPosition());
    }

    @Override
    public float getRequestedHeight() {
        float length = getShip().getBody().getLinearVelocity().len() + 1;
        return getShip().getRadius() / (0.2f / (length * length) + 0.08f);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (isOriginal()) {
            return model.keyDown(keycode);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (isOriginal()) {
            return model.keyUp(keycode);
        }
        return false;
    }

    @Override
    public void setCameraFocus(boolean cameraFocus) {
        if (isOriginal()) {
            model.setCameraFocus(cameraFocus);
        }
    }

    @Override
    public void removeComponent() {
        super.removeComponent();
        getSpaceSimulation().removeCoreUnit(this);
    }

    @Override
    public void addComponent() {
        super.addComponent();
        getSpaceSimulation().addCoreUnit(this);
    }

    private class ComputeCoreModel extends ComponentModel {

        //region properties
        //virtual property when a key is pressed
        public final NotifyProperty<String> keyDown = register(new NotifyProperty<>(KEY_DOWN_KEY));
        //virtual property when key is released
        public final NotifyProperty<String> keyUp = register(new NotifyProperty<>(KEY_UP_KEY));
        //wrapper for the angularRotation from Body
        public final VirtualProperty<Float> angularVelocity = register(new VirtualProperty<>(ANGULAR_VELOCITY_KEY) {
            @Override
            public Float get2() {
                return getShip().getBody().getAngularVelocity();
            }
        });

        //wrapper for the velocity from Body
        public final VirtualProperty<ListObject> velocity = register(new VirtualProperty<>(VELOCITY_KEY) {
            @Override
            public ListObject get2() {
                ListObject velocity = new ListObject(2);
                velocity.items[0] = new StackItem<Float>(DataType.FLOAT);
                velocity.items[1] = new StackItem<Float>(DataType.FLOAT);
                Vector2 vel = getShip().getBody().getLinearVelocity();
                velocity.items[0].setValue(vel.x * 10);
                velocity.items[1].setValue(vel.y * 10);
                return velocity;
            }
        });

        //focus from SpaceSimulation
        public final VirtualProperty<Boolean> cameraFocus = register(new VirtualProperty<>(CAMERA_FOCUS_KEY) {
            @Override
            public void set(Boolean value) {
                super.set(value);
                if (value && getSpaceSimulation().getCameraCoreUnit() != ComputeCore.this){
                    getSpaceSimulation().setCameraCoreUnit(ComputeCore.this);
                }
            }

            @Override
            public void setInitValue(String value) {
                set(value.equals("true"));
            }

            @Override
            public Boolean get2() {
                return getSpaceSimulation().getCameraCoreUnit() == ComputeCore.this;
            }
        });

        //the init handler
        public final VirtualProperty<String> initCallback = register(new VirtualProperty<>(INIT_CALLBACK_KEY) {
            @Override
            public String get2() {
                return getName();
            }

            //overwrite init to call the init handler first before anything different happens
            @Override
            public void init(ExternalPropertyData data, Map<String, MethodStatement> methods) {
                super.init(data, methods);
                changed = true;
                startChangedHandler(getSpaceSimulation().getScriptingEngine(), getShip().model.globalVariables);
            }
        });

        //the Position
        public final VirtualProperty<ListObject> shipPosition = register(new VirtualProperty<>(SHIP_POSITION_KEY) {
            @Override
            public ListObject get2() {
                ListObject pos = new ListObject(2);
                pos.items[0] = new StackItem<Float>(DataType.FLOAT);
                pos.items[1] = new StackItem<Float>(DataType.FLOAT);
                Vector2 currentCenterPos = getWorldCenterPosition();
                pos.items[0].setValue(10f * currentCenterPos.x);
                pos.items[1].setValue(10f * currentCenterPos.y);
                return pos;
            }
        });

        //the Rotation
        public final VirtualProperty<Float> rotation = register(new VirtualProperty<>(ROTATION_KEY) {
            @Override
            public Float get2() {
                return getShip().getRotation();
            }
        });
        //endregion

        public ComputeCoreModel(Ship.ShipModel shipModel, ComponentDef componentDef) {
            super(shipModel, componentDef);
            keyUp.allowParallel();
            keyDown.allowParallel();
        }

        @Override
        public void act(float delta) {
            super.act(delta);
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
            this.cameraFocus.set(cameraFocus);
        }



        //endregion
    }
}
