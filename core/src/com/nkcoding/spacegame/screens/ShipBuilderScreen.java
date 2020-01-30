package com.nkcoding.spacegame.screens;

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
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.interpreter.MethodStatement;
import com.nkcoding.interpreter.compiler.CompileException;
import com.nkcoding.interpreter.compiler.Compiler;
import com.nkcoding.interpreter.compiler.NormalMethodDefinition;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.ExtAssetManager;
import com.nkcoding.spacegame.SaveGameManager;
import com.nkcoding.spacegame.SpaceGame;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentDef;
import com.nkcoding.spacegame.simulation.spaceship.components.ComponentType;
import com.nkcoding.spacegame.simulation.spaceship.properties.ExternalPropertyData;
import com.nkcoding.ui.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;


public class ShipBuilderScreen implements Screen {

    //region data
    public final ShipDef shipDef;
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
    private final Table shipInfoTable;
    private final Label shipInfoLabel;
    //temporary storage for PropertyBoxes which are not necessary because of a ComponentDef change
    //the can be updated and used later
    private final ArrayDeque<Actor> oldPropertyBoxes = new ArrayDeque<>();
    //ZoomScrollPane for the shipDesigner
    private final ZoomScrollPane shipDesignerZoomScrollPane;
    //the main Designer for the Ship
    private final ShipDesigner shipDesigner;
    //button to switch views
    private final Drawable switchButton_ok;
    private final Drawable switchButton_error;
    private final ImageButton switchButton;
    private final CodeEditor codeEditor;
    //the table with the component / ship name
    private final Table basicInfoTable;
    private final Label componentNameLabel;
    private final ImageButton rotateButton;
    private final TextField nameTextField;
    //check Button
    private final Drawable checkButton_ok;
    private final Drawable checkButton_error;
    private final Drawable checkButton_actionNecessary;
    private final ImageButton checkButton;

    //endregion
    //error log
    private final TextField errorLog;
    //style for more propertyBoxes
    PropertyBox.PropertyBoxStyle propertyBoxStyle;
    //compiler to check the code
    private Compiler compiler;
    //the compiled code
    private MethodStatement[] methods = new MethodStatement[0];
    private HashMap<String, NormalMethodDefinition> methodPositions = new HashMap<>();
    //normal (ship) view?
    private boolean isShipView = true;
    private final ScrollPane propertiesScrollPane;
    private boolean shipValidated;
    private boolean codeValidated;
    //endregion

    //constructor
    public ShipBuilderScreen(SpaceGame spaceGame, ShipDef shipDef) {
        this.shipDef = shipDef;
        //create new compiler
        compiler = shipDef.createCompiler("");


        this.spaceGame = spaceGame;
        this.spriteBatch = spaceGame.getBatch();
        this.assetManager = spaceGame.getAssetManager();
        //region create the stage with and all its components
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(0.75f / Gdx.graphics.getDensity());
        System.out.println(Gdx.graphics.getDensity());
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

        //ZoomScrollPane
        ZoomScrollPane.ZoomScrollPaneStyle zoomScrollPaneStyle = new ZoomScrollPane.ZoomScrollPaneStyle(scrollPaneStyle);

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

        //PropertyBox
        propertyBoxStyle = new PropertyBox.PropertyBoxStyle();
        propertyBoxStyle.background = background;
        propertyBoxStyle.codeButtonDrawable = assetManager.getDrawable(Asset.CodeSymbol);
        propertyBoxStyle.getterButtonDrawable = assetManager.getDrawable(Asset.GetterSymbol);
        propertyBoxStyle.setterButtonDrawable = assetManager.getDrawable(Asset.SetterSymbol);
        propertyBoxStyle.textFieldStyle = textFieldStyle;
        propertyBoxStyle.illegalInputColor = new Color(0xff0000ff);
        propertyBoxStyle.legalInputColor = new Color(0xffffffff);
        propertyBoxStyle.labelStyle = labelStyleSmall;
        propertyBoxStyle.spacing = 10f;

        //CodeEditor
        CodeEditor.CodeEditorStyle codeEditorStyle = new CodeEditor.CodeEditorStyle(textFieldStyle,
                assetManager.getDrawable(Asset.LightBackground), scrollPaneStyle, new ScriptColorParser());

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
        UIHelper.activateScrollOnHover(componentsScrollPane);

        //shipDesigner
        shipDesigner = new ShipDesigner(shipDef, assetManager, assetManager.getTexture(Asset.NoComponent), assetManager.getTexture(Asset.Selection), this::selectedComponentChanged);
        shipDesignerZoomScrollPane = new ZoomScrollPane(shipDesigner, zoomScrollPaneStyle);
        shipDesignerZoomScrollPane.setFlickScroll(true);
        shipDesignerZoomScrollPane.setFadeScrollBars(false);
        shipDesignerZoomScrollPane.setOverscroll(false, false);
        UIHelper.activateScrollOnHover(shipDesignerZoomScrollPane);

        //basicInfoTable
        componentNameLabel = new Label("Ship", labelStyleBig);

        rotateButton = new ImageButton(assetManager.getDrawable(Asset.RotateSymbol));
        rotateButton.setVisible(false);
        rotateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                shipDesigner.rotateSelectedComponent();
            }
        });

        final Label nameLabel = new Label("name", labelStyleSmall);

        nameTextField = new TextField("", textFieldStyle);
        nameTextField.setTextFieldListener((textField, c) -> {
            verifyName();
        });
        verifyName();

        basicInfoTable = new Table();
        basicInfoTable.setBackground(background);
        basicInfoTable.pad(10, 10, 10, 10);
        basicInfoTable.add(componentNameLabel).growX().left();
        basicInfoTable.add(rotateButton).size(40, 40).right();
        basicInfoTable.row();
        basicInfoTable.add(nameLabel).left().colspan(2).pad(10, 10, 10, 0);
        basicInfoTable.row();
        basicInfoTable.add(nameTextField).left().colspan(2).pad(0, 10, 10, 0).fillX();

        final Container<Table> basicInfoContainer = new Container<>(basicInfoTable).pad(10, 10, 0, 10).fillX();


        //propertiesVerticalGroup
        propertiesVerticalGroup = new VerticalGroup();
        propertiesVerticalGroup.addActor(basicInfoContainer);
        propertiesVerticalGroup.grow();

        shipInfoTable = new Table();
        shipInfoLabel = new Label("", labelStyleSmall);
        Container<Label> shipInfoContainer = new Container<>(shipInfoLabel);
        shipInfoContainer.fillX();
        shipInfoContainer.setBackground(background);
        shipInfoTable.add(shipInfoContainer).left().top().growX().expandY().pad(10);

        //propertiesScrollPane
        //ScrollPane for the propertiesStack
        propertiesScrollPane = new CustomScrollPane(shipInfoTable, scrollPaneStyle);
        propertiesScrollPane.setFlickScroll(false);
        propertiesScrollPane.setScrollingDisabled(true, false);
        propertiesScrollPane.setFadeScrollBars(false);
        UIHelper.activateScrollOnHover(propertiesScrollPane);

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
        shipRootTable.add(rightLayoutTable).right().width(450).growY();

        addDragAndDropListeners();

        //endregion

        //region code editor ui

        codeEditor = new CodeEditor(codeEditorStyle);
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
        selectedComponentChanged(null, null);
    }

    private void addDragAndDropListeners() {
        //region drag and drop for the Components
        DragAndDrop componentsDragAndDrop = new DragAndDrop();
        componentsDragAndDrop.setKeepWithinStage(false);
        //add the componentsStack as a source
        componentsDragAndDrop.addSource(new DragAndDrop.Source(componentsStack) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                Actor actor = componentsStack.hit(x, y, false);
                if (!(actor instanceof Image)) return null;

                Image image = (Image) actor;
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                ComponentType type = (ComponentType) image.getUserObject();
                payload.setObject(new ComponentDef(type));
                Image dragActor = new Image(image.getDrawable());
                float componentSize = ShipDesigner.COMPONENT_SIZE * shipDesignerZoomScrollPane.getZoom();
                dragActor.setSize(
                        componentSize * type.width,
                        componentSize * type.height);
                payload.setDragActor(dragActor);
                componentsDragAndDrop.setDragActorPosition(dragActor.getWidth() - componentSize / 2, -componentSize / 2);
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
                    Image dragActor = new Image(assetManager.getTexture(def.getDefaultTexture()));
                    dragActor.setRotation(def.getRotation() * 90);
                    dragActor.setSize(
                            componentSize * def.getWidth(),
                            componentSize * def.getHeight());
                    payload.setDragActor(dragActor);
                    switch (def.getRotation()) {
                        case 0:
                            componentsDragAndDrop.setDragActorPosition(componentSize * (def.getWidth() - 0.5f), -componentSize / 2);
                            break;
                        case 1:
                            componentsDragAndDrop.setDragActorPosition(componentSize * (def.getWidth() + def.getHeight() - 0.5f), -componentSize / 2);
                            break;
                        case 2:
                            componentsDragAndDrop.setDragActorPosition(componentSize * (2 * def.getWidth() - 0.5f), componentSize * (def.getHeight() - 0.5f));
                            break;
                        case 3:
                            componentsDragAndDrop.setDragActorPosition(componentSize * (def.getHeight() - def.getWidth() - 0.5f), componentSize * (def.getWidth() - 0.5f));
                            break;
                    }
                    return payload;
                } else return null;
            }
        });

        //add the shipDesigner as a target
        componentsDragAndDrop.addTarget(new DragAndDrop.Target(shipDesigner) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return shipDesigner.drag((ComponentDef) payload.getObject(), x, y, pointer);
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                ComponentDef def = (ComponentDef) payload.getObject();
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
                if (source.getActor() == shipDesigner) shipDesigner.removeComponent((ComponentDef) payload.getObject());
            }
        });

        //endregion
    }

    //helper method to add all components to the componentsStack
    private void initComponentsStack() {
        //at this point, this is (sadly) only the TestImp
        for (ComponentType type : ComponentType.values()) {
            Image img = new Image(assetManager.getTexture(type.defaultTexture));

            img.setUserObject(type);
            componentsStack.add(img).width(ShipDesigner.COMPONENT_SIZE * type.width)
                    .height(ShipDesigner.COMPONENT_SIZE * type.height)
                    .pad(10, 10, 0, 10).top().left();
            componentsStack.row();
        }
    }

    private void selectedComponentChanged(ComponentDef newDef, ComponentDef oldDef) {
        //save
        saveComponentDef(oldDef);
        //update the property stack
        Map<String, ExternalPropertyData> properties;
        if (newDef != null) {
            propertiesScrollPane.setActor(propertiesVerticalGroup);
            selectComponent(newDef);
        } else {
            propertiesScrollPane.setActor(shipInfoTable);
            selectShip();
        }
    }

    /**
     * the case that a real component was selected
     */
    private void selectComponent(ComponentDef newDef) {
        Map<String, ExternalPropertyData> properties;
        properties = newDef.properties;
        nameTextField.setText(newDef.getName());
        componentNameLabel.setText(newDef.getType().toString());
        rotateButton.setVisible(true);

        int oldCount = propertiesVerticalGroup.getChildren().size - 1;
        int newCount = properties.size();
        //remove PropertyBoxes if necessary
        if (newCount < oldCount) {
            Actor[] actors = new Actor[oldCount - newCount];
            for (int x = newCount; x < oldCount; x++) {
                actors[x - newCount] = propertiesVerticalGroup.getChild(x + 1);
            }
            for (Actor actor : actors) {
                propertiesVerticalGroup.removeActor(actor);
                oldPropertyBoxes.add(actor);
            }
        }

        int x = 0;
        for (ExternalPropertyData data : properties.values()) {
            if (x < oldCount) {
                //the component exists
                Container container = (Container) propertiesVerticalGroup.getChild(x + 1);
                PropertyBox propertyBox = (PropertyBox) container.getActor();
                propertyBox.update(newDef.getName(), data);
            } else {
                if (oldPropertyBoxes.isEmpty()) {
                    //the component does not exist yet, and there is no one available on the stack
                    Container<PropertyBox> container = new Container<>(new PropertyBox(propertyBoxStyle, data.name, data, methodPositions) {
                        @Override
                        public void codeButtonClicked() {
                            if (methodPositions.containsKey(this.getHandlerName())) {
                                switchView();

                                RunnableAction action = new RunnableAction();
                                action.setRunnable(() -> {
                                    codeEditor.moveTo(methodPositions.get(this.getHandlerName()).getLine());
                                });
                                codeEditor.addAction(action);
                            } else if (!this.getHandlerName().isBlank()) {
                                //actually use \n because it leads to way better results
                                String codeToAdd = String.format("\n\nvoid %s(%s value) {\n   \n}", this.getHandlerName(), getDataType().name);
                                codeEditor.setText(codeEditor.getText() + codeToAdd);
                                switchView();

                                RunnableAction action = new RunnableAction();
                                action.setRunnable(codeEditor::moveToEnd);
                                codeEditor.addAction(action);
                            }
                        }
                    });
                    container.pad(10, 10, 0, 10).fill();
                    propertiesVerticalGroup.addActor(container);
                } else {
                    //the component does not exist yet, but there is one available on the stack
                    Container container = (Container) oldPropertyBoxes.pop();
                    PropertyBox propertyBox = (PropertyBox) container.getActor();
                    propertyBox.update(newDef.getName(), data);
                    propertiesVerticalGroup.addActor(container);
                }
            }
            x++;
        }

        //validate the color of the nameTextField
        verifyName();
    }

    /**
     * the case that no Component -> the ship was selected
     * update the shipInfoTable
     */
    private void selectShip() {
        StringBuilder sb = new StringBuilder();
        sb.append("Amount of components: ");
        sb.append(shipDef.getComponentCount());
        int maxProduction = 0;
        int maxConsumption = 0;
        int mass = 0;
        for (ComponentDef def : shipDef.componentDefs) {
            if (def.getType().maxPowerLevel < 0) {
                maxProduction -= def.getType().maxPowerLevel;
            } else {
                maxConsumption += def.getType().maxPowerLevel;
            }
            mass += def.getType().mass;
        }
        validate();
        sb.append("\nMax power production: " + maxProduction);
        sb.append("\nMax power consumption: " + maxConsumption);
        sb.append("\nMass: " + mass);
        sb.append("\n");
        sb.append("\nShip status: " + shipValidated);
        sb.append("\nCode status: " + codeValidated);
        shipInfoLabel.setText(sb.toString());
    }

    //checks if the name is ok
    private void verifyName() {
        nameTextField.setColor(shipDef.verifyComponentName(shipDesigner.getSelectedComponent(), nameTextField.getText()) ?
                new Color(0xffffffff) : new Color(0xff0000ff));
    }

    //switches the view
    private void switchView() {
        if (isShipView) {
            stage.addActor(codeRootTable);
            shipRootTable.remove();
        } else {
            parse(true);

            stage.addActor(shipRootTable);
            codeRootTable.remove();
            //update PropertyBoxes
            SnapshotArray<Actor> children = propertiesVerticalGroup.getChildren();
            for (int x = 1; x < children.size; x++) {
                Actor actor = children.get(x);
                ((PropertyBox) ((Container) actor).getActor()).verify();
            }
        }
        shipDesigner.setSelectedComponent(null);
        isShipView = !isShipView;
    }

    //compiles and updates error log
    private boolean parse(boolean updateMethodsMap) {
        compiler.update(codeEditor.getText().split("\\r?\\n"));
        try {
            errorLog.setText("");
            long start = System.nanoTime();
            methods = compiler.compile().methods;
            System.out.println("compile time: " + (System.nanoTime() - start) / 1000000.0 + "ms");
            if (updateMethodsMap) {
                //update the map, update references and remove old ones
                methodPositions.clear();
                for (MethodStatement statement : methods) {
                    NormalMethodDefinition definition = ((NormalMethodDefinition) statement.getDefinition());
                    methodPositions.put(definition.getName(), definition);
                }
            }
            ImageButton.ImageButtonStyle style = checkButton.getStyle();
            style.imageUp = checkButton_ok;
            checkButton.setStyle(style);
            style = switchButton.getStyle();
            style.imageUp = switchButton_ok;
            switchButton.setStyle(style);
            codeValidated = true;
            return true;
        } catch (CompileException e) {
            errorLog.setText(e.toString());
            ImageButton.ImageButtonStyle style = checkButton.getStyle();
            style.imageUp = checkButton_error;
            checkButton.setStyle(style);
            style = switchButton.getStyle();
            style.imageUp = switchButton_error;
            switchButton.setStyle(style);
            codeValidated = false;
            return false;
        }
    }

    //saves the verticalPropertiesGroup
    private void saveComponentDef(ComponentDef def) {
        SnapshotArray<Actor> children = propertiesVerticalGroup.getChildren();
        //save name
        if (def != null) {
            def.setName(nameTextField.getText());
        }

        for (int x = 1; x < children.size; x++) {
            Actor actor = children.get(x);
            ((PropertyBox) ((Container) actor).getActor()).save();
        }
    }

    //saves the current state
    private void save() {
        shipDef.code = codeEditor.getText();
        saveComponentDef(shipDesigner.getSelectedComponent());
        validate();

        shipDef.setValidated(codeValidated && shipValidated);
        SaveGameManager.save();
    }

    private void validate() {
        //verify the ship
        codeValidated = parse(true);
        shipValidated = shipDef.verifyComponentProperties(methodPositions) && shipDef.verifyNames();
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
