package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.spaceship.ComponentDef;

public class ShipBuilderScreen implements Screen {

    //the game
    private SpaceGame spaceGame;

    //the stage
    private Stage stage;

    //fields from game
    //spriteBatch for the stage
    private SpriteBatch spriteBatch;

    private AssetManager assetManager;

    //region fields for ui elements
    //table that contains all the other controls
    private Table rootTable;

    //CodeEditor for the file with all methods
    private CodeEditor codeEditor;

    //ScrollPane for the componentsStack
    private ScrollPane componentsScrollPane;

    //Stack for the possible components
    private Table componentsStack;

    //ScrollPane for the propertiesStack
    private ScrollPane propertiesScrollPane;

    //Stack for the external properties for the selected Component
    private Table propertiesStack;

    //the main Designer for the Ship
    private ShipDesigner shipDesigner;

    //button which saves (and probably closes?)
    private Button saveButton;

    //button which switches to code view
    private Button switchButton;
    //endregion

    //normal (ship) view?
    private boolean isShipView = true;

    //constructor
    public ShipBuilderScreen(SpaceGame spaceGame) {
        this.spaceGame = spaceGame;
        this.spriteBatch = spaceGame.getBatch();
        this.assetManager = spaceGame.getAssetManager();

        //region create the stage with and all its components
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1.4f * Gdx.graphics.getDensity());
        System.out.println(Gdx.graphics.getDensity());
        stage = new Stage(viewport, spriteBatch);

        //region styles

        //ScrollPane
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.hScroll = new SpriteDrawable(new Sprite(assetManager.get("newScrollBarBackground.png", Texture.class)));
        scrollPaneStyle.background = new SpriteDrawable(new Sprite(assetManager.get("simpleborder.png", Texture.class)));

        //Button
        ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.up = new SpriteDrawable(new Sprite(assetManager.get("badlogic.jpg", Texture.class)));

        //endregion


        //I may replace this with a skin later, if I really want to (because I hate jason)

        //create the root table
        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        //componentsStack
        componentsStack = new Table();
        initComponentsStack();

        //componentsScrollPane
        componentsScrollPane = new ScrollPane(componentsStack, scrollPaneStyle);
        componentsScrollPane.setFlickScroll(false);
        componentsScrollPane.setScrollingDisabled(true, false);

        //shipDesigner
        shipDesigner = new ShipDesigner();

        //propertiesStack
        propertiesStack = new Table();

        //propertiesScrollPane
        propertiesScrollPane = new ScrollPane(propertiesStack, scrollPaneStyle);
        propertiesScrollPane.setFlickScroll(false);
        propertiesScrollPane.setScrollingDisabled(true, false);

        //save Button
        saveButton = new ImageButton(imageButtonStyle);

        //TODO add listener

        //switchButton
        switchButton = new ImageButton(imageButtonStyle);
        //TODO set background
        //TODO add listener

        //regions add the components to the rootTable
        //add the buttons
        rootTable.add(switchButton).right().size(100);
        rootTable.add(saveButton).right().size(100);
        rootTable.row();

        //add all the main controls
        rootTable.add(componentsScrollPane).left().growY().width(120);
        rootTable.add(shipDesigner).expand();
        rootTable.add(propertiesScrollPane).right().growY().width(200);

        //endregion

        //endregion

        //just some debugging
        rootTable.setDebug(true, true);

    }

    //helper method to add all components to the componentsStack
    private void initComponentsStack() {
        //TODO add all components that exist
        //at this point, this is (sadly) only the TestImp
        for (ComponentDef.ComponentInfo info : ComponentDef.componentInfos) {
            Image img = new Image(assetManager.get(info.previewImg, Texture.class));

            img.setUserObject(info);
            componentsStack.add(img).size(100).top().expand();
            componentsStack.row();
        }
    }

    //switches the view
    private void switchView() {
        //TODO implementation
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
