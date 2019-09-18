package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.spaceship.ComponentDef;
import com.nkcoding.spacegame.spaceship.ComponentType;
import com.nkcoding.spacegame.spaceship.ShipDef;


public class ShipBuilderScreen implements Screen {

    //the game
    private SpaceGame spaceGame;

    //the stage
    private Stage stage;

    //fields from game
    //spriteBatch for the stage
    private SpriteBatch spriteBatch;

    private ExtAssetManager assetManager;

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
    private VerticalGroup propertiesStack;

    //ZoomScrollOane for the shipDesigner
    private ZoomScrollPane shipDesignerZoomScrollPane;

    //the main Designer for the Ship
    private ShipDesigner shipDesigner;

    //button which saves (and probably closes?)
    private Button saveButton;

    //button which switches to code view
    private Button switchButton;
    //endregion

    //style for more propertyBoxes
    PropertyBox.PropertyBoxStyle propertyBoxStyle;

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

        Drawable background = new NinePatchDrawable(new NinePatch(assetManager.getTexture(Asset.SimpleBorder),3, 3, 3, 3));
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

        //ZoomScrollPane
        ZoomScrollPane.ZoomScrollPaneStyle zoomScrollPaneStyle = new ZoomScrollPane.ZoomScrollPaneStyle(scrollPaneStyle);

        //Button
        ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.up = assetManager.getDrawable(Asset.Badlogic);

        //Label
        Label.LabelStyle labelStyle = new Label.LabelStyle(assetManager.getBitmapFont(Asset.Consolas_18), new Color(0xffffffff));

        //TextField
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = assetManager.getBitmapFont(Asset.Consolas_18);
        textFieldStyle.fontColor = new Color(0xffffffff);
        textFieldStyle.cursor = assetManager.getDrawable(Asset.Cursor);
        textFieldStyle.background = assetManager.getDrawable(Asset.ScrollBarBackground);

        //PropertyBox
        propertyBoxStyle = new PropertyBox.PropertyBoxStyle();
        propertyBoxStyle.background = background;
        propertyBoxStyle.textFieldStyle = textFieldStyle;
        propertyBoxStyle.labelStyle = labelStyle;
        propertyBoxStyle.spacing = 10f;

        //endregion


        //I may replace this with a skin later, if I really want to (because I hate jason)

        //create the root table
        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        //componentsStack
        componentsStack = new Table();
        componentsStack.setTouchable(Touchable.enabled);
        componentsStack.top();
        initComponentsStack();

        //componentsScrollPane
        componentsScrollPane = new CustomScrollPane(componentsStack, scrollPaneStyle);
        componentsScrollPane.setFlickScroll(false);
        componentsScrollPane.setScrollingDisabled(true, false);


        //shipDesigner
        shipDesigner = new ShipDesigner(shipDef, assetManager, assetManager.getTexture(Asset.NoComponent), this::selectedComponentChanged);
        shipDesignerZoomScrollPane = new ZoomScrollPane(shipDesigner, zoomScrollPaneStyle);
        shipDesignerZoomScrollPane.setFlickScroll(false);
        shipDesignerZoomScrollPane.setFadeScrollBars(false);
        shipDesignerZoomScrollPane.setOverscroll(false, false);

        //propertiesStack
        propertiesStack = new VerticalGroup();
        propertiesStack.grow();

        //propertiesScrollPane
        propertiesScrollPane = new CustomScrollPane(propertiesStack, scrollPaneStyle);
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

        //add all the main controls
        rootTable.add(componentsScrollPane).left().growY();
        rootTable.add(shipDesignerZoomScrollPane).grow();
        rootTable.add(propertiesScrollPane).right().width(200).growY();

        //endregion

        //endregion

        //region drag and drop for the Components
        DragAndDrop componentsDragAndDrop = new DragAndDrop();
        //add the componentsStack as a source
        componentsDragAndDrop.addSource(new DragAndDrop.Source(componentsStack) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                Actor actor = componentsStack.hit(x, y, false);
                if (!(actor instanceof Image)) return null;

                Image image = (Image)actor;
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
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
                    Image dragActor = new Image(assetManager.getTexture(def.getPreviewImage()));
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
                ComponentDef def = (ComponentDef)payload.getObject();
                shipDesigner.drop(def, x, y, pointer);
                shipDesigner.setSelectedComponent(def);
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

        //endregion


        //just some debugging
        rootTable.setDebug(false, true);
    }

    //helper method to add all components to the componentsStack
    private void initComponentsStack() {
        //at this point, this is (sadly) only the TestImp
        for (ComponentType type : ComponentType.values()) {
            ComponentDef.ComponentInfo info = ComponentDef.componentInfos.get(type);
            Image img = new Image(assetManager.getTexture(info.previewImg));

            img.setUserObject(info);
            componentsStack.add(img).width(ShipDesigner.COMPONENT_SIZE * info.width).height(ShipDesigner.COMPONENT_SIZE * info.height).pad(10, 10, 0, 10).top();
            componentsStack.row();
        }
    }

    private void selectedComponentChanged(ComponentDef def) {
        //update the property stack
        //TODO add name stuff
        //TODO add better version
        propertiesStack.clear();
        if (def != null) {
            def.properties.forEach((name, data) -> {
                Container<PropertyBox> container = new Container<>(new PropertyBox(propertyBoxStyle, name, data));
                container.pad(10, 10, 0, 10).fill();
                propertiesStack.addActor(container);
            });
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
