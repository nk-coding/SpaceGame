package com.nkcoding.spacegame.spaceship;

import com.badlogic.gdx.physics.box2d.Body;

import java.util.List;

public class Ship {

    //the body which represents the Ship in box2d
    private Body body;

    public  Body getBody(){
        return body;
    }

    //construct Ship out of ShipDef (public constructor)
    public Ship(ShipDef def){

    }

    //package-private constructor to construct Ship out of components (used to split up a ship)
    Ship(List<Component> components){

    }

    //destroy a component (called by a component if it health 0)
    //check structural integrity afterwards
    void destroyComponent(){

    }

    private void checkStructure(){

    }

}
