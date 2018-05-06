package com.breakthecore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tiles.RegularTile;
import com.breakthecore.tilemap.TilemapTile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;


/* Maybe this should not be a static class in the future for multithreading? */
public class LevelFormatParser {
    private static Pool<ParsedTile> parsedTilePool = new Pool<ParsedTile>() {
        @Override
        protected ParsedTile newObject() {
            return new ParsedTile();
        }
    };
    private static Array<ParsedTile> parsedTileArray = new Array<>();


    private LevelFormatParser() {
    }

    public static boolean fileExists(String name) {
        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");
        return file.exists();
    }


    public static Array<ParsedTile> load(String name) {
        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");
        if (!file.exists()) return null;

        clearParsedTileArray();

        BufferedReader reader = file.reader(1024);
        try {
            int tilemapID = 0;
            while (true) {
                String line = reader.readLine();
                if (line == null) break;

                int tileCount = Integer.parseInt(line);
                for (int i = 0; i < tileCount; ++i) {
                    line = reader.readLine();
                    String[] tokens = line.split(":");
                    ParsedTile tile = parsedTilePool.obtain();

                    tile.tilemapID = tilemapID;
                    tile.tileID = Integer.parseInt(tokens[0]);
                    tile.relativePosition.set(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                    parsedTileArray.add(tile);
                }
                ++tilemapID;
            }
        } catch (IOException ex) {

        }

        return parsedTileArray;
    }


    private static void clearParsedTileArray() {
        for (ParsedTile tile : parsedTileArray) {
            parsedTilePool.free(tile);
        }

        parsedTileArray.clear();
    }


//    public static boolean load(String name, TilemapManager tilemapManager) {
//        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");
//        if (!file.exists()) return false;
//
//        BufferedReader reader = file.reader(1024);
//        String line;
//        try {
//            while (true) {
//                line = reader.readLine();
//                if (line == null) break;
//                Tilemap tm = tilemapManager.newTilemap();
//
//                int tileCount = Integer.parseInt(line);
//                for (int i = 0; i < tileCount; ++i) {
//                    line = reader.readLine();
//                    String[] tokens = line.split(":");
//
//                    int id = Integer.parseInt(tokens[0]);
//                    int x = Integer.parseInt(tokens[1]);
//                    int y = Integer.parseInt(tokens[2]);
//
//                    tm.setRelativeTile(x, y, new RegularTile(id));
//                }
//            }
//
//            return true;
//        } catch (IOException ex) {
//
//        }
//        return false;
//    }

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

                        writer.println(String.format(Locale.ENGLISH, "%d:%d:%d", tile.getTileID(), x - centerTile, y - centerTile));
                    }
                }
            }
        }
    }

    public static class ParsedTile {
        private final Coords2D relativePosition = new Coords2D();
        private int tileID;
        private int tilemapID;

        private ParsedTile() {
        }


        public Coords2D getRelativePosition() {
            return relativePosition;
        }


        public int getTileID() {
            return tileID;
        }
    }
}
