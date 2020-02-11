package com.nkcoding.spacegame.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nkcoding.spacegame.SpaceGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1920, 1080);
        //config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        //config.useVsync(false);
        new Lwjgl3Application(new SpaceGame(), config);
    }
}

