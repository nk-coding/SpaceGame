package com.nkcoding.spacegame;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.nkcoding.communication.Communication;
import com.nkcoding.communication.ResetDataOutputStream;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.spacegame.simulation.Simulated;
import com.nkcoding.spacegame.simulation.SimulatedType;
import com.nkcoding.spacegame.simulation.SynchronizationPriority;
import com.nkcoding.spacegame.simulation.communication.TransmissionID;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SpaceSimulation implements InputProcessor {
    public static final float TILE_SIZE = 8f;

    private static final float LOW_TIMEOUT = 1f / 2;
    private static final float MEDIUM_TIMEOUT = 1f / 5;
    private static final float HIGH_TIMEOUT = 1f / 20;
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
    private short clientID = 0;
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
    private int bodyUpdateID = 1;

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
        ResetDataOutputStream outputStream = getOutputStream(true);
        try {
            outputStream.writeInt(TransmissionID.CREATE_NEW);
            simulated.serialize(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendToAll(outputStream);
    }

    /**
     * remove a simulated
     *
     * @param simulated the Simulated to remove
     */
    public void removeSimulated(Simulated simulated) {
        simulatedToRemove.add(simulated);
        ResetDataOutputStream outputStream = getOutputStream(true);
        try {
            outputStream.writeInt(TransmissionID.REMOVE);
            outputStream.writeInt(simulated.id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendToAll(outputStream);
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
        handleScriptingEngine();
        handleMessages();
        int synchronizationMask = getBodySynchronization(time);
        // call step on the world
        world.step(time, 6, 2);

        DataOutputStream outputStream = null;
        ArrayList<Simulated> bodyUpdateList = new ArrayList<>();
        //act on simulateds
        for (Simulated simulated : simulatedMap.values()) {
            simulated.act(time);
            if (simulated.getOwner() == clientID && (simulated.getSyncPriority() & synchronizationMask) != 0) {
                bodyUpdateList.add(simulated);
            }
        }
        sendBodyUpdates(bodyUpdateList);

        updateSimulatedMap();
        // update the camera
        updateCamera();
    }

    private void sendBodyUpdates(ArrayList<Simulated> bodyUpdateList) {
        if (!bodyUpdateList.isEmpty()) {
            try {
                int maxAmount = Communication.MAX_SIZE / 28;
                for (int x = 0; x < Math.ceil(bodyUpdateList.size() / (float)maxAmount); x++) {
                    int max = Math.min(bodyUpdateList.size(), (x + 1) * maxAmount);
                    ResetDataOutputStream outputStream = getOutputStream(false);
                    outputStream.writeInt(TransmissionID.UPDATE_BODY_STATE);
                    //write the id
                    outputStream.writeInt(bodyUpdateID);
                    outputStream.writeInt(max - x * maxAmount);
                    for (int i = x * maxAmount; i < max; i++) {
                        bodyUpdateList.get(i).serialize(outputStream);
                    }
                    sendToAll(outputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //increase the id
            bodyUpdateID++;
        }
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
            try {
                while (communication.hasTransmissions()) {
                    DataInputStream inputStream = communication.getTransmission();
                    switch (inputStream.readInt()) {
                        case TransmissionID.CREATE_NEW:
                            SimulatedType type = SimulatedType.deserialize(inputStream);
                            Simulated newSimulated = type.constructor.apply(this, inputStream);
                            newSimulated.deserializeBodyState(inputStream, 0);
                            simulatedToAdd.add(newSimulated);
                            break;
                        case TransmissionID.REMOVE:
                            //Simulated toRemove = getSimulated(((RemoveTransmission)transmission).simulatedID);
                            int removeID = inputStream.readInt();
                            Simulated toRemove = getSimulated(removeID);
                            if (toRemove != null) simulatedToRemove.add(toRemove);
                            else System.out.println("cannot remove" + removeID);
                            break;
                        case TransmissionID.UPDATE:
                            short updateID = inputStream.readShort();
                            Simulated toUpdate = getSimulated(updateID);
                            if (toUpdate != null) {
                                toUpdate.receiveTransmission(toUpdate.deserializeTransmission(inputStream, inputStream.readShort()));
                            } else {
                                System.out.println("cannot update " + updateID);
                            }
                            break;
                        case TransmissionID.UPDATE_BODY_STATE:
                            int bodyUpdateID = inputStream.readInt();
                            int amount = inputStream.readInt();
                            for (int x = 0; x < amount; x++) {
                                int simulatedID = inputStream.readInt();
                                Simulated updateBody = getSimulated(simulatedID);
                                if (updateBody != null) {
                                    updateBody.deserializeBodyState(inputStream, bodyUpdateID);
                                } else {
                                    System.out.println("cannot update body: " + simulatedID);
                                }
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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

    public short getClientID() {
        return clientID;
    }

    public int getNewId() {
        return clientID * 1000000 + idCounter++;
    }

    public void sendTo(ResetDataOutputStream transmission, short target) {
        if (communication != null) {
            communication.sendTo(target, transmission);
        } else {
            try {
                transmission.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToAll(ResetDataOutputStream transmission) {
        if (communication != null) {
            communication.sendToAll(transmission);
        } else {
            try {
                transmission.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ResetDataOutputStream getOutputStream(boolean reliable) {
        if (communication != null) {
            return communication.getOutputStream(reliable);
        } else {
            return new ResetDataOutputStream();
        }
    }
}
