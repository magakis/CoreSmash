package com.breakthecore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.tiles.RegularTile;
import com.breakthecore.tiles.TilemapTile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.Buffer;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class LevelFormatParser {
    private LevelFormatParser() {
    }

    public static boolean fileExists(String name) {
        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");
        return file.exists();
    }

    public static boolean load(String name, TilemapManager tilemapManager) {
        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");
        if (!file.exists()) return false;

        BufferedReader reader = file.reader(1024);
        String line;
        try {
            while (true) {
                line = reader.readLine();
                if (line == null) break;
                Tilemap tm = tilemapManager.newTilemap();

                int tileCount = Integer.parseInt(line);
                for (int i = 0; i < tileCount; ++i) {
                    line = reader.readLine();
                    String[] tokens = line.split(":");
                    int colorId = Integer.parseInt(tokens[0]);
                    int x = Integer.parseInt(tokens[1]);
                    int y = Integer.parseInt(tokens[2]);

                    tm.setRelativeTile(x, y, new TilemapTile(new RegularTile(colorId)));
                }
            }

            return true;
        } catch (IOException ex) {

        }
        return false;
    }

    public static void saveTo(String name, TilemapManager tilemapManager) {
        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");

        int tilemapCount = tilemapManager.getTilemapCount();

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(file.writer(false)))) {
            for (int i = 0; i < tilemapCount; ++i) {
                Tilemap tm = tilemapManager.getTilemap(i);
                writer.println(tm.getTileCount());

                int tilemapSize = tm.getTilemapSize();
                int centerTile = tm.getCenterTilePos();
                for (int y = 0; y < tilemapSize; ++y) {
                    for (int x = 0; x < tilemapSize; ++x) {
                        TilemapTile tile = tm.getAbsoluteTile(x, y);
                        if (tile == null) continue;

                        writer.println(String.format(Locale.ENGLISH, "%d:%d:%d", tile.getColor(), x - centerTile, y - centerTile));
                    }
                }
            }
        }
    }

}
