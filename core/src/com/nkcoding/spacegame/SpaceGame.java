package com.nkcoding.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.nkcoding.communication.Communication;
import com.nkcoding.spacegame.screens.GameScreen;
import com.nkcoding.spacegame.screens.LauncherScreen;
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

        glProfiler = new GLProfiler(Gdx.graphics);
        glProfiler.enable();
        setScreen(new LauncherScreen(this));
    }

    public void startEditor() {
        setScreen(new ShipBuilderScreen(this, saveGame.shipDef));
    }

    public void startGame(Communication communication, Vector2 initialPosition) {
        setScreen(new GameScreen(this, saveGame.shipDef, communication, initialPosition));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        batch.dispose();
    }
}
