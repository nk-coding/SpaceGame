package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SpaceGame;

public class RotationTestScreen implements Screen, InputProcessor {
    private Stage stage;

    ExtAssetManager assetManager;

    Group mainGroup;
    Image image;

    public RotationTestScreen(SpaceGame spaceGame) {
        this.stage = new Stage(new ScreenViewport(), spaceGame.getBatch());
        assetManager = spaceGame.getAssetManager();

        Gdx.input.setInputProcessor(stage);

        //new group with rotation
        mainGroup = new Group();
        image = new Image(assetManager.getDrawable(Asset.Badlogic));
        image.setSize(100, 200);
        image.setPosition(100, 100);

        Image image2 = new Image(assetManager.getDrawable(Asset.Badlogic));
        image2.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                System.out.println("key down");
                return super.keyDown(event, keycode);
            }
        });
        image.setSize(200,200);
        image.setPosition(300, 400);

        mainGroup.addActor(image);
        mainGroup.addActor(image2);
        stage.addActor(mainGroup);
        stage.setKeyboardFocus(image2);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

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

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.A) {
            mainGroup.setRotation(mainGroup.getRotation() + 10f);
        }
        if (keycode == Input.Keys.W) {
            stage.getCamera().rotateAround(new Vector3(0,0,0), new Vector3(0,0,1), 10);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
