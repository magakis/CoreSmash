package com.archapp.coresmash.levelbuilder;

import com.archapp.coresmash.tilemap.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Pool;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.xmlpull.v1.XmlPullParser.NO_NAMESPACE;

public final class LevelParser {
    static final String TAG_LEVEL = "num";
    static final String TAG_LEVEL_SETTINGS = "levelSettings";
    static final String TAG_MAP_SETTINGS = "mapSettings";
    static final String TAG_LIVES = "lives";
    static final String TAG_MOVES = "moves";
    static final String TAG_TARGETSCORE_ONE = "targetScoreOne";
    static final String TAG_TARGETSCORE_TWO = "targetScoreTwo";
    static final String TAG_TARGETSCORE_THREE = "targetScoreThree";
    static final String TAG_TIME = "time";
    static final String TAG_MAP = "map";
    static final String TAG_MAP_OFFSET = "offset";
    static final String TAG_MAP_ORIGIN = "origin";
    static final String TAG_MINSPEED = "minSpeed";
    static final String TAG_MAXSPEED = "maxSpeed";
    static final String TAG_ORIGIN_MINSPEED = "originMinSpeed";
    static final String TAG_ORIGIN_MAXSPEED = "originMaxSpeed";
    static final String TAG_ROTATECCW = "rotateCCW";
    static final String TAG_CHAINED = "chained";
    static final String TAG_COLORCOUNT = "colorCount";
    static final String TAG_BALLSPEED = "ballSpeed";
    static final String TAG_LAUNCHERSIZE = "launcherSize";
    static final String TAG_LAUNCHERCD = "launcherCD";
    static final String TAG_CONTENT = "content";
    static final String TAG_BALL = "ball";

    private static Pool<ParsedTile> parsedTilePool = new Pool<ParsedTile>() {
        @Override
        protected ParsedTile newObject() {
            return new ParsedTile();
        }
    };
    private static ParsedLevel parsedLevel = new ParsedLevel();

    public static boolean saveAs(String name, Map map, LevelSettings levelSettings, MapSettings[] mapSettings) {
        int maxTilemaps = map.layerCount();

        boolean isLevelValid = false;
        for (int i = 0; i < maxTilemaps; ++i) {
            if (map.getTileCountFrom(i) > 0) {
                isLevelValid = true;
                break;
            }
        }
        if (!isLevelValid) return false;

        FileHandle file = Gdx.files.external("/CoreSmash/levels/" + name + ".xml");
        XmlSerializer serializer = XmlManager.getSerializer();

        try (Writer writer = file.writer(false)) {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", false);

            serializer.startTag(NO_NAMESPACE, TAG_LEVEL);
            serializer.startTag(NO_NAMESPACE, TAG_LEVEL_SETTINGS);
            XmlManager.createElement(TAG_LIVES, levelSettings.livesLimit);
            XmlManager.createElement(TAG_MOVES, levelSettings.movesLimit);
            XmlManager.createElement(TAG_TIME, levelSettings.timeLimit);
            XmlManager.createElement(TAG_TARGETSCORE_ONE, levelSettings.targetScoreOne);
            XmlManager.createElement(TAG_TARGETSCORE_TWO, levelSettings.targetScoreTwo);
            XmlManager.createElement(TAG_TARGETSCORE_THREE, levelSettings.targetScoreThree);
            XmlManager.createElement(TAG_BALLSPEED, levelSettings.ballSpeed);
            XmlManager.createElement(TAG_LAUNCHERSIZE, levelSettings.launcherSize);
            XmlManager.createElement(TAG_LAUNCHERCD, levelSettings.launcherCooldown);
            serializer.endTag(NO_NAMESPACE, TAG_LEVEL_SETTINGS);

            serializer.startTag(NO_NAMESPACE, TAG_MAP_SETTINGS);
            int groupID = 0;
            for (int mapIndex = 0; mapIndex < maxTilemaps; ++mapIndex) {
                if (map.getTileCountFrom(mapIndex) == 0) continue;
                MapSettings setting = mapSettings[mapIndex];

                serializer.startTag(NO_NAMESPACE, TAG_MAP);
                serializer.attribute(NO_NAMESPACE, "id", String.valueOf(groupID++));
                serializer.startTag(NO_NAMESPACE, TAG_MAP_ORIGIN);
                serializer.attribute(NO_NAMESPACE, "x", String.valueOf(setting.origin.x));
                serializer.attribute(NO_NAMESPACE, "y", String.valueOf(setting.origin.y));
                serializer.endTag(NO_NAMESPACE, TAG_MAP_ORIGIN);
                serializer.startTag(NO_NAMESPACE, TAG_MAP_OFFSET);
                serializer.attribute(NO_NAMESPACE, "x", String.valueOf(setting.offset.x));
                serializer.attribute(NO_NAMESPACE, "y", String.valueOf(setting.offset.y));
                serializer.endTag(NO_NAMESPACE, TAG_MAP_OFFSET);
                XmlManager.createElement(TAG_ORIGIN_MINSPEED, setting.minMapSpeed);
                XmlManager.createElement(TAG_ORIGIN_MAXSPEED, setting.maxMapSpeed);
                XmlManager.createElement(TAG_MINSPEED, setting.minSpeed);
                XmlManager.createElement(TAG_MAXSPEED, setting.maxSpeed);
                XmlManager.createElement(TAG_CHAINED, setting.chained);
                XmlManager.createElement(TAG_ROTATECCW, setting.rotateCCW);
                XmlManager.createElement(TAG_COLORCOUNT, setting.colorCount);
                serializer.startTag(NO_NAMESPACE, TAG_CONTENT);
                map.serializeBalls(mapIndex, serializer, NO_NAMESPACE, TAG_BALL);
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

    public static ParsedLevel loadFrom(String filename, LevelListParser.Source source) {
        if (source == null) throw new RuntimeException("Source is required!");

        FileHandle file;
        if (source.equals(LevelListParser.Source.EXTERNAL)) {
            file = Gdx.files.external("/CoreSmash/levels/" + filename + ".xml");
        } else {
            file = Gdx.files.internal("levels/" + filename + ".xml");
        }
        if (!file.exists()) return null;

        parsedLevel.reset();
        XmlPullParser parser = XmlManager.getParser();
        Objects.requireNonNull(parser);

        try (Reader reader = file.reader()) {
            parser.setInput(reader);
            int type = parser.getEventType();
            do {
                String name = parser.getName();
                switch (type) {
                    case XmlPullParser.START_TAG:
                        switch (name) {
                            case TAG_LEVEL_SETTINGS:
                                parseLevelSettings(parser);
                                break;
                            case TAG_MAP_SETTINGS:
                                parseMapSettings(parser);
                                break;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                }
                type = parser.next();
            } while (type != XmlPullParser.END_DOCUMENT);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return parsedLevel;
    }

    private static void parseLevelSettings(XmlPullParser parser) throws IOException, XmlPullParserException {
        int type;
        String name;
        do {
            do {
                type = parser.next();
                name = parser.getName();
            } while (type == XmlPullParser.TEXT);

            if (type == XmlPullParser.START_TAG) {
                String text;

                switch (name) {
                    case TAG_LIVES:
                        text = parser.nextText();
                        parsedLevel.levelSettings.livesLimit = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_MOVES:
                        text = parser.nextText();
                        parsedLevel.levelSettings.movesLimit = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_TIME:
                        text = parser.nextText();
                        parsedLevel.levelSettings.timeLimit = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_LAUNCHERSIZE:
                        text = parser.nextText();
                        parsedLevel.levelSettings.launcherSize = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_LAUNCHERCD:
                        text = parser.nextText();
                        parsedLevel.levelSettings.launcherCooldown = text.isEmpty() ? 0 : Float.parseFloat(text);
                        break;
                    case TAG_TARGETSCORE_ONE:
                        text = parser.nextText();
                        parsedLevel.levelSettings.targetScoreOne = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_TARGETSCORE_TWO:
                        text = parser.nextText();
                        parsedLevel.levelSettings.targetScoreTwo = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_TARGETSCORE_THREE:
                        text = parser.nextText();
                        parsedLevel.levelSettings.targetScoreThree = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_BALLSPEED:
                        text = parser.nextText();
                        parsedLevel.levelSettings.ballSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                }
            }
        } while (!name.equals(TAG_LEVEL_SETTINGS));
    }

    private static void parseMapSettings(XmlPullParser parser) throws IOException, XmlPullParserException {
        int type;
        String name;

        int mapIndex = 0;
        do {
            do {
                type = parser.next();
                name = parser.getName();
            } while (type == XmlPullParser.TEXT);

            if (type == XmlPullParser.START_TAG) {
                if (name.equals(TAG_MAP)) {
                    if (parsedLevel.mapSettings.size() == mapIndex) {
                        parsedLevel.mapSettings.add(new MapSettings());
                        parsedLevel.mapTiles.add(new ArrayList<ParsedTile>());
                    }
                    parseMap(parser, mapIndex++);
                }
            }
        } while (!name.equals(TAG_MAP_SETTINGS));
    }

    private static void parseMap(XmlPullParser parser, int index) throws IOException, XmlPullParserException {
        if (index == parsedLevel.mapSettings.size())
            throw new IndexOutOfBoundsException("Index was: " + index);

        int type;
        String name;
        MapSettings map = parsedLevel.mapSettings.get(index);

        do {
            do {
                type = parser.next();
                name = parser.getName();
            } while (type == XmlPullParser.TEXT);

            if (type == XmlPullParser.START_TAG) {
                String text;
                switch (name) {
                    case TAG_MAP_ORIGIN:
                        text = parser.getAttributeValue(NO_NAMESPACE, "x");
                        map.origin.x = text == null || text.isEmpty() ? 0 : Float.parseFloat(text);
                        text = parser.getAttributeValue(NO_NAMESPACE, "y");
                        map.origin.y = text == null || text.isEmpty() ? 0 : Float.parseFloat(text);
                    case TAG_MAP_OFFSET:
                        text = parser.getAttributeValue(NO_NAMESPACE, "x");
                        map.offset.x = text == null || text.isEmpty() ? 0 : Float.parseFloat(text);
                        text = parser.getAttributeValue(NO_NAMESPACE, "y");
                        map.offset.y = text == null || text.isEmpty() ? 0 : Float.parseFloat(text);
                    case TAG_MINSPEED:
                        text = parser.nextText();
                        map.minSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_MAXSPEED:
                        text = parser.nextText();
                        map.maxSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_ORIGIN_MINSPEED:
                        text = parser.nextText();
                        map.minMapSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_ORIGIN_MAXSPEED:
                        text = parser.nextText();
                        map.maxMapSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_CHAINED:
                        text = parser.nextText();
                        map.chained = text.isEmpty() ? true : Boolean.valueOf(text);
                        break;
                    case TAG_ROTATECCW:
                        text = parser.nextText();
                        map.rotateCCW = !text.isEmpty() && Boolean.parseBoolean(text);
                        break;
                    case TAG_COLORCOUNT:
                        text = parser.nextText();
                        map.colorCount = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case TAG_CONTENT:
                        List<ParsedTile> tiles = parsedLevel.mapTiles.get(index);
                        do {
                            do {
                                type = parser.next();
                                name = parser.getName();
                            } while (type == XmlPullParser.TEXT);

                            if (type == XmlPullParser.START_TAG) {
                                if (name.equals(TAG_BALL)) {
                                    ParsedTile tile = parsedTilePool.obtain();

                                    tile.tilemapID = index;
                                    tile.tileID = Integer.parseInt(parser.getAttributeValue(0));
                                    tile.x = Integer.parseInt(parser.getAttributeValue(1));
                                    tile.y = Integer.parseInt(parser.getAttributeValue(2));

                                    tiles.add(tile);
                                }
                            }
                        } while (!name.equals(TAG_CONTENT));
                        break;
                }
            }
        } while (!name.equals(TAG_MAP));
    }

}
