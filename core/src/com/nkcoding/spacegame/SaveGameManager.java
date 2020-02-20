package com.nkcoding.spacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.nkcoding.spacegame.simulation.spaceship.ShipDef;

import java.io.IOException;
import java.io.Writer;

public class SaveGameManager {
    private static final String SAVE_FILE_PATH = "saveGame.json";
    private static final String EMERGENCY_SAVE_FILE_PATH = "emergencySave.json";
    private static SaveGame saveGame = null;

    /**
     * loads a SaveGame
     */
    public static SaveGame load() {
        if (saveGame == null) {
            FileHandle handle = Gdx.files.local(SAVE_FILE_PATH);
            if (handle.exists()) saveGame = new SaveGame(handle);
            else saveGame = new SaveGame();
        }
        return saveGame;
    }

    public static void save() {
        FileHandle handle = Gdx.files.local(SAVE_FILE_PATH);
        saveGame.save(handle);
    }

    public static void emergencySave() {
        FileHandle handle = Gdx.files.local(EMERGENCY_SAVE_FILE_PATH);
        saveGame.save(handle);
    }

    public static class SaveGame {
        public ShipDef shipDef;

        /**
         * constructor when file exists
         *
         * @param file the FileHandle which contains the file
         */
        private SaveGame(FileHandle file) {
            //load Json
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(file);

            //load shipDef
            shipDef = ShipDef.fromJson(root.get(ShipDef.class.getSimpleName()));
        }

        /**
         * constructor when no file exists
         */
        private SaveGame() {
            shipDef = new ShipDef();
        }

        private void save(FileHandle handle) {
            Json json = new Json(JsonWriter.OutputType.json);
            try (Writer writer = handle.writer(false)) {
                json.setWriter(writer);
                json.writeObjectStart();
                shipDef.toJson(json);
                json.writeObjectEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
