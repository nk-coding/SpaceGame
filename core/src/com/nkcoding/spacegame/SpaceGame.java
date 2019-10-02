package com.nkcoding.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.nkcoding.ui.RotationTestScreen;
import com.nkcoding.ui.ShipBuilderScreen;
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
	ShipBuilderScreen shipBuilderScreen;
	RotationTestScreen rotationTestScreen;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		assetManager = new ExtAssetManager();
		//probably load a loading screen here, but I don't care now
		//load the resources
		assetManager.loadAll();
		//load saveGame
		saveGame = SaveGameManager.load();
		//testScreen = new UITestScreen(this);
		//shipBuilderScreen = new ShipBuilderScreen(this, saveGame.shipDef);
		rotationTestScreen = new RotationTestScreen(this);
		setScreen(rotationTestScreen);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		//testScreen.dispose();
		//shipBuilderScreen.dispose();
		assetManager.dispose();
		batch.dispose();
	}
}
