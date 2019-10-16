package com.nkcoding.spacegame;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.SnapshotArray;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.spacegame.spaceship.ExternalPropertyHandler;
import com.nkcoding.spacegame.spaceship.Simulated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SpaceSimulation implements InputProcessor {
    public static final float SCALE_FACTOR = 350f;

    //list with all simulateds
    private final SnapshotArray<Simulated> simulateds = new SnapshotArray<>();

    //list with all simulated that receive key events
    private final ArrayList<Simulated> keyHandlers = new ArrayList<>();

    //map with all objects that can receive futures
    private final HashMap<String, ExternalPropertyHandler> propertyHandlers = new HashMap<>();

    //handles all the ExternalPropertyHandlers
    private ScriptingEngine scriptingEngine;

    public ScriptingEngine getScriptingEngine() {
        return scriptingEngine;
    }

    //AssetManager to load the resources
    private final ExtAssetManager assetManager;

    public ExtAssetManager getAssetManager() {
        return assetManager;
    }

    //camera to draw stuff correctly
    private OrthographicCamera camera;

    //set from resize, necessary for camera
    private int width, height;

    //Simulated that the camera should follow
    private Simulated cameraSimulated;

    public Simulated getCameraSimulated() {
        return cameraSimulated;
    }

    public void setCameraSimulated(Simulated cameraSimulated) {
        if (cameraSimulated != this.cameraSimulated && this.cameraSimulated != null) this.cameraSimulated.setCameraFocus(false);
        this.cameraSimulated = cameraSimulated;
    }

    //DEBUG
    Box2DDebugRenderer debugRenderer;

    //World for Box2D
    //this is the physics simulation
    private final World world;

    public World getWorld() {
        return world;
    }

    //constructor
    public SpaceSimulation(SpaceGame spaceGame) {
        //set Batch and assetManager
        assetManager = spaceGame.getAssetManager();
        //init scriptingEngine
        scriptingEngine = new ScriptingEngine();
        //init the world
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Simulated s1 = (Simulated)contact.getFixtureA().getBody().getUserData();
                Simulated s2 = (Simulated)contact.getFixtureB().getBody().getUserData();
                if (s1.getCollisionPriority() > s2.getCollisionPriority()) {
                    s1.beginContact(s2, contact.getFixtureA(), contact.getFixtureB());
                }
                else {
                    s2.beginContact(s1, contact.getFixtureB(), contact.getFixtureA());
                }
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
        //init camera
        this.camera = new OrthographicCamera();
        debugRenderer = new Box2DDebugRenderer();
    }

    /**add a simulated
     * @param simulated the Simulated to add
     */
    public void addSimulated(Simulated simulated) {
        simulateds.add(simulated);
    }

    /**remove a simulated
     * @param simulated the Simulated to remove
     */
    public void removeSimulated(Simulated simulated) {
        simulateds.removeValue(simulated, true);
    }

    /**
     * adds a ExternalPropertyHandler
     * @param handler the handler to add
     */
    public void addExternalPropertyHandler(ExternalPropertyHandler handler) {
        propertyHandlers.put(handler.getName(), handler);
    }

    /**
     * removes an ExternalPropertyHandler
     * @param handler the handler to remove
     */
    public void removeExternalPropertyHandler(ExternalPropertyHandler handler) {
        propertyHandlers.remove(handler.getName());
    }

    public boolean containsExternalPropertyHandler(String str) {
        return propertyHandlers.get(str) != null;
    }

    public void updateReceivesKeyInput(Simulated simulated) {
        if (simulated.isReceivesKeyInput()) {
            keyHandlers.add(simulated);
        }
        else {
            keyHandlers.remove(simulated);
        }
    }


    //calls act on all Simulateds
    //deals with ExternalMethodFutures
    public void act(float time) {
        //handle all external Methods
        while (!scriptingEngine.getFutureQueue().isEmpty()) {
            ExternalMethodFuture future = scriptingEngine.getFutureQueue().poll();
            ExternalPropertyHandler handler = propertyHandlers.get(future.getParameters()[0]);
            if (handler != null) {
                handler.handleExternalMethod(future);
            }
            //complete future manually if none of the simulateds completed it
            if (!future.isDone()) {
                System.out.println("no module completed " + future.toString());
                switch (future.getType()) {
                    case DataTypes.Boolean:
                        future.complete(false);
                        break;
                    case DataTypes.Float:
                        future.complete(0f);
                        break;
                    case DataTypes.Integer:
                        future.complete(0);
                        break;
                    case DataTypes.String:
                        future.complete("");
                        break;
                    case DataTypes.Void:
                        future.complete(null);
                        break;
                    default:
                        throw new IllegalArgumentException("unknown DataType " + future.getType());
                }
            }
        }
        //call step on the world
        world.step(time, 6, 2);
        for (Simulated simulated : simulateds) {
            //call act on simulateds
            simulated.act(time);
        }
        //update the camera
        updateCamera();
    }

    public void draw(Batch batch) {
        //update the batch
        batch.setProjectionMatrix(camera.combined);
        //debugRenderer.render(world, batch.getProjectionMatrix().cpy());
        //draw simulateds
        if (false) {
            for (Simulated simulated : simulateds) simulated.draw(batch);
            batch.flush();
        }
        else {
            debugRenderer.render(world, camera.combined);
        }

    }

    //called when the screen is resized
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    //updates the camera
    public void updateCamera() {
        if (cameraSimulated != null) {
            Vector2 position = cameraSimulated.localToWorldCoordinates(cameraSimulated.getCenterPosition());
            //float rotation = -cameraSimulated.getRotation() * MathUtils.radiansToDegrees;
            float length = cameraSimulated.getBody().getLinearVelocity().len() + 1;
            float h = cameraSimulated.getHeight() / (0.15f / (length * length) + 0.08f);
            //rotation += Math.atan(cameraSimulated.getBody().getAngularVelocity()) * 45;

            camera.setToOrtho(false, h / height * width , h);
            camera.position.x = position.x;
            camera.position.y = position.y;
            //camera.rotate(rotation);
            camera.update();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        Iterator<Simulated> iter = keyHandlers.iterator();
        boolean handled = false;
        while (iter.hasNext() && !handled) {
            handled = iter.next().keyDown(keycode);
        }
        return handled;
    }

    @Override
    public boolean keyUp(int keycode) {
        Iterator<Simulated> iter = keyHandlers.iterator();
        boolean handled = false;
        while (iter.hasNext() && !handled) {
            handled = iter.next().keyUp(keycode);
        }
        return handled;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
