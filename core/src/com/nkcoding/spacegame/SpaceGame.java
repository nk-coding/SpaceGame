package com.nkcoding.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.nkcoding.ui.UITestScreen;

public class SpaceGame extends Game {
	//one SpriteBatch for multiple Screens because heavy object
	private SpriteBatch batch = null;

	public SpriteBatch getBatch() {
		return batch;
	}

	private AssetManager assetManager = null;

	public AssetManager getAssetManager() {
		return assetManager;
	}

	UITestScreen testScreen;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		assetManager = new AssetManager();
		//probably load a loading screen here, but I don't care now
		//load the resources
		assetManager.load("badlogic.jpg", Texture.class);
		assetManager.load("consolas.fnt", BitmapFont.class);
		assetManager.load("cursor.png", Texture.class);
		assetManager.load("newScrollBarBackground.png", Texture.class);
		assetManager.load("numbers.png", Texture.class);
		assetManager.load("scrollBarBackground.png", Texture.class);
		assetManager.load("scrollBarThumb.png", Texture.class);
		assetManager.load("simpleborder.png", Texture.class);
		assetManager.update();
		assetManager.finishLoading();
		testScreen = new UITestScreen(this);
		setScreen(testScreen);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		testScreen.dispose();
		assetManager.dispose();
		batch.dispose();
	}
}
