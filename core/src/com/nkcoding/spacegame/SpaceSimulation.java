package com.nkcoding.spacegame;

import com.nkcoding.spacegame.spaceship.Ship;

import java.util.ArrayList;

public class SpaceSimulation {
    public static final float SCALE_FACTOR = 350f;

    //list with all ships
    private ArrayList<Ship> ships = new ArrayList<>();

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


}
