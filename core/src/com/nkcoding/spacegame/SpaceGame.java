package com.nkcoding.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

	UITestScreen testScreen;
	ShipBuilderScreen shipBuilderScreen;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		assetManager = new ExtAssetManager();
		//probably load a loading screen here, but I don't care now
		//load the resources
		assetManager.loadAll();
		//testScreen = new UITestScreen(this);
		shipBuilderScreen = new ShipBuilderScreen(this);
		setScreen(shipBuilderScreen);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		//testScreen.dispose();
		shipBuilderScreen.dispose();
		assetManager.dispose();
		batch.dispose();
	}
}
