package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.spaceship.ComponentDef;
import com.nkcoding.spacegame.spaceship.ComponentType;
import com.nkcoding.spacegame.spaceship.ExternalPropertyData;
import com.nkcoding.spacegame.spaceship.ShipDef;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;


public class ShipBuilderScreen implements Screen {

    //the game
    private SpaceGame spaceGame;

    //the stage
    private Stage stage;

    //fields from game
    //spriteBatch for the stage
    private SpriteBatch spriteBatch;

    private ExtAssetManager assetManager;

    //the main tables
    private Table shipRootTable;
    private Table codeRootTable;

    //Stack for the possible components
    private Table componentsStack;

    //Stack for the external properties for the selected Component
    private VerticalGroup propertiesVerticalGroup;

    //ZoomScrollOane for the shipDesigner
    private ZoomScrollPane shipDesignerZoomScrollPane;

    //the main Designer for the Ship
    private ShipDesigner shipDesigner;

    //endregion

    //style for more propertyBoxes
    PropertyBox.PropertyBoxStyle propertyBoxStyle;

    //compiler to check the code
    private Compiler compiler;

    //normal (ship) view?
    private boolean isShipView = true;

    //region data
    private ShipDef shipDef;
    //endregion

    //constructor
    public ShipBuilderScreen(SpaceGame spaceGame) {
        //debug
        FileHandle saveGame = Gdx.files.local("saveGame.json");
        if (saveGame.exists()) {
            JsonReader jsonReader = new JsonReader();
            shipDef = ShipDef.fromJson(jsonReader.parse(saveGame));
        }
        else shipDef = new ShipDef();

        //create new compiler
        //create the external method statements for the components
        HashMap<String, ExternalPropertyData> externalPropertyDatas = new HashMap<>();
        for(ComponentType com : ComponentType.values()) {
            for(ExternalPropertyData data : com.propertyDefs) {
                if (!externalPropertyDatas.containsKey(data.name)) {
                    externalPropertyDatas.put(data.name, data);
                }
            }
        }
        ArrayList<MethodDefinition> methodDefinitions = new ArrayList<>();
        for (ExternalPropertyData data : externalPropertyDatas.values()) {
            data.addExternalMethodDefs(methodDefinitions);
        }
        //create the new compiler
        compiler = new Compiler(new String[0], methodDefinitions.toArray(MethodDefinition[]::new));


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

        //Label
        Label.LabelStyle labelStyle = new Label.LabelStyle(assetManager.getBitmapFont(Asset.Consolas_18), new Color(0xffffffff));

        //TextField
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = assetManager.getBitmapFont(Asset.Consolas_18);
        textFieldStyle.fontColor = new Color(0xffffffff);
        textFieldStyle.cursor = assetManager.getDrawable(Asset.Cursor);
        textFieldStyle.selection = assetManager.getDrawable(Asset.Selection);
        Drawable textFieldBackground = assetManager.getDrawable(Asset.ScrollBarBackground);
        textFieldBackground.setLeftWidth(5);
        textFieldBackground.setRightWidth(5);
        textFieldBackground.setTopHeight(5);
        textFieldBackground.setBottomHeight(5);
        textFieldStyle.background = textFieldBackground;

        //PropertyBox
        propertyBoxStyle = new PropertyBox.PropertyBoxStyle();
        propertyBoxStyle.background = background;
        propertyBoxStyle.textFieldStyle = textFieldStyle;
        propertyBoxStyle.illegalInputColor = new Color(0xff0000ff);
        propertyBoxStyle.legalInputColor = new Color(0xffffffff);
        propertyBoxStyle.labelStyle = labelStyle;
        propertyBoxStyle.spacing = 10f;

        //endregion


        //I may replace this with a skin later, if I really want to (because I hate json)

        //create the root table
        //region ship designer ui
        //table that contains all the other controls
        shipRootTable = new Table();
        shipRootTable.setFillParent(true);
        stage.addActor(shipRootTable);

        //componentsStack
        componentsStack = new Table();
        componentsStack.setTouchable(Touchable.enabled);
        componentsStack.top();
        initComponentsStack();

        //componentsScrollPane
        //ScrollPane for the componentsStack
        ScrollPane componentsScrollPane = new CustomScrollPane(componentsStack, scrollPaneStyle);
        componentsScrollPane.setFlickScroll(false);
        componentsScrollPane.setScrollingDisabled(true, false);


        //shipDesigner
        shipDesigner = new ShipDesigner(shipDef, assetManager, assetManager.getTexture(Asset.NoComponent), this::selectedComponentChanged);
        shipDesignerZoomScrollPane = new ZoomScrollPane(shipDesigner, zoomScrollPaneStyle);
        shipDesignerZoomScrollPane.setFlickScroll(false);
        shipDesignerZoomScrollPane.setFadeScrollBars(false);
        shipDesignerZoomScrollPane.setOverscroll(false, false);

        //propertiesStack
        propertiesVerticalGroup = new VerticalGroup();
        propertiesVerticalGroup.grow();

        //propertiesScrollPane
        //ScrollPane for the propertiesStack
        ScrollPane propertiesScrollPane = new CustomScrollPane(propertiesVerticalGroup, scrollPaneStyle);
        propertiesScrollPane.setFlickScroll(false);
        propertiesScrollPane.setScrollingDisabled(true, false);
        propertiesScrollPane.setFadeScrollBars(false);


        //save Button
        //button which saves (and probably closes?)
        ImageButton saveButton = new ImageButton(assetManager.getDrawable(Asset.SaveSymbol));
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                save();
            }
        });

        //switchButton
        //button which switches to code view
        ImageButton switchButton = new ImageButton(assetManager.getDrawable(Asset.CodeSymbol));
        switchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                switchView();
            }
        });

        //rightLayoutTable
        Table rightLayoutTable = new Table();
        rightLayoutTable.add(propertiesScrollPane).colspan(2).grow();
        rightLayoutTable.row();
        rightLayoutTable.add(saveButton).size(40, 40).growX().pad(10).right();
        rightLayoutTable.add(switchButton).size(40, 40).pad(10).right();

        //add the components to the rootTable
        //add the buttons

        //add all the main controls
        shipRootTable.add(componentsScrollPane).left().growY();
        shipRootTable.add(shipDesignerZoomScrollPane).grow();
        shipRootTable.add(rightLayoutTable).right().width(200).growY();


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
                ComponentType type = (ComponentType) image.getUserObject();
                payload.setObject(new ComponentDef(type));
                Image dragActor = new Image(image.getDrawable());
                float componentSize = ShipDesigner.COMPONENT_SIZE * shipDesignerZoomScrollPane.getZoom();
                dragActor.setSize(
                         componentSize * type.width,
                        componentSize * type.height);
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

        //endregion

        //region code editor ui

        codeRootTable = new Table();
        codeRootTable.setFillParent(true);
        //switch button
        ImageButton closeButton = new ImageButton(assetManager.getDrawable(Asset.CloseSymbol));
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                switchView();
            }
        });

        //code editor
        //CodeEditor for the file with all methods
        CodeEditor codeEditor = new CodeEditor(textFieldStyle, scrollPaneStyle, new ScriptColorParser());

        codeRootTable.add(codeEditor).grow();
        codeRootTable.add(closeButton).size(40, 40).bottom().pad(10);


        //endregion

        //just some debugging
        shipRootTable.setDebug(false, true);
    }

    //helper method to add all components to the componentsStack
    private void initComponentsStack() {
        //at this point, this is (sadly) only the TestImp
        for (ComponentType type : ComponentType.values()) {
            Image img = new Image(assetManager.getTexture(type.previewImg));

            img.setUserObject(type);
            componentsStack.add(img).width(ShipDesigner.COMPONENT_SIZE * type.width).height(ShipDesigner.COMPONENT_SIZE * type.height).pad(10, 10, 0, 10).top().left();
            componentsStack.row();
        }
    }

    private void selectedComponentChanged(ComponentDef def) {
        //update the property stack
        //TODO add name stuff
        //TODO add better version
        propertiesVerticalGroup.getChildren().forEach(actor -> ((PropertyBox)((Container)actor).getActor()).save());
        propertiesVerticalGroup.clear();
        if (def != null) {
            def.properties.forEach((name, data) -> {
                Container<PropertyBox> container = new Container<>(new PropertyBox(propertyBoxStyle, name, data));
                container.pad(10, 10, 0, 10).fill();
                propertiesVerticalGroup.addActor(container);
            });
        }
    }

    //switches the view
    private void switchView() {
        //TODO complete implementation
        if(isShipView) {
            stage.addActor(codeRootTable);
            shipRootTable.remove();
        }
        else {
            stage.addActor(shipRootTable);
            codeRootTable.remove();
        }
        isShipView = !isShipView;
    }

    private void save() {
        //debug
        Json json = new Json(JsonWriter.OutputType.json);
        FileHandle handle = Gdx.files.local("saveGame.json");
        try (Writer writer = handle.writer(false)) {
            json.setWriter(writer);
            shipDef.toJson(json);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
