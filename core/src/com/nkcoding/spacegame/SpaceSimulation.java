package com.nkcoding.spacegame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.spacegame.spaceship.Component;
import com.nkcoding.spacegame.spaceship.ExternalPropertyHandler;
import com.nkcoding.spacegame.spaceship.Ship;

import java.util.ArrayList;
import java.util.HashMap;

public class SpaceSimulation{
    public static final float SCALE_FACTOR = 350f;

    //list with all ships
    private ArrayList<Ship> ships = new ArrayList<>();

    //handles all the ExternalPropertyHandlers
    private ScriptingEngine scriptingEngine;

    public ScriptingEngine getScriptingEngine() {
        return scriptingEngine;
    }

    //map with all objects that can receive futures
    private HashMap<String, ExternalPropertyHandler> propertyHandlers = new HashMap<>();

    //constructor
    public SpaceSimulation() {
        //init scriptingEngine
        scriptingEngine = new ScriptingEngine();
    }

    /**add a ship
     * @param ship the Ship to add
     */
    public void addShip(Ship ship) {
        ships.add(ship);
        propertyHandlers.put(ship.getName(), ship);
        for (Actor child : ship.getChildren()) {
            if (child instanceof ExternalPropertyHandler) {
                ExternalPropertyHandler handler = (ExternalPropertyHandler)child;
                propertyHandlers.put(handler.getName(), handler);
            }
        }
    }

    /**remove a ship
     * @param ship the Ship to remove
     */
    public void removeShip(Ship ship) {
        ships.remove(ship);
        propertyHandlers.remove(ship.getName());
        for (Actor child : ship.getChildren()) {
            if (child instanceof ExternalPropertyHandler) {
                ExternalPropertyHandler handler = (ExternalPropertyHandler)child;
                propertyHandlers.remove(handler.getName());
            }
        }
    }


    //calls act on all ships
    //deals with ExternalMethodFutures
    public void act(float time) {
        //handle all external Methods
        while (!scriptingEngine.getFutureQueue().isEmpty()) {
            ExternalMethodFuture future = scriptingEngine.getFutureQueue().poll();
            ExternalPropertyHandler handler = propertyHandlers.get(future.getParameters()[0]);
            if (handler != null) {
                handler.handleExternalMethod(future);
            }
            //complete future manually if none of the ships completed it
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
        for (Ship ship : ships) {
            //call act on ships
            ship.act(time);
        }
    }

    //implementation for Simulated
    public void draw(SpriteBatch batch) {
        //draw ships
        //TODO
    }
}
