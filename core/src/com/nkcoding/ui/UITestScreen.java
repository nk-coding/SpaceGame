package com.nkcoding.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nkcoding.spacegame.SpaceGame;


public class UITestScreen implements Screen {
	SpaceGame spaceGame;

	SpriteBatch batch;

	Stage stage;
	MultiColorTextArea textArea;
	ScrollPane scrollPane;

	Array<String> listViewItems1;
	Array<String> listViewItems2;

	List<String> list1;
	List<String> list2;

	//constructor
	public UITestScreen(SpaceGame spaceGame) {
		this.spaceGame = spaceGame;
		this.batch = spaceGame.getBatch();
		//create the stage
		stage = new Stage(new ScreenViewport(), batch);
		Gdx.input.setInputProcessor(stage);

		//create the bitmapFont
		BitmapFont consolasFont = new BitmapFont(Gdx.files.internal("consolas.fnt"));


		NinePatchDrawable ninePatch = new NinePatchDrawable(new NinePatch(new Texture(Gdx.files.internal("simpleborder.png")),3, 3, 3, 3));
		final Label.LabelStyle labelStyle = new Label.LabelStyle(consolasFont, new Color(1, 1, 1, 1));
		/*
		//create a table
		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);
		table.setDebug(false);

		table.setBackground(ninePatch);

		//test lable
		//create the style

		Label testLabel = new Label("this is a test", labelStyle);


		//create the design for the text area
		TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
		textFieldStyle.fontColor = new Color(1,1,1,1);
		textFieldStyle.font = consolasFont;
		//textFieldStyle.background = new SpriteDrawable(new Sprite(new Texture("scrollBarBackground.png")));;
		textFieldStyle.cursor = new SpriteDrawable(new Sprite(new Texture("cursor.png")));

		//create the text area
		textArea = new TextArea("\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na", textFieldStyle) {
			@Override
			public float getPrefHeight() {
				return getLines() * getStyle().font.getLineHeight();
			}
		};
		//table.add(textArea).grow();

		/*
		//try a container
		Container<TextArea> container = new Container<TextArea>();
		container.pad(100f);
		container.setBackground(ninePatch, true);
		container.setActor(textArea);
		//container.fill();
		container.setDebug(true);


		//create a ScrollPaneStyle
		ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
		scrollPaneStyle.vScrollKnob = new SpriteDrawable(new Sprite(new Texture("cursor.png")));
		scrollPaneStyle.vScroll = new SpriteDrawable(new Sprite(new Texture("scrollBarBackground.png")));

		Label muchTextLabel = new Label("\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na", labelStyle);

		//create the ScrollPane
		scrollPane = new ScrollPane(textArea, scrollPaneStyle);
		table.add(scrollPane).grow();
		table.add(testLabel);
		*/

		Table table = new Table();
		//table.setBackground(new SpriteDrawable(new Sprite(new Texture("badlogic.jpg"))));
		table.setFillParent(true);
		stage.addActor(table);


		//create the design for the text area
		TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
		textFieldStyle.fontColor = new Color(1,1,1,1);
		textFieldStyle.font = consolasFont;
		textFieldStyle.selection = new SpriteDrawable(new Sprite(new Texture("scrollBarBackground.png")));
		textFieldStyle.cursor = new SpriteDrawable(new Sprite(new Texture("cursor.png")));

		//create the text area
		textArea = new MultiColorTextArea("test 1\n\"check this", textFieldStyle);
		textArea.setColorParser(new ScriptColorParser());

		//create a ScrollPaneStyle
		ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
		scrollPaneStyle.background = ninePatch;
		scrollPaneStyle.vScrollKnob = new SpriteDrawable(new Sprite(new Texture("scrollBarThumb.png")));
		scrollPaneStyle.vScroll = new SpriteDrawable(new Sprite(new Texture("newScrollBarBackground.png")));
		scrollPaneStyle.hScrollKnob = new SpriteDrawable(new Sprite(new Texture("scrollBarThumb.png")));
		scrollPaneStyle.hScroll = new SpriteDrawable(new Sprite(new Texture("newScrollBarBackground.png")));

		//create the ScrollPane
		//scrollPane = new ScrollPane(textArea, scrollPaneStyle);
		//scrollPane.setFadeScrollBars(false);
		//if (Gdx.app.getType() != Application.ApplicationType.Android) scrollPane.setFlickScroll(false);

		//CodeEditor editor = new CodeEditor(textFieldStyle, scrollPaneStyle, new ScriptColorParser());

		//table.add(editor).grow();
		SimpleZoomableWidget simpleZoomableWidget = new SimpleZoomableWidget(new SpriteDrawable(new Sprite(new Texture("badlogic.jpg"))),
				1, 1, 2000f);

		ZoomScrollPane.ZoomScrollPaneStyle zspStyle = new ZoomScrollPane.ZoomScrollPaneStyle();
		zspStyle.vScrollKnob = new SpriteDrawable(new Sprite(new Texture("scrollBarThumb.png")));
		zspStyle.vScroll = new SpriteDrawable(new Sprite(new Texture("newScrollBarBackground.png")));
		zspStyle.hScrollKnob = new SpriteDrawable(new Sprite(new Texture("scrollBarThumb.png")));
		zspStyle.hScroll = new SpriteDrawable(new Sprite(new Texture("newScrollBarBackground.png")));

		ZoomScrollPane zsp = new ZoomScrollPane(simpleZoomableWidget, zspStyle);
		//table.add(zsp).grow();

		//add some useless Strings to the list
		listViewItems1 = new Array<>(true, 101);
		listViewItems1.add("this is another useless test");
		for (int x = 1; x < 100; x++) {
			listViewItems1.add(String.valueOf(x));
		}
		listViewItems1.add("this is a very very very very very very very very very very very very long String");

		listViewItems2 = new Array<>(true, 1);
		//listViewItems2.add("ajsflaksflöjadslöögjlkasghlöadskhgölaskjgjölasjglkwhgkölahklgjskölgjsdjlfjhlsdfjnhkälsdjkhladhölkjfdhkjljhakjsjdgköaöjkdhkdshfjlgkhaölkdfhgladhfgö0");

		List.ListStyle listStyle = new List.ListStyle(consolasFont, Color.BLACK, Color.WHITE, new SpriteDrawable(new Sprite(new Texture("cursor.png"))) );
		//listStyle.background = ninePatch;

		list1 = new List<>(listStyle);
		list1.setItems(listViewItems1);

		list2 = new List<>(listStyle);
		list2.setItems(listViewItems2);


		ScrollPane scrollPane2 = new ScrollPane(list1, scrollPaneStyle);
		scrollPane2.setFlickScroll(false);
		scrollPane2.setScrollingDisabled(true, false);

		table.add(scrollPane2).expand().fillY().left().width(400);

		ScrollPane scrollPane3 = new ScrollPane(list2, scrollPaneStyle);
		scrollPane3.setFlickScroll(false);
		scrollPane3.setScrollingDisabled(true, false);

		table.add(scrollPane3).expand().fill();

		//region DragAndDrop stuff

		DragAndDrop dragAndDrop = new DragAndDrop();
		dragAndDrop.addSource(new DragAndDrop.Source(list1) {
			@Override
			public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
				System.out.println("source.dragStart()");
				DragAndDrop.Payload payload = new DragAndDrop.Payload();
				payload.setObject(list1.getSelected());
				payload.setDragActor(new Label(list1.getSelected(), labelStyle));
				payload.setInvalidDragActor(new Label("invalid", labelStyle));
				payload.setValidDragActor(new Label("valid", labelStyle));
				return payload;
			}

			@Override
			public void drag(InputEvent event, float x, float y, int pointer) {
				//System.out.println("source.drag()");
				super.drag(event, x, y, pointer);
			}

			@Override
			public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
				System.out.println("source.dragStop()");
				super.dragStop(event, x, y, pointer, payload, target);
			}
		});

		dragAndDrop.addTarget(new DragAndDrop.Target(list2) {
			@Override
			public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				//System.out.println("target.drag()");
				return true;
			}

			@Override
			public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				System.out.println("target.drop()");
				listViewItems2.add((String)payload.getObject());
				list2.setItems(listViewItems2);
			}
		});

		//endregion

	}

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {
		//test to fix scroll issues

		//scrollPane.scrollTo(textArea.getCursorX(), textArea.getHeight() - textArea.getCursorY(), 0, textArea.getStyle().font.getLineHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		//super.resize(width, height);
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
	public void dispose () {
		stage.dispose();
	}
}
