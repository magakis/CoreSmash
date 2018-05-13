package com.breakthecore.levelbuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapTile;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import static org.xmlpull.v1.XmlPullParser.NO_NAMESPACE;


/* Maybe this should not be a static class in the future for multithreading? */
public final class LevelFormatParser {
    static final String TAG_LEVEL = "level";
    static final String TAG_LEVEL_SETTINGS = "levelSettings";
    static final String TAG_MAP_SETTINGS = "mapSettings";
    static final String TAG_LIVES = "lives";
    static final String TAG_MOVES = "moves";
    static final String TAG_TIME = "time";
    static final String TAG_MAP = "map";
    static final String TAG_MINSPEED = "minSpeed";
    static final String TAG_MAXSPEED = "maxSpeed";
    static final String TAG_ROTATECCW = "rotateCCW";
    static final String TAG_COLORCOUNT = "colorCount";
    static final String TAG_BALLSPEED = "ballSpeed";
    static final String TAG_LAUNCHERSIZE= "launcherSize";
    static final String TAG_LAUNCHERCD= "launcherCD";
    static final String TAG_CONTENT = "content";
    static final String TAG_BALL = "ball";

    private static XmlSerializer serializer = new KXmlSerializer();
    private static LevelParser levelParser = new LevelParser();

    private LevelFormatParser() {
    }

    public static boolean saveAs(String name, TilemapManager tilemapManager, LevelSettings levelSettings, MapSettings[] mapSettings) {
        FileHandle file = Gdx.files.external("/CoreSmash/levels/" + name + ".xml");
        int maxTilemaps = tilemapManager.getMaxTilemapCount();

        try (Writer writer = file.writer(false)){
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", false);

            serializer.startTag(NO_NAMESPACE, TAG_LEVEL);
            serializer.startTag(NO_NAMESPACE, TAG_LEVEL_SETTINGS);
            createElement(TAG_LIVES, levelSettings.lives);
            createElement(TAG_MOVES, levelSettings.moves);
            createElement(TAG_TIME, levelSettings.time);
            createElement(TAG_BALLSPEED, levelSettings.ballSpeed);
            createElement(TAG_LAUNCHERSIZE, levelSettings.launcherSize);
            createElement(TAG_LAUNCHERCD, levelSettings.launcherCooldown);
            serializer.endTag(NO_NAMESPACE, TAG_LEVEL_SETTINGS);

            serializer.startTag(NO_NAMESPACE, TAG_MAP_SETTINGS);
            for (int mapIndex = 0; mapIndex < maxTilemaps; ++mapIndex) {
                if (tilemapManager.getTileCountFrom(mapIndex) == 0) break;
                MapSettings map = mapSettings[mapIndex];

                serializer.startTag(NO_NAMESPACE, TAG_MAP);
                serializer.attribute(NO_NAMESPACE, "id", String.valueOf(mapIndex));
                createElement(TAG_MINSPEED, map.minSpeed);
                createElement(TAG_MAXSPEED, map.maxSpeed);
                createElement(TAG_ROTATECCW, map.rotateCCW);
                createElement(TAG_COLORCOUNT, map.colorCount);
                serializer.startTag(NO_NAMESPACE, TAG_CONTENT);
                for (TilemapTile tile : tilemapManager.getTileList(mapIndex)) {
                    serializer.startTag(NO_NAMESPACE, TAG_BALL);
                    serializer.attribute(NO_NAMESPACE, "id", String.valueOf(tile.getTile().getID()));
                    serializer.attribute(NO_NAMESPACE, "x", String.valueOf(tile.getRelativePosition().x));
                    serializer.attribute(NO_NAMESPACE, "y", String.valueOf(tile.getRelativePosition().y));
                    serializer.endTag(NO_NAMESPACE, TAG_BALL);
                }
                serializer.endTag(NO_NAMESPACE, TAG_CONTENT);
                serializer.endTag(NO_NAMESPACE, TAG_MAP);
            }
            serializer.endTag(NO_NAMESPACE, TAG_MAP_SETTINGS);
            serializer.endTag(NO_NAMESPACE, TAG_LEVEL);
            serializer.endDocument();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static ParsedLevel loadFrom(String fileName) {
        return levelParser.parseFile(fileName);
    }

    private static <P> void createElement(String name, P value) throws IOException {
        serializer.startTag(NO_NAMESPACE, name).text(String.valueOf(value)).endTag(NO_NAMESPACE, name);
    }

    private static void handleStartTag() {
    }

    ////////////| OLD |////////////
    public static boolean fileExists(String name) {
        FileHandle file = Gdx.files.external("/CoreSmash/levels/" + name + ".xml");
        return file.exists();
    }

//    public static Array<ParsedTile> load(String name) {
//        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");
//        if (!file.exists()) return null;
//
//        clearParsedTileArray();
//
//        BufferedReader reader = file.reader(1024);
//        try {
//            int tilemapID = 0;
//            while (true) {
//                String line = reader.readLine();
//                if (line == null) break;
//
//                int tileCount = Integer.parseInt(line);
//                for (int i = 0; i < tileCount; ++i) {
//                    line = reader.readLine();
//                    String[] tokens = line.split(":");
//                    ParsedTile tile = parsedTilePool.obtain();
//
//                    tile.tilemapID = tilemapID;
//                    tile.tileID = Integer.parseInt(tokens[0]);
//                    tile.relativePosition.set(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
//                    parsedTileArray.add(tile);
//                }
//                ++tilemapID;
//            }
//        } catch (IOException ex) {
//
//        }
//
//        return parsedTileArray;
//    }


    public static void saveTo(String name, TilemapManager tilemapManager) {
        FileHandle file = Gdx.files.external("/CoreSmash/maps/" + name + ".map");

        int tilemapCount = tilemapManager.getMaxTilemapCount();

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


}
