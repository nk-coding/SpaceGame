package com.nkcoding.spacegame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.communication.Communication;
import com.nkcoding.communication.SocketCommunication;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SpaceGame;

public class LauncherScreen implements Screen {

    private SpaceGame spaceGame;
    private final Stage stage;
    private final ExtAssetManager assetManager;
    private final Batch spriteBatch;

    private final Table selectionTable;
    private final TextButton editorButton;
    private final TextButton singleplayerButton;
    private final TextButton serverButton;
    private final TextButton clientButton;

    private Communication communication;

    private final Table serverTable;
    private final Label serverPortLabel;
    private final TextField serverPortTextField;
    private final TextButton startServerButton;

    private final Table clientTable;
    private final Label clientClientPortLabel;
    private final TextField clientClientPortTextField;
    private final Label clientServerPortLabel;
    private final TextField clientServerPortTextField;
    private final Label clientServerIPLabel;
    private final TextField clientServerIPTextField;
    private final TextButton startClientButton;

    //private final Table serverTable;

    //private final Table clientTable;

    public LauncherScreen(SpaceGame spaceGame) {
        this.spaceGame = spaceGame;
        this.spriteBatch = spaceGame.getBatch();
        this.assetManager = spaceGame.getAssetManager();
        //region create the stage with and all its components
        ScreenViewport viewport = new ScreenViewport();
        //viewport.setUnitsPerPixel(0.75f / Gdx.graphics.getDensity());
        stage = new Stage(viewport, spriteBatch);
        Gdx.input.setInputProcessor(stage);

        //region styles

        Drawable background = new NinePatchDrawable(new NinePatch(assetManager.getTexture(Asset.SimpleBorder), 3, 3, 3, 3));
        background.setLeftWidth(10);
        background.setRightWidth(10);
        background.setTopHeight(10);
        background.setBottomHeight(10);

        //ScrollPane
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        //scrollPaneStyle.background = new SpriteDrawable(new Sprite(assetManager.get("simpleborder.png", Texture.class)));
        scrollPaneStyle.vScrollKnob = assetManager.getDrawable(Asset.ScrollBarKnob);
        scrollPaneStyle.vScroll = assetManager.getDrawable(Asset.ScrollBarBackground);
        scrollPaneStyle.hScrollKnob = assetManager.getDrawable(Asset.ScrollBarKnob);
        scrollPaneStyle.hScroll = assetManager.getDrawable(Asset.ScrollBarBackground);

        //Label
        Label.LabelStyle labelStyleSmall = new Label.LabelStyle(assetManager.getBitmapFont(Asset.SourceCodePro_18), new Color(0xffffffff));
        Label.LabelStyle labelStyleBig = new Label.LabelStyle(assetManager.getBitmapFont(Asset.SourceCodePro_32), new Color(0xffffffff));

        //TextField
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = assetManager.getBitmapFont(Asset.SourceCodePro_18);
        textFieldStyle.fontColor = new Color(0xffffffff);
        textFieldStyle.cursor = assetManager.getDrawable(Asset.Cursor);
        textFieldStyle.selection = assetManager.getDrawable(Asset.Selection);
        Drawable textFieldBackground = assetManager.getDrawable(Asset.ScrollBarBackground);
        textFieldBackground.setLeftWidth(5);
        textFieldBackground.setRightWidth(5);
        textFieldBackground.setTopHeight(5);
        textFieldBackground.setBottomHeight(5);
        textFieldStyle.background = textFieldBackground;

        //textbutton style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = assetManager.getBitmapFont(Asset.SourceCodePro_32);
        textButtonStyle.fontColor = new Color(0xffffffff);
        textButtonStyle.down = background;
        textButtonStyle.up = background;
        //endregion

        //region selection table
        selectionTable = new Table();
        selectionTable.setFillParent(true);
        stage.addActor(selectionTable);

        editorButton = new TextButton("Editor", textButtonStyle);
        singleplayerButton = new TextButton("Singleplayer", textButtonStyle);
        serverButton = new TextButton("Server", textButtonStyle);
        clientButton = new TextButton("Client", textButtonStyle);

        selectionTable.add(editorButton).pad(15);
        selectionTable.add(singleplayerButton).pad(15);
        selectionTable.add(serverButton).pad(15);
        selectionTable.add(clientButton).pad(15);

        editorButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                spaceGame.startEditor();
            }
        });
        singleplayerButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                spaceGame.startGame(null);
            }
        });
        serverButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                stage.getActors().removeIndex(0);
                stage.addActor(serverTable);
            }
        });
        clientButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                stage.getActors().removeIndex(0);
                stage.addActor(clientTable);
            }
        });

        //endregion

        //region server table

        serverTable = new Table();
        serverTable.setFillParent(true);
        serverPortLabel = new Label("Port", labelStyleBig);
        serverPortTextField = new TextField("8001", textFieldStyle);
        serverPortTextField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        startServerButton = new TextButton("Start server", textButtonStyle);
        startServerButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (communication == null) {
                    communication = new SocketCommunication(true, Integer.parseInt(serverPortTextField.getText()));
                    startServerButton.setText("Start Game");
                } else {
                    spaceGame.startGame(communication);
                }
            }
        });

        serverTable.add(serverPortLabel).pad(15).row();
        serverTable.add(serverPortTextField).pad(15).row();
        serverTable.add(startServerButton).pad(15);

        //endregion

        //region client table

        clientTable = new Table();
        clientTable.setFillParent(true);
        clientClientPortLabel = new Label("Client Port", labelStyleBig);
        clientClientPortTextField = new TextField("8000", textFieldStyle);
        clientServerIPLabel = new Label("Server IP", labelStyleBig);
        clientServerIPTextField = new TextField("", textFieldStyle);
        clientServerPortLabel = new Label("Server Port", labelStyleBig);
        clientServerPortTextField = new TextField("8001", textFieldStyle);
        startClientButton = new TextButton("Start Client", textButtonStyle);
        startClientButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (communication == null) {
                    communication = new SocketCommunication(false, Integer.parseInt(clientClientPortTextField.getText()));
                    communication.openCommunication(clientServerIPTextField.getText(), Integer.parseInt(clientServerPortTextField.getText()));
                    startClientButton.setText("Start Game");
                } else {
                    spaceGame.startGame(communication);
                }
            }
        });

        clientTable.add(clientClientPortLabel).pad(15).row();
        clientTable.add(clientClientPortTextField).pad(15).row();
        clientTable.add(clientServerIPLabel).pad(15).row();
        clientTable.add(clientServerIPTextField).pad(15).row();
        clientTable.add(clientServerPortLabel).pad(15).row();
        clientTable.add(clientServerPortTextField).pad(15).row();
        clientTable.add(startClientButton).pad(15);


        //endregion

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
        //resize the stage
        stage.getViewport().update(width, height, true);
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
