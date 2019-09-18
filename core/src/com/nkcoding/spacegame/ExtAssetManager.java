package com.nkcoding.spacegame;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class ExtAssetManager extends AssetManager {


    public void loadAll() {
        for (Asset asset : Asset.values()) {
            this.load(asset.getValue(), getAssetClass(asset.getValue()));
        }
        this.update();
        this.finishLoading();
    }

    public Texture getTexture(Asset asset) {
        return get(asset.getValue(), Texture.class);
    }

    public Drawable getDrawable(Asset asset) {
        return new SpriteDrawable(new Sprite(getTexture(asset)));
    }

    public BitmapFont getBitmapFont(Asset asset) {
        return get(asset.getValue(), BitmapFont.class);
    }

    private Class getAssetClass(String asset) {
        if (asset.endsWith(".png") || asset.endsWith(".jpg")) return Texture.class;
        else if (asset.endsWith(".fnt")) return BitmapFont.class;
        else throw new IllegalArgumentException("no such ending found");
    }

}
