package com.nkcoding.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.nkcoding.spacegame.Asset;
import com.nkcoding.spacegame.ExtAssetManager;

public class Styles {
    public final Drawable borderBackgroundDrawable;
    public final ScrollPane.ScrollPaneStyle scrollPaneStyle;
    public final ZoomScrollPane.ZoomScrollPaneStyle zoomScrollPaneStyle;
    public final Label.LabelStyle labelStyleBig;
    public final Label.LabelStyle labelStyleSmall;
    public final TextField.TextFieldStyle textFieldStyle;
    public final PropertyBox.PropertyBoxStyle propertyBoxStyle;
    public final CodeEditor.CodeEditorStyle codeEditorStyle;
    public final TextButton.TextButtonStyle textButtonStyle;
    public final BitmapFont bitmapFontBig;
    public final BitmapFont bitmapFontSmall;

    /**scale factor*/
    public final float scaleFactor;

    private static final float defaultAbs = 10f;
    public final float defaultScaledAbs;

    private static Styles singleton;

    public static Styles getDefaultStyles(ExtAssetManager assetManager) {
        if (singleton == null) {
            singleton = new Styles(assetManager);
        }
        return singleton;
    }

    private Styles(ExtAssetManager assetManager) {

        scaleFactor = Gdx.graphics.getDensity() / 0.75f;
        //scaleFactor = 2.5f;
        defaultScaledAbs = defaultAbs * scaleFactor;

        bitmapFontBig = generateFont((int)(32 * scaleFactor));
        bitmapFontSmall = generateFont((int)(18 * scaleFactor));

        borderBackgroundDrawable = new NinePatchDrawable(new NinePatch(assetManager.getTexture(Asset.SimpleBorder), 3, 3, 3, 3));
        borderBackgroundDrawable.setLeftWidth(defaultScaledAbs);
        borderBackgroundDrawable.setRightWidth(defaultScaledAbs);
        borderBackgroundDrawable.setTopHeight(defaultScaledAbs);
        borderBackgroundDrawable.setBottomHeight(defaultScaledAbs);

        //ScrollPane
        scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        Drawable scrollBarBackground = assetManager.getDrawable(Asset.ScrollBarBackground);
        Drawable scrollBarKnob = assetManager.getDrawable(Asset.ScrollBarKnob);
        float scrollBarSize = 12 * scaleFactor;
        scrollBarBackground.setMinWidth(scrollBarSize);
        scrollBarBackground.setMinHeight(scrollBarSize);
        scrollBarKnob.setMinHeight(scrollBarSize);
        scrollBarKnob.setMinWidth(scrollBarSize);
        scrollPaneStyle.vScrollKnob = scrollBarKnob;
        scrollPaneStyle.vScroll = scrollBarBackground;
        scrollPaneStyle.hScrollKnob = scrollBarKnob;
        scrollPaneStyle.hScroll = scrollBarBackground;

        //ZoomScrollPane
        zoomScrollPaneStyle = new ZoomScrollPane.ZoomScrollPaneStyle(scrollPaneStyle);

        //Label
        labelStyleSmall = new Label.LabelStyle(bitmapFontSmall, new Color(0xffffffff));
        labelStyleBig = new Label.LabelStyle(bitmapFontBig, new Color(0xffffffff));

        //TextField
        textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = bitmapFontSmall;
        textFieldStyle.fontColor = new Color(0xffffffff);
        textFieldStyle.cursor = assetManager.getDrawable(Asset.Cursor);
        textFieldStyle.selection = assetManager.getDrawable(Asset.Selection);
        Drawable textFieldBackground = assetManager.getDrawable(Asset.ScrollBarBackground);
        textFieldBackground.setLeftWidth(defaultScaledAbs / 2);
        textFieldBackground.setRightWidth(defaultScaledAbs / 2);
        textFieldBackground.setTopHeight(defaultScaledAbs / 2);
        textFieldBackground.setBottomHeight(defaultScaledAbs / 2);
        textFieldStyle.background = textFieldBackground;

        //PropertyBox
        propertyBoxStyle = new PropertyBox.PropertyBoxStyle();
        propertyBoxStyle.background = borderBackgroundDrawable;
        propertyBoxStyle.codeButtonDrawable = assetManager.getDrawable(Asset.CodeSymbol);
        propertyBoxStyle.getterButtonDrawable = assetManager.getDrawable(Asset.GetterSymbol);
        propertyBoxStyle.setterButtonDrawable = assetManager.getDrawable(Asset.SetterSymbol);
        propertyBoxStyle.textFieldStyle = textFieldStyle;
        propertyBoxStyle.illegalInputColor = new Color(0xff0000ff);
        propertyBoxStyle.legalInputColor = new Color(0xffffffff);
        propertyBoxStyle.labelStyle = labelStyleSmall;
        propertyBoxStyle.spacing = defaultScaledAbs;

        //CodeEditor
        codeEditorStyle = new CodeEditor.CodeEditorStyle(textFieldStyle,
                assetManager.getDrawable(Asset.LightBackground), scrollPaneStyle, new ScriptColorParser());

        //TextButton style
        textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = bitmapFontBig;
        textButtonStyle.fontColor = new Color(0xffffffff);
        textButtonStyle.down = borderBackgroundDrawable;
        textButtonStyle.up = borderBackgroundDrawable;
    }

    private BitmapFont generateFont(int fontSize) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/SourceCodePro-Medium.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = fontSize;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }
}
