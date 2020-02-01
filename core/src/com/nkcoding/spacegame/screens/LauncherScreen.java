package com.nkcoding.spacegame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.communication.Communication;
import com.nkcoding.communication.DatagramSocketCommunication;
import com.nkcoding.communication.ResetDataOutputStream;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.ui.Styles;

import java.io.DataInputStream;
import java.io.IOException;

public class LauncherScreen implements Screen {

    private static final int START_GAME = -1;

    private SpaceGame spaceGame;
    private final Stage stage;

    private Communication communication;

    private final Table serverTable;
    private final TextField serverPortTextField;
    private final TextButton startServerButton;

    private final Table clientTable;
    private final TextField clientClientPortTextField;
    private final TextField clientServerPortTextField;
    private final TextField clientServerIPTextField;
    private final TextButton startClientButton;

    private final float defaultPadding;

    public LauncherScreen(SpaceGame spaceGame) {        
        this.spaceGame = spaceGame;
        Batch spriteBatch = spaceGame.getBatch();
        ExtAssetManager assetManager = spaceGame.getAssetManager();
        final Styles styles = new Styles(assetManager);
        defaultPadding = 15 * styles.scaleFactor;

        //region create the stage with and all its components
        ScreenViewport viewport = new ScreenViewport();

        stage = new Stage(viewport, spriteBatch);
        Gdx.input.setInputProcessor(stage);

        //region selection table
        Table selectionTable = new Table();
        selectionTable.setFillParent(true);
        stage.addActor(selectionTable);

        TextButton editorButton = new TextButton("Editor", styles.textButtonStyle);
        TextButton singleplayerButton = new TextButton("Singleplayer", styles.textButtonStyle);
        TextButton serverButton = new TextButton("Server", styles.textButtonStyle);
        TextButton clientButton = new TextButton("Client", styles.textButtonStyle);

        selectionTable.add(editorButton).pad(defaultPadding);
        selectionTable.add(singleplayerButton).pad(defaultPadding);
        selectionTable.add(serverButton).pad(defaultPadding);
        selectionTable.add(clientButton).pad(defaultPadding);

        editorButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                spaceGame.startEditor();
            }
        });
        singleplayerButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                spaceGame.startGame(null, new Vector2());
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
        Label serverPortLabel = new Label("Port", styles.labelStyleBig);
        serverPortTextField = new TextField("8001", styles.textFieldStyle);
        serverPortTextField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        startServerButton = new TextButton("Start server", styles.textButtonStyle);
        startServerButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (communication == null) {
                    communication = new DatagramSocketCommunication(true, Integer.parseInt(serverPortTextField.getText()));
                    startServerButton.setText("Start Game");
                } else {
                    int counter = 0;
                    for (short peer : communication.getPeers()) {
                        counter++;
                        ResetDataOutputStream outputStream = communication.getOutputStream(true);
                        try {
                            outputStream.writeInt(START_GAME);
                            outputStream.writeFloat(0);
                            outputStream.writeFloat(counter * ShipDef.UNIT_SIZE * ShipDef.MAX_SIZE);
                            communication.sendTo(peer, outputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    spaceGame.startGame(communication, new Vector2());
                }
            }
        });

        serverTable.add(serverPortLabel).pad(defaultPadding).row();
        serverTable.add(serverPortTextField).pad(defaultPadding).row();
        serverTable.add(startServerButton).pad(defaultPadding);

        //endregion

        //region client table

        clientTable = new Table();
        clientTable.setFillParent(true);
        Label clientClientPortLabel = new Label("Client Port", styles.labelStyleBig);
        clientClientPortTextField = new TextField("8000", styles.textFieldStyle);
        Label clientServerIPLabel = new Label("Server IP", styles.labelStyleBig);
        clientServerIPTextField = new TextField("", styles.textFieldStyle);
        Label clientServerPortLabel = new Label("Server Port", styles.labelStyleBig);
        clientServerPortTextField = new TextField("8001", styles.textFieldStyle);
        startClientButton = new TextButton("Start Client", styles.textButtonStyle);
        startClientButton.addCaptureListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (communication == null) {
                    communication = new DatagramSocketCommunication(false, Integer.parseInt(clientClientPortTextField.getText()));
                    communication.openCommunication(clientServerIPTextField.getText(), Integer.parseInt(clientServerPortTextField.getText()));
                    startClientButton.setVisible(false);
                }
            }
        });

        clientTable.add(clientClientPortLabel).pad(defaultPadding).row();
        clientTable.add(clientClientPortTextField).pad(defaultPadding).row();
        clientTable.add(clientServerIPLabel).pad(defaultPadding).row();
        clientTable.add(clientServerIPTextField).pad(defaultPadding).row();
        clientTable.add(clientServerPortLabel).pad(defaultPadding).row();
        clientTable.add(clientServerPortTextField).pad(defaultPadding).row();
        clientTable.add(startClientButton).pad(defaultPadding);


        //endregion

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (communication != null) {
            while (communication.hasTransmissions()) {
                DataInputStream inputStream = communication.getTransmission();
                try {
                    int id = inputStream.readInt();
                    if (id == START_GAME) {
                        //start the game
                        float posX = inputStream.readFloat();
                        float posY = inputStream.readFloat();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        spaceGame.startGame(communication, new Vector2(posX, posY));
                        break;
                    } else {
                        System.err.println("drop transmission");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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
