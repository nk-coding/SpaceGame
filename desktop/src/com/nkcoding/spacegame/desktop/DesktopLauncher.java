package com.nkcoding.spacegame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.nkcoding.spacegame.SpaceGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1280;
        config.height = 720;
//		config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
//		config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
//		config.fullscreen = true;
//		config.vSyncEnabled = false;
//		config.foregroundFPS = 0;
//		config.backgroundFPS = 0;
        new LwjglApplication(new SpaceGame(), config);
    }
}

