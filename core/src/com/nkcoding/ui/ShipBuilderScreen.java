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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.compiler.CompileException;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.compiler.MethodDefinition;
import com.nkcoding.interpreter.compiler.NormalMethodDefinition;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SaveGameManager;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.spaceship.ComponentDef;
import com.nkcoding.spacegame.spaceship.ComponentType;
import com.nkcoding.spacegame.spaceship.ExternalPropertyData;
import com.nkcoding.spacegame.spaceship.ShipDef;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;


public class ShipBuilderScreen implements Screen {

    //the game
    private final SpaceGame spaceGame;

    //the stage
    private final Stage stage;

    //fields from game
    //spriteBatch for the stage
    private final SpriteBatch spriteBatch;

    private final ExtAssetManager assetManager;

    //the main tables
    private final Table shipRootTable;
    private final Table codeRootTable;

    //Stack for the possible components
    private final Table componentsStack;

    //Stack for the external properties for the selected Component
    private final VerticalGroup propertiesVerticalGroup;

    //temporary storage for PropertyBoxes which are not necessary because of a ComponentDef change
    //the can be updated and used later
    private final ArrayDeque<Actor> oldPropertyBoxes = new ArrayDeque<>();

    //ZoomScrollOane for the shipDesigner
    private final ZoomScrollPane shipDesignerZoomScrollPane;

    //the main Designer for the Ship
    private final ShipDesigner shipDesigner;

    //button to switch views
    private final Drawable switchButton_ok;
    private final Drawable switchButton_error;
    private final ImageButton switchButton;

    private final CodeEditor codeEditor;

    //check Button
    private final Drawable checkButton_ok;
    private final Drawable checkButton_error;
    private final Drawable checkButton_actionNecessary;
    private final ImageButton checkButton;

    //error log
    private final TextField errorLog;

    //endregion

    //style for more propertyBoxes
    PropertyBox.PropertyBoxStyle propertyBoxStyle;

    //compiler to check the code
    private Compiler compiler;

    //the compiled code
    private MethodStatement[] methods = new MethodStatement[0];
    private HashMap<String, Integer> methodPositions = new HashMap<>();

    //normal (ship) view?
    private boolean isShipView = true;

    //region data
    public final ShipDef shipDef;
    //endregion

    //constructor
    public ShipBuilderScreen(SpaceGame spaceGame, ShipDef shipDef) {
        this.shipDef = shipDef;
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
        propertyBoxStyle.codeButtonDrawable = assetManager.getDrawable(Asset.CodeSymbol);
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
        switchButton_ok = assetManager.getDrawable(Asset.CodeSymbol);
        switchButton_error = assetManager.getDrawable(Asset.CodeErrorSymbol);
        switchButton = new ImageButton(switchButton_ok);
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
        shipRootTable.add(rightLayoutTable).right().width(300).growY();


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

        codeEditor = new CodeEditor(textFieldStyle, scrollPaneStyle, new ScriptColorParser());
        codeEditor.setText(shipDef.code);

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


        //error log
        errorLog = new TextField("", textFieldStyle);
        errorLog.setDisabled(true);

        //drawables for the checkButton
        checkButton_ok = assetManager.getDrawable(Asset.OkSymbol);
        checkButton_error = assetManager.getDrawable(Asset.ErrorSymbol);
        checkButton_actionNecessary = assetManager.getDrawable(Asset.ActionNecessarySymbol);
        //check Button
        checkButton = new ImageButton(checkButton_actionNecessary);
        checkButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //check if compiler has to check code again
                if (checkButton.getStyle().imageUp == checkButton_error || checkButton.getStyle().imageUp == checkButton_actionNecessary) {
                    long time = System.currentTimeMillis();
                    parse(false);
                    System.out.println(System.currentTimeMillis() - time);
                }
            }
        });

        //code editor
        //CodeEditor for the file with all methods
        codeEditor.setTextFieldListener((textFieldBase, c) -> {
            if (checkButton.getStyle().imageUp != checkButton_actionNecessary) {
                ImageButton.ImageButtonStyle style = checkButton.getStyle();
                style.imageUp = checkButton_actionNecessary;
                checkButton.setStyle(style);
            }
        });

        codeRootTable.add(codeEditor).colspan(3).grow();
        codeRootTable.row();
        codeRootTable.add(errorLog).growX().pad(10);
        codeRootTable.add(checkButton).size(40, 40).bottom().pad(10).right();
        codeRootTable.add(closeButton).size(40, 40).bottom().pad(10).right();


        //endregion

        parse(true);

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
        if (def != null) {
            propertiesVerticalGroup.getChildren().forEach(actor -> ((PropertyBox)((Container)actor).getActor()).save());

            int oldCount = propertiesVerticalGroup.getChildren().size;
            int newCount = def.properties.size();
            //remove PropertyBoxes if necessary
            if (newCount < oldCount) {
                for (int x = newCount; x < oldCount; x++) {
                    oldPropertyBoxes.add(propertiesVerticalGroup.getChild(x));
                }
                propertiesVerticalGroup.getChildren().removeRange(newCount, oldCount - 1);
            }


            System.out.println(newCount == propertiesVerticalGroup.getChildren().size);
            int x = 0;
            for (ExternalPropertyData data : def.properties.values()) {
                if (x < oldCount) {
                    //the component exists
                    Container container = (Container)propertiesVerticalGroup.getChild(x);
                    PropertyBox propertyBox = (PropertyBox)container.getActor();
                    propertyBox.update(data.name, data);
                }
                else {
                    if (oldPropertyBoxes.isEmpty()) {
                        //the component does not exist yet, and there is no one available on the stack
                        Container<PropertyBox> container = new Container<>(new PropertyBox(propertyBoxStyle, data.name, data) {
                            @Override
                            public void codeButtonClicked() {
                                methodPositions.forEach((s, l) -> System.out.println(s + ", " + l));
                                if (methodPositions.containsKey(this.getHandlerName())) {
                                    System.out.println(methodPositions.get(this.getHandlerName()));
                                }
                                else {
                                    System.out.println("does not contain");
                                }
                            }
                        });
                        container.pad(10, 10, 0, 10).fill();
                        propertiesVerticalGroup.addActor(container);
                    }
                    else {
                        //the component does not exist yet, but there is one available on the stack
                        Container container = (Container)oldPropertyBoxes.pop();
                        PropertyBox propertyBox = (PropertyBox)container.getActor();
                        propertyBox.update(data.name, data);
                    }
                }
                x++;
            }
        }
    }

    //switches the view
    private void switchView() {
        if(isShipView) {
            stage.addActor(codeRootTable);
            shipRootTable.remove();
        }
        else {
            parse(true);
            stage.addActor(shipRootTable);
            codeRootTable.remove();
        }
        isShipView = !isShipView;
    }

    //compiles and updates error log
    private void parse(boolean updateMethodsMap) {
        compiler.update(codeEditor.getText().split("\\r?\\n"));
        try {
            errorLog.setText("");
            methods = compiler.compile();
            if (updateMethodsMap) {
                System.out.println("updateMethodsMap");
                //update the map, update references and remove old ones
                methodPositions.clear();
                for (MethodStatement statement : methods) {
                    NormalMethodDefinition definition = ((NormalMethodDefinition)statement.getDefinition());
                    methodPositions.put(definition.getName(), definition.getLine());
                }
            }
            ImageButton.ImageButtonStyle style = checkButton.getStyle();
            style.imageUp = checkButton_ok;
            checkButton.setStyle(style);
            style = switchButton.getStyle();
            style.imageUp = switchButton_ok;
            switchButton.setStyle(style);
        } catch (CompileException e) {
            errorLog.setText(e.toString());
            ImageButton.ImageButtonStyle style = checkButton.getStyle();
            style.imageUp = checkButton_error;
            checkButton.setStyle(style);
            style = switchButton.getStyle();
            style.imageUp = switchButton_error;
            switchButton.setStyle(style);
        }
    }

    //saves the current state
    private void save() {
        shipDef.code = codeEditor.getText();
        propertiesVerticalGroup.getChildren().forEach(actor -> ((PropertyBox)((Container)actor).getActor()).save());
        SaveGameManager.save();
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
