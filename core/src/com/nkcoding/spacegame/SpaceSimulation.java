package com.nkcoding.spacegame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.nkcoding.interpreter.ScriptingEngine;
import com.nkcoding.spacegame.spaceship.Ship;

import java.util.ArrayList;

public class SpaceSimulation implements Simulated{
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
    @Override
    public void act(float time) {
        for (Ship ship : ships) {
            //call act on ships
            ship.act(time);
        }
    }

    //implementation for Simulated
    @Override
    public void draw(SpriteBatch batch) {
        //draw ships
        ships.forEach(ship -> ship.draw(batch));
    }
}
