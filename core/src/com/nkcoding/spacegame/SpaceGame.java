package com.nkcoding.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.nkcoding.spacegame.screens.GameScreen;
import com.nkcoding.spacegame.screens.ShipBuilderScreen;
import com.nkcoding.ui.UITestScreen;

public class SpaceGame extends Game {
    //one SpriteBatch for multiple Screens because heavy object
    private SpriteBatch batch = null;

    public SpriteBatch getBatch() {
        return batch;
    }

    private ExtAssetManager assetManager = null;

    public ExtAssetManager getAssetManager() {
        return assetManager;
    }

    private SaveGameManager.SaveGame saveGame;

    UITestScreen testScreen;
    Screen screen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new ExtAssetManager();
        //probably load a loading screen here, but I don't care now
        //load the resources
        assetManager.loadAll();
        //load saveGame
        saveGame = SaveGameManager.load();
        if (true) {
            screen = new ShipBuilderScreen(this, saveGame.shipDef);
        } else {
            screen = new GameScreen(this, saveGame.shipDef);
        }
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
