package com.nkcoding.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.nkcoding.spacegame.screens.GameScreen;
import com.nkcoding.spacegame.screens.ShipBuilderScreen;

public class SpaceGame extends Game {
    public GLProfiler glProfiler;
    Screen screen;
    //one SpriteBatch for multiple Screens because heavy object
    private SpriteBatch batch = null;
    private ExtAssetManager assetManager = null;
    private SaveGameManager.SaveGame saveGame;

    public SpriteBatch getBatch() {
        return batch;
    }

    public ExtAssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new ExtAssetManager();
        //probably load a loading screen here, but I don't care now
        //load the resources
        assetManager.loadAll();
        //load saveGame
        saveGame = SaveGameManager.load();
        if (false) {
            screen = new ShipBuilderScreen(this, saveGame.shipDef);
        } else {
            screen = new GameScreen(this, saveGame.shipDef);
        }
        glProfiler = new GLProfiler(Gdx.graphics);
        glProfiler.enable();
        setScreen(screen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        //testScreen.dispose();
        //shipBuilderScreen.dispose();
        assetManager.dispose();
        batch.dispose();
    }
}
