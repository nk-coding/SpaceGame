package com.nkcoding.spacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.nkcoding.ui.UITestScreen;

public class SpaceGame extends Game {
	//one SpriteBatch for multiple Screens because heavy object
	private SpriteBatch batch = null;

	public SpriteBatch getBatch() {
		return batch;
	}

	UITestScreen testScreen;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
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
		batch.dispose();
	}
}
