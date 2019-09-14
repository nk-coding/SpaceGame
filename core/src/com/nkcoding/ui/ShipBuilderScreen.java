package com.nkcoding.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class ShipBuilderScreen implements Screen {

    //fields for ui elements
    //table that contains all the other controls
    private Table rootTable;

    //CodeEditor for the file with all methods
    private CodeEditor codeEditor;

    //ScrollPane for the componentsStack
    private ScrollPane componentsScrollPane;

    //Stack for the possible components
    private Stack componentsStack;

    //ScrollPane for the propertiesStack
    private ScrollPane propertiesScrollPane;

    //Stack for the external properties for the selected Component
    private Stack propertiesStack;

    //the main Designer for the Ship
    private ShipDesigner shipDesigner;


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

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
}
