package com.breakthecore.levelbuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.breakthecore.Coords2D;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

final class LevelParser {
    private XmlPullParser parser = new KXmlParser();
    private Pool<ParsedTile> parsedTilePool = new Pool<ParsedTile>() {
        @Override
        protected ParsedTile newObject() {
            return new ParsedTile();
        }
    };
    private ParsedLevel parsedLevel = new ParsedLevel();

    LevelParser() {
    }

    public ParsedLevel parseFile(String filename) {
        FileHandle file = Gdx.files.external("/CoreSmash/levels/" + filename + ".xml");
        if (!file.exists()) return null;

        parsedLevel.reset();

        try (Reader reader = file.reader()) {
            parser.setInput(reader);
            int type = parser.getEventType();
            do {
                switch (type) {
                    case XmlPullParser.START_TAG:
                        switch (parser.getName()) {
                            case LevelFormatParser.TAG_LEVEL_SETTINGS:
                                parseLevelSettings();
                                break;
                            case LevelFormatParser.TAG_MAP_SETTINGS:
                                parseMapSettings();
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

    private void parseLevelSettings() throws IOException, XmlPullParserException {
        int type;
        String name;
        do {
            type = parser.next();
            name = parser.getName();

            if (type == XmlPullParser.START_TAG) {
                String text;

                switch (name) {
                    case LevelFormatParser.TAG_LIVES:
                        text = parser.nextText();
                        parsedLevel.levelSettings.lives = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case LevelFormatParser.TAG_MOVES:
                        text = parser.nextText();
                        parsedLevel.levelSettings.moves = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case LevelFormatParser.TAG_TIME:
                        text = parser.nextText();
                        parsedLevel.levelSettings.time = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case LevelFormatParser.TAG_LAUNCHERSIZE:
                        text = parser.nextText();
                        parsedLevel.levelSettings.launcherSize = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case LevelFormatParser.TAG_LAUNCHERCD:
                        text = parser.nextText();
                        parsedLevel.levelSettings.launcherCooldown = text.isEmpty() ? 0 : Float.parseFloat(text);
                        break;
                    case LevelFormatParser.TAG_BALLSPEED:
                        text = parser.nextText();
                        parsedLevel.levelSettings.ballSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                }
            }
        } while (!name.equals(LevelFormatParser.TAG_LEVEL_SETTINGS));
    }

    private void parseMapSettings() throws IOException, XmlPullParserException {
        int type;
        String name;

        int mapIndex = 0;
        do {
            type = parser.next();
            name = parser.getName();

            if (type == XmlPullParser.START_TAG) {
                if (name.equals(LevelFormatParser.TAG_MAP)) {
                    parseMap(mapIndex++);
                }
            }
        } while (!name.equals(LevelFormatParser.TAG_MAP_SETTINGS));
    }

    private void parseMap(int index) throws IOException, XmlPullParserException {
        if (index == parsedLevel.mapSettings.length) throw new IndexOutOfBoundsException("Index was: "+index);

        int type;
        String name;
        MapSettings map = parsedLevel.mapSettings[index];

        do {
            type = parser.next();
            name = parser.getName();

            if (type == XmlPullParser.START_TAG) {
                String text;
                switch (name) {
                    case LevelFormatParser.TAG_MINSPEED:
                        text = parser.nextText();
                        map.minSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case LevelFormatParser.TAG_MAXSPEED:
                        text = parser.nextText();
                        map.maxSpeed = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case LevelFormatParser.TAG_ROTATECCW:
                        text = parser.nextText();
                        map.rotateCCW = !text.isEmpty() && Boolean.parseBoolean(text);
                        break;
                    case LevelFormatParser.TAG_COLORCOUNT:
                        text = parser.nextText();
                        map.colorCount = text.isEmpty() ? 0 : Integer.parseInt(text);
                        break;
                    case LevelFormatParser.TAG_CONTENT:
                        List<ParsedTile> tiles = parsedLevel.mapTiles[index];
                        do {
                            type = parser.next();
                            name = parser.getName();

                            if (type == XmlPullParser.START_TAG) {
                                if (name.equals(LevelFormatParser.TAG_BALL)) {
                                    ParsedTile tile = parsedTilePool.obtain();

                                    tile.tilemapID = index;
                                    tile.tileID = Integer.parseInt(parser.getAttributeValue(0));
                                    tile.x = Integer.parseInt(parser.getAttributeValue(1));
                                    tile.y = Integer.parseInt(parser.getAttributeValue(2));

                                    tiles.add(tile);
                                }
                            }
                        } while (!name.equals(LevelFormatParser.TAG_CONTENT));
                        break;
                }
            }
        } while (!name.equals(LevelFormatParser.TAG_MAP));
    }

}
