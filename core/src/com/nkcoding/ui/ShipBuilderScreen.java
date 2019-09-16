package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.spaceship.ComponentDef;
import com.nkcoding.spacegame.spaceship.ShipDef;

public class ShipBuilderScreen implements Screen {

    //how much space between the components
    private static final float COMPONENTS_SPACING = 10f;

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

    //ZoomScrollOane for the shipDesigner
    private ZoomScrollPane shipDesignerZoomScrollPane;

    //the main Designer for the Ship
    private ShipDesigner shipDesigner;

    //button which saves (and probably closes?)
    private Button saveButton;

    //button which switches to code view
    private Button switchButton;
    //endregion

    //normal (ship) view?
    private boolean isShipView = true;

    //region data
    ShipDef shipDef;
    //endregion

    //constructor
    public ShipBuilderScreen(SpaceGame spaceGame) {
        //debug
        shipDef = new ShipDef();


        this.spaceGame = spaceGame;
        this.spriteBatch = spaceGame.getBatch();
        this.assetManager = spaceGame.getAssetManager();

        //region create the stage with and all its components
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(0.6f / Gdx.graphics.getDensity());
        System.out.println(Gdx.graphics.getDensity());
        stage = new Stage(viewport, spriteBatch);
        Gdx.input.setInputProcessor(stage);

        //region styles

        //ScrollPane
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        //scrollPaneStyle.background = new SpriteDrawable(new Sprite(assetManager.get("simpleborder.png", Texture.class)));
        scrollPaneStyle.vScrollKnob = new SpriteDrawable(new Sprite(assetManager.get("scrollBarThumb.png", Texture.class)));
        scrollPaneStyle.vScroll = new SpriteDrawable(new Sprite(assetManager.get("newScrollBarBackground.png", Texture.class)));
        scrollPaneStyle.hScrollKnob = new SpriteDrawable(new Sprite(assetManager.get("scrollBarThumb.png", Texture.class)));
        scrollPaneStyle.hScroll = new SpriteDrawable(new Sprite(assetManager.get("newScrollBarBackground.png", Texture.class)));

        //ZoomScrollPane
        ZoomScrollPane.ZoomScrollPaneStyle zoomScrollPaneStyle = new ZoomScrollPane.ZoomScrollPaneStyle(scrollPaneStyle);

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
        componentsStack.setTouchable(Touchable.enabled);
        initComponentsStack();

        //componentsScrollPane
        componentsScrollPane = new ScrollPane(componentsStack, scrollPaneStyle);
        componentsScrollPane.setFlickScroll(false);
        componentsScrollPane.setScrollingDisabled(true, false);


        //shipDesigner
        shipDesigner = new ShipDesigner(shipDef, assetManager, assetManager.get("noComponent.png", Texture.class));
        shipDesignerZoomScrollPane = new ZoomScrollPane(shipDesigner, zoomScrollPaneStyle);
        shipDesignerZoomScrollPane.setFlickScroll(false);
        shipDesignerZoomScrollPane.setFadeScrollBars(false);
        shipDesignerZoomScrollPane.setOverscroll(false, false);

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
        rootTable.add();
        rootTable.add(switchButton).right().size(50);
        rootTable.add(saveButton).right().size(100);
        rootTable.row();

        //add all the main controls
        rootTable.add(componentsScrollPane).left().growY().width(120);
        rootTable.add(shipDesignerZoomScrollPane).grow();
        rootTable.add(propertiesScrollPane).right().growY().width(200);

        //endregion

        //endregion

        //drag and drop for the Components
        DragAndDrop componentsDragAndDrop = new DragAndDrop();
        //add the componentsStack as a source
        componentsDragAndDrop.addSource(new DragAndDrop.Source(componentsStack) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                //calculate the selected
                int selected = (int)((componentsStack.getHeight() - y) / (ShipDesigner.COMPONENT_SIZE + COMPONENTS_SPACING));
                if (selected < 0 || selected >= ComponentDef.componentInfos.size()) return null;

                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                Image image = (Image)componentsStack.getChild(selected);
                ComponentDef.ComponentInfo info = (ComponentDef.ComponentInfo)image.getUserObject();
                payload.setObject(new ComponentDef(info));
                Image dragActor = new Image(image.getDrawable());
                float componentSize = ShipDesigner.COMPONENT_SIZE * shipDesignerZoomScrollPane.getZoom();
                dragActor.setSize(
                         componentSize * info.width,
                        componentSize * info.height);
                payload.setDragActor(dragActor);
                componentsDragAndDrop.setDragActorPosition(dragActor.getWidth() - componentSize / 2, -dragActor.getHeight() + componentSize / 2);
                return payload;
            }
        });

        //add the shipDesigner as a source
        componentsDragAndDrop.addSource(new DragAndDrop.Source(shipDesigner) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                ComponentDef def = shipDesigner.getSelectedComponent();
                if (def != null) {
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();
                    payload.setObject(def);
                    float componentSize = ShipDesigner.COMPONENT_SIZE * shipDesignerZoomScrollPane.getZoom();
                    Image dragActor = new Image(assetManager.get(def.getPreviewImage(), Texture.class));
                    dragActor.setRotation(def.getRotation());
                    dragActor.setSize(
                            componentSize * def.getWidth(),
                            componentSize * def.getHeight());
                    payload.setDragActor(dragActor);
                    componentsDragAndDrop.setDragActorPosition(dragActor.getWidth() - componentSize / 2, -dragActor.getHeight() + componentSize / 2);
                    return payload;
                }
                else return null;
            }
        });

        //add the shipDesigner as a target
        componentsDragAndDrop.addTarget(new DragAndDrop.Target(shipDesigner) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return shipDesigner.drag((ComponentDef)payload.getObject(), x, y, pointer);
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                shipDesigner.drop((ComponentDef)payload.getObject(), x, y, pointer);
            }
        });

        //add the componentsStack as a target

        componentsDragAndDrop.addTarget(new DragAndDrop.Target(componentsStack) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                if (source.getActor() == shipDesigner) shipDesigner.removeComponent((ComponentDef)payload.getObject());
            }
        });


        //just some debugging
        rootTable.setDebug(true, true);

    }

    //helper method to add all components to the componentsStack
    private void initComponentsStack() {
        //TODO add all components that exist
        //at this point, this is (sadly) only the TestImp
        for (ComponentDef.ComponentInfo info : ComponentDef.componentInfos.values()) {
            Image img = new Image(assetManager.get(info.previewImg, Texture.class));

            img.setUserObject(info);
            componentsStack.add(img).size(ShipDesigner.COMPONENT_SIZE).top().expand();
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
