package com.nkcoding.spacegame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.SpaceSimulation;
import com.nkcoding.spacegame.spaceship.Ship;
import com.nkcoding.spacegame.spaceship.ShipDef;

public class GameScreen implements Screen {
    //game
    private SpaceGame spaceGame;

    //main batch
    private Batch batch;

    //the simulation that handles physics, drawing etc.
    private SpaceSimulation spaceSimulation;
    //Ship for the simulation
    private ShipDef shipDef;

    public GameScreen(SpaceGame spaceGame, ShipDef shipDef) {
        this.spaceGame = spaceGame;
        this.batch = spaceGame.getBatch();
        //create and init spaceSimulation
        this.spaceSimulation = new SpaceSimulation(spaceGame);
        Gdx.input.setInputProcessor(spaceSimulation);
        this.shipDef = shipDef;
        Ship ship = new Ship(shipDef, spaceSimulation);
        spaceSimulation.addSimulated(ship);
        spaceSimulation.setCameraSimulated(ship);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spaceSimulation.act(delta);
        batch.begin();
        spaceSimulation.draw(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        spaceSimulation.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
