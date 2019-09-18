package com.nkcoding.spacegame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.nkcoding.interpreter.ExternalMethodFuture;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.interpreter.compiler.DataTypes;
import com.nkcoding.spacegame.spaceship.Ship;

import java.util.ArrayList;

public class SpaceSimulation{
    public static final float SCALE_FACTOR = 350f;

    //list with all ships
    private ArrayList<Ship> ships = new ArrayList<>();

    //handles all the script stuff for the ship(s)
    private ScriptingEngine scriptingEngine;

    //constructor
    public SpaceSimulation() {
        //init scriptingEngine
        scriptingEngine = new ScriptingEngine();
    }

    /**add a ship
     * @param ship the Ship to add
     */
    public void addShip(Ship ship) {
        //TODO implementation
        ships.add(ship);
    }

    /**remove a ship
     * @param ship the Ship to remove
     */
    public void removeShip(Ship ship) {
        //TODO implementation
        ships.remove(ship);
    }


    //implementation for Simulated
    public void act(float time) {
        //handle all external Methods
        while (!scriptingEngine.getFutureQueue().isEmpty()) {
            ExternalMethodFuture future = scriptingEngine.getFutureQueue().poll();
            for (Ship ship : ships) {
                if (!future.isDone()) ship.handleExternalMethod(future);
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
        ships.forEach(ship -> ship.draw(batch));
    }
}
