package com.nkcoding.spacegame;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.nkcoding.communication.Communication;
import com.nkcoding.communication.Transmission;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.spacegame.simulation.BodyState;
import com.nkcoding.spacegame.simulation.Simulated;
import com.nkcoding.spacegame.simulation.SynchronizationPriority;
import com.nkcoding.spacegame.simulation.communication.*;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SpaceSimulation implements InputProcessor {
    public static final float TILE_SIZE = 8f;

    private static final float LOW_TIMEOUT = 1f / 2;
    private static final float MEDIUM_TIMEOUT = 1f / 5;
    private static final float HIGH_TIMEOUT = 1f / 30;
    private float lastLow = 0;
    private float lastMedium = 0;
    private float lastHigh = 0;


    private final SpaceGame spaceGame;
    // list with all simulateds
    //private final SnapshotArray<Simulated> simulateds = new SnapshotArray<>();
    private final HashMap<Integer, Simulated> simulatedMap = new HashMap<>();
    private final List<Simulated> simulatedToRemove = new ArrayList<>();
    private final List<Simulated> simulatedToAdd = new ArrayList<>();
    // list with all simulated that receive key events
    private final ArrayList<Simulated> keyHandlers = new ArrayList<>();
    // map with all objects that can receive futures
    private final HashMap<String, ExternalPropertyHandler> propertyHandlers = new HashMap<>();
    // AssetManager to load the resources
    private final ExtAssetManager assetManager;
    // World for Box2D
    // this is the physics simulation
    private final World world;
    //the id of the client
    private int clientID = 0;
    //id counter
    private int idCounter = 0;
    // handles all the ExternalPropertyHandlers
    private ScriptingEngine scriptingEngine;
    // camera to draw stuff correctly
    private OrthographicCamera camera;
    // tiles that must be drawn
    private List<int[]> tilesToDraw = new ArrayList<>();
    //the amount of tiles
    private int tileCount = 0;
    // set from resize, necessary for camera
    private int width, height;
    // center pos and radius, necessary for improved drawing
    private Vector2 centerPos;
    private float radius, scaledRadius;
    // Simulated that the camera should follow
    private Simulated cameraSimulated;

    private Communication communication;

    // constructor
    public SpaceSimulation(SpaceGame spaceGame, Communication communication) {
        this.spaceGame = spaceGame;
        this.communication = communication;
        if (communication != null) {
            this.clientID = communication.getId();
        }
        // set Batch and assetManager
        assetManager = spaceGame.getAssetManager();
        // init scriptingEngine
        scriptingEngine = new ScriptingEngine();
        // init the world
        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Simulated s1 = (Simulated) contact.getFixtureA().getBody().getUserData();
                Simulated s2 = (Simulated) contact.getFixtureB().getBody().getUserData();
                Fixture f1 = contact.getFixtureA();
                Fixture f2 = contact.getFixtureB();
                if (s1.getCollisionPriority() < s2.getCollisionPriority() && s1.getOwner() == clientID) {
                    Simulated temp = s2;
                    s2 = s1;
                    s1 = temp;
                    Fixture fTemp = f2;
                    f2 = f1;
                    f1 = fTemp;
                }

                if (s2.getOwner() == clientID) {
                    s1.beginContact(s2, f1, f2);
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
        // init camera
        this.camera = new OrthographicCamera();
    }

    public ScriptingEngine getScriptingEngine() {
        return scriptingEngine;
    }

    public ExtAssetManager getAssetManager() {
        return assetManager;
    }

    public Simulated getCameraSimulated() {
        return cameraSimulated;
    }

    public void setCameraSimulated(Simulated cameraSimulated) {
        if (cameraSimulated != this.cameraSimulated && this.cameraSimulated != null)
            this.cameraSimulated.setCameraFocus(false);
        this.cameraSimulated = cameraSimulated;
    }

    public World getWorld() {
        return world;
    }

    /**
     * add a simulated
     *
     * @param simulated the Simulated to add
     */
    public void addSimulated(Simulated simulated) {
        simulatedToAdd.add(simulated);
        sendToAll(simulated.getMirrorData());
    }

    /**
     * remove a simulated
     *
     * @param simulated the Simulated to remove
     */
    public void removeSimulated(Simulated simulated) {
        simulatedToRemove.add(simulated);
        sendToAll(new RemoveTransmission(simulated.id));
    }

    /**
     * adds a ExternalPropertyHandler
     *
     * @param handler the handler to add
     */
    public void addExternalPropertyHandler(ExternalPropertyHandler handler) {
        propertyHandlers.put(handler.getName(), handler);
    }

    /**
     * removes an ExternalPropertyHandler
     *
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
        } else {
            keyHandlers.remove(simulated);
        }
    }

    // calls act on all Simulateds
    // deals with ExternalMethodFutures
    public void act(float time) {
        System.out.println("//////////////////////////////////////////");
        handleScriptingEngine();
        handleMessages();
        int synchronizationMask = getBodySynchronization(time);
        // call step on the world
        world.step(time, 6, 2);
        List<BodyState> bodyStatesToSend = new ArrayList<>();
        //act on simulateds
        for (Simulated simulated : simulatedMap.values()) {
            simulated.act(time);
            if (simulated.getOwner() == clientID && (simulated.getSyncPriority() & synchronizationMask) != 0) {
                bodyStatesToSend.add(simulated.getBodyState());
            }
        }
        if (!bodyStatesToSend.isEmpty()) sendToAll(new UpdateBodysTransmission(bodyStatesToSend.toArray(new BodyState[0])));
        updateSimulatedMap();
        // update the camera
        updateCamera();
    }

    private void handleScriptingEngine() {
        while (!scriptingEngine.getFutureQueue().isEmpty()) {
            ExternalMethodFuture future = scriptingEngine.getFutureQueue().poll();
            ExternalPropertyHandler handler = propertyHandlers.get(future.getParameters()[0]);
            if (handler != null) {
                handler.handleExternalMethod(future);
            }
            // complete future manually if none of the simulateds completed it
            if (!future.isDone()) {
                System.out.println("no module completed " + future.toString());
                future.complete(future.getType().getDefaultValue());
            }
        }
    }

    private void updateSimulatedMap() {
        //add new simulated
        for (Simulated toAdd : simulatedToAdd) {
            simulatedMap.put(toAdd.id, toAdd);
        }
        //remove the Simulated to remove
        for (Simulated toRemove : simulatedToRemove) {
            simulatedMap.remove(toRemove.id);
            keyHandlers.remove(toRemove);
            world.destroyBody(toRemove.getBody());
        }
        simulatedToRemove.clear();
        simulatedToAdd.clear();
    }

    private int getBodySynchronization(float delta) {
        int result = 0;
        lastLow += delta;
        lastMedium += delta;
        lastHigh += delta;
        if (lastLow> LOW_TIMEOUT) {
            lastLow = 0;
            result |= SynchronizationPriority.LOW;
        }
        if (lastMedium > MEDIUM_TIMEOUT) {
            lastMedium = 0;
            result |= SynchronizationPriority.MEDIUM;
        }
        if (lastHigh > HIGH_TIMEOUT) {
            lastHigh = 0;
            result |= SynchronizationPriority.HIGH;
        }
        return result;
    }

    private void handleMessages() {
        if (communication != null) {
            while (communication.hasTransmissions()) {
                Transmission transmission = communication.getTransmission();
                switch (transmission.getId()) {
                    case TransmissionID.CREATE_NEW:
                        CreateTransmission createTransmission = (CreateTransmission)transmission;
                        Simulated newSimulated = createTransmission.type.constructor.apply(this, createTransmission);
                        newSimulated.update(createTransmission.bodyState);
                        simulatedToAdd.add(newSimulated);
                        break;
                    case TransmissionID.REMOVE:
                        Simulated toRemove = getSimulated(((RemoveTransmission)transmission).simulatedID);
                        if (toRemove != null) simulatedToRemove.add(toRemove);
                        else System.out.println("cannot remove" + transmission);
                        break;
                    case TransmissionID.UPDATE:
                        UpdateTransmission updateTransmission = (UpdateTransmission)transmission;
                        Simulated toUpdate = getSimulated(updateTransmission.simulatedID);
                        if (toUpdate != null) {
                            toUpdate.receiveTransmission(updateTransmission);
                        } else {
                            System.out.println("cannot update " + transmission);
                        }
                        break;
                    case TransmissionID.UPDATE_BODY_STATE:
                        UpdateBodysTransmission updateBodysTransmission = (UpdateBodysTransmission) transmission;
                        System.out.println(updateBodysTransmission.bodyStates.length);
                        for (BodyState bodyState : updateBodysTransmission.bodyStates) {
                            Simulated updateBody = getSimulated(bodyState.id);
                            if (updateBody != null) {
                                float dist = updateBody.getBody().getPosition().sub(bodyState.position()).len();
                                if (dist > 0.05) System.out.println(dist);
                                updateBody.update(bodyState);
                            } else {
                                System.out.println("cannot update body: " + bodyState);
                            }

                        }
                        break;
                }
            }
        }
    }

    private Simulated getSimulated(int id) {
        Simulated res = simulatedMap.get(id);
        if (res != null) {
            return res;
        } else {
            for (Simulated simulated : simulatedToAdd) {
                if (simulated.id == id) return simulated;
            }
            return null;
        }
    }

    public void draw(Batch batch) {
        // update the batch
        batch.setProjectionMatrix(camera.combined);

        drawBackground(batch);
        for (Simulated simulated : simulatedMap.values()) {
            float maxAbs = simulated.getRadius() + scaledRadius;
            float abs = simulated.localToWorldCoordinates(simulated.getCenterPosition()).sub(centerPos).len2();
            if (abs < (maxAbs * maxAbs)) {
                simulated.draw(batch);
            }
        }
        batch.flush();
        spaceGame.glProfiler.reset();
    }

    private void drawBackground(Batch batch) {
        Texture tileTexture;
        if (tileCount < 6) {
            tileTexture = assetManager.getTexture(Asset.StarBackground_high);
        } else if (tileCount < 15) {
            tileTexture = assetManager.getTexture(Asset.StarBackground_medium);
        } else {
            tileTexture = assetManager.getTexture(Asset.StarBackground_low);
        }
        for (int[] val : tilesToDraw) {
            for (int y = val[1]; y < val[2]; y++) {
                batch.draw(tileTexture, val[0] * TILE_SIZE, y * TILE_SIZE, TILE_SIZE,
                        TILE_SIZE);
            }
        }
    }

    // called when the screen is resized
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        radius = (float) Math.sqrt(width * width + height * height) / 2.0f;
    }

    // updates the camera
    public void updateCamera() {
        if (cameraSimulated != null) {
            centerPos = cameraSimulated.localToWorldCoordinates(cameraSimulated.getCenterPosition());
            float length = cameraSimulated.getBody().getLinearVelocity().len() + 1;
            float h = cameraSimulated.getHeight() / (0.15f / (length * length) + 0.08f);
            float w = h / height * width;

            scaledRadius = radius * h / height;
            camera.setToOrtho(false, w, h);
            camera.position.x = centerPos.x;
            camera.position.y = centerPos.y;
            camera.update();

            // CHANGE THIS WHEN ROTATION IS APPLIED!!!!!!!
            int x1 = (int) Math.floor((centerPos.x - w / 2) / TILE_SIZE);
            int y1 = (int) Math.floor((centerPos.y - h / 2) / TILE_SIZE);
            int x2 = (int) Math.floor((centerPos.x + w / 2) / TILE_SIZE);
            int y2 = (int) Math.floor((centerPos.y + h / 2) / TILE_SIZE);
            int deltaX = x2 - x1 + 2;
            while (tilesToDraw.size() > deltaX) {
                tilesToDraw.remove(deltaX);
            }
            while (tilesToDraw.size() < deltaX) {
                tilesToDraw.add(new int[3]);
            }
            //add all the tiles, also update tile count
            tileCount = 0;
            for (int x = 0; x < deltaX; x++) {
                int[] val = tilesToDraw.get(x);
                val[0] = x + x1;
                val[1] = y1;
                val[2] = y2 + 1;
                tileCount += y2 - y1 + 1;
            }
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

    //region unused events
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
    //endregion

    public int getClientID() {
        return clientID;
    }

    public int getNewId() {
        return clientID * 1000000 + idCounter++;
    }

    public void sendTo(Transmission transmission, int target) {
        if (communication != null) {
            communication.sendTo(target, transmission);
        }
    }

    public void sendToAll(Transmission transmission) {
        if (communication != null) {
            communication.sendToAll(transmission);
        }
    }
}
