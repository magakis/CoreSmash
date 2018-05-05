package com.breakthecore.tilemap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.breakthecore.Coords2D;
import com.breakthecore.LevelFormatParser;
import com.breakthecore.tiles.RandomTile;
import com.breakthecore.tiles.RegularTile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class TilemapBuilder {
    private final int maxColorCount = 16;// XXX(4/5/2018): MAGIC VALUE 16
    private final int blueprintSize = 30;// XXX(4/5/2018): MAGIC VALUE 30
    private final int centerTile = blueprintSize / 2;

    private boolean debugEnabled;
    private boolean isBuilt;
    private Tilemap tilemap;
    private int colorCount;
    private int maxDistance;
    private int blueprintTileCount;

    private Random rand;
    private Pool<BlueprintTile> blueprintTilePool;
    private ColorGroupContainer[] colorGroupList;
    private Array<BlueprintTile> fixedTilesArrays;
    private Comparator<ColorGroupContainer> compSizes, compColors;
    private Comparator<BlueprintTile> compDistance;
    private BlueprintTile[][] blueprintMap;

    private int minRotSpeed;
    private int maxRotSpeed;
    private Boolean isRotating;

    public TilemapBuilder() {
        rand = new Random();
        colorGroupList = new ColorGroupContainer[maxColorCount];
        blueprintMap = new BlueprintTile[blueprintSize][blueprintSize];
        fixedTilesArrays = new Array<>();
        blueprintTilePool = new Pool<BlueprintTile>() {
            @Override
            protected BlueprintTile newObject() {
                return new BlueprintTile();
            }
        };

        for (int i = 0; i < maxColorCount; ++i) { // XXX(4/5/2018): Magic value,
            colorGroupList[i] = new ColorGroupContainer(i);
        }

        compColors = new Comparator<ColorGroupContainer>() {
            @Override
            public int compare(ColorGroupContainer o1, ColorGroupContainer o2) {
                return Integer.compare(o1.groupColor, o2.groupColor);
            }
        };

        compSizes = new Comparator<ColorGroupContainer>() {
            @Override
            public int compare(ColorGroupContainer o1, ColorGroupContainer o2) {
                if (o1.groupColor < colorCount) {
                    if (o2.groupColor < colorCount) {
                        return Integer.compare(o1.list.size(), o2.list.size());
                    } else {
                        return -1;
                    }
                } else if (o2.groupColor < colorCount) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };

        compDistance = new Comparator<BlueprintTile>() {
            @Override
            public int compare(BlueprintTile o1, BlueprintTile o2) {
                int o1Dist = getTileDistance(o1.x, o1.y, 0, 0);
                int o2Dist = getTileDistance(o2.x, o2.y, 0, 0);
                return Integer.compare(o1Dist, o2Dist);
            }
        };
    }

    public TilemapBuilder startNewTilemap(Tilemap tm) {
        if (tm == null) throw new NullPointerException();
        reset();
        tm.reset();
        tilemap = tm;
        return this;
    }

    public TilemapBuilder setColorCount(int colorCount) {
        this.colorCount = colorCount;
        checkIfCanBuild();
        return this;
    }

    public TilemapBuilder generateRadius(int radius) {
        checkIfCanBuild();

        for (int y = -radius - 1; y < radius + 1; ++y) {
            for (int x = -radius - 1; x < radius + 1; ++x) {
                if (getTileDistance(x, y, 0, 0) <= radius) {
                    if (debugEnabled) {
                        setTile(x, y, 17); // XXX(5/5/2018): MAGIC VALUE 17
                    } else {
                        setTile(x, y, getRandomColorID());
                    }
                }
            }
        }
        return this;
    }

    public TilemapBuilder balanceColorAmounts() {
        checkIfCanBuild();

        fillColorGroupContainers();
        int aver = blueprintTileCount / colorCount;
        int maxIndex = colorCount - 1;

        Arrays.sort(colorGroupList, compSizes);

        while (colorGroupList[0].list.size() < aver) {
            ColorGroupContainer cgcLeastFilled = colorGroupList[0];
            ColorGroupContainer cgcMostFilled = colorGroupList[maxIndex];

            BlueprintTile tile = cgcMostFilled.list.get(rand.nextInt(cgcMostFilled.list.size()));
            cgcMostFilled.list.remove(tile);

            tile.ID = cgcLeastFilled.groupColor;
            cgcLeastFilled.list.add(tile);

            Arrays.sort(colorGroupList, compSizes);
        }

        return this;
    }

    public TilemapBuilder forceEachColorOnEveryRadius() {
        checkIfCanBuild();

        fillColorGroupContainers();
        for (ColorGroupContainer cgc : colorGroupList) {
            Collections.sort(cgc.list, compDistance);
        }

        int distanceToSearchFor;

        startLoop:
        for (ColorGroupContainer cgc : colorGroupList) {
            distanceToSearchFor = 1;
            while (distanceToSearchFor <= maxDistance) {
                if (cgc.list.size() == 0) break;
                for (int listIndex = 0; listIndex < cgc.list.size(); ++listIndex) {
                    BlueprintTile tile = cgc.list.get(listIndex);
                    int distance = tile.distance;

                    if (distance > distanceToSearchFor || listIndex == cgc.list.size() - 1 && distance < distanceToSearchFor) {
                        BlueprintTile tileToSwapA = null;
                        int prevDistance = -1;

                        for (int i = 0; i < cgc.list.size(); ++i) {
                            int curDistance = cgc.list.get(i).distance;
                            if (prevDistance == curDistance) {
                                tileToSwapA = cgc.list.get(i);
                                break;
                            } else {
                                prevDistance = curDistance;
                            }
                        }

                        if (tileToSwapA == null) break startLoop;

                        ColorGroupContainer innerCgc = null;
                        BlueprintTile tileToSwapB = null;

                        loopB:
                        for (ColorGroupContainer color : colorGroupList) {
                            innerCgc = color;
                            if (cgc == innerCgc) continue;

                            prevDistance = -1;
                            for (int i = 0; i < innerCgc.list.size(); ++i) {
                                int curDistance = innerCgc.list.get(i).distance;
                                if (prevDistance == curDistance && curDistance == distanceToSearchFor) {
                                    tileToSwapB = innerCgc.list.get(i);
                                    break loopB;
                                } else {
                                    prevDistance = curDistance;
                                }
                            }
                        }

                        if (tileToSwapB == null) break startLoop;

                        BlueprintTile tmpTile = tileToSwapB;
                        tileToSwapB = tileToSwapA;
                        tileToSwapA = tmpTile;

                        cgc.list.remove(tileToSwapA);
                        cgc.list.add(tileToSwapB);

                        innerCgc.list.remove(tileToSwapB);
                        innerCgc.list.add(tileToSwapA);

                        Collections.sort(cgc.list, compDistance);
                        Collections.sort(innerCgc.list, compDistance);

                        ++distanceToSearchFor;
                    } else if (distance == distanceToSearchFor) {
                        ++distanceToSearchFor;
                        break;
                    }
                }
            }
        }
        return this;
    }

    public TilemapBuilder setMinMaxRotationSpeed(int min, int max) {
        maxRotSpeed = max;
        minRotSpeed = min;
        isRotating = true;
        return this;
    }

    public TilemapBuilder debug() {
        debugEnabled = true;
        return this;
    }

    /**
     * TODO: Currently it looks on the external drive while it should look in the game files
     */
    public TilemapBuilder loadMapFromFile(String name) {
        Array<LevelFormatParser.ParsedTile> parsedTiles = LevelFormatParser.load(name);

        if (parsedTiles == null) throw new RuntimeException("Map '" + name + "' doesn't exist!");

        for (LevelFormatParser.ParsedTile parsedTile : parsedTiles) {
            Coords2D pos = parsedTile.getRelativePosition();
            if (parsedTile.getTileID() == 17) {

                if (debugEnabled) {
                    BlueprintTile tile = getTile(pos.x, pos.y);
                    if (tile == null) {
                        tile = blueprintTilePool.obtain();
                    }
                    tile.set(pos.x, pos.y);
                    tile.ID = 17;
                    uncheckedPutTile(pos.x, pos.y, tile);
                } else {
                    checkIfCanBuild();
                    BlueprintTile tile = getTile(pos.x, pos.y);
                    if (tile == null) {
                        tile = blueprintTilePool.obtain();
                        tile.set(pos.x, pos.y);
                        tile.ID = getRandomColorID();
                        uncheckedPutTile(pos.x, pos.y, tile);
                    }
                }
            } else {
                BlueprintTile tile = blueprintTilePool.obtain();
                tile.set(pos.x, pos.y);
                tile.ID = parsedTile.getTileID();
                fixedTilesArrays.add(tile);
            }
        }
        return this;
    }

    public void build() {
        putFixedTilesInBlueprint();
        // TODO(4/5/2018): this function needs to know what each id represents...

        for (BlueprintTile[] arr : blueprintMap) {
            for (BlueprintTile tile : arr) {
                if (tile == null) continue;
                if (debugEnabled) {
                    if (tile.ID == 17)
                        tilemap.setRelativeTile(tile.x, tile.y, new RandomTile());
                    else
                        tilemap.setRelativeTile(tile.x, tile.y, new RegularTile(tile.ID));
                } else {
                    tilemap.setRelativeTile(tile.x, tile.y, new RegularTile(tile.ID));
                }
            }
        }

        tilemap.setMinMaxSpeed(minRotSpeed, maxRotSpeed);
        tilemap.setAutoRotation(isRotating);

        tilemap.initialized();
        isBuilt = true;
    }

    private BlueprintTile getTile(int x, int y) {
        return blueprintMap[y + centerTile][x + centerTile];
    }

    private void setTile(int x, int y, int ID) {
        BlueprintTile tile = getTile(x, y);

        if (tile == null) {
            tile = blueprintTilePool.obtain();
            tile.distance = getTileDistance(x, y, 0, 0);

            if (tile.distance > maxDistance) {
                maxDistance = tile.distance;
            }

            ++blueprintTileCount;
            uncheckedPutTile(x, y, tile);
        }

        tile.ID = ID;
        tile.x = x;
        tile.y = y;
    }

    private void putFixedTilesInBlueprint() {
        for (BlueprintTile tile : fixedTilesArrays) {
            BlueprintTile currentTile = getTile(tile.x, tile.y);
            if (currentTile != null) {
                blueprintTilePool.free(currentTile);
            }

            uncheckedPutTile(tile.x, tile.y, tile);
        }
        fixedTilesArrays.clear();
    }

    private int getRandomColorID() {
        return rand.nextInt(colorCount);
    }

    private int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
    }

    private void fillColorGroupContainers() {
        resetColorGroupContainers();

        for (int y = 0; y < blueprintSize; ++y) {
            for (int x = 0; x < blueprintSize; ++x) {
                BlueprintTile tile = blueprintMap[y][x];
                if (tile == null) continue;

                colorGroupList[tile.ID].list.add(tile);
            }
        }
    }

    private void resetColorGroupContainers() {
        for (ColorGroupContainer cgc : colorGroupList) {
            cgc.reset();
        }
        Arrays.sort(colorGroupList, compColors);
    }

    private void uncheckedPutTile(int x, int y, BlueprintTile tile) {
        blueprintMap[y + centerTile][x + centerTile] = tile;
    }

    private void checkIfCanBuild() {
        if (isBuilt || tilemap == null) throw new RuntimeException("Can't build");
        if (colorCount == 0) throw new RuntimeException("ColorCount must be set first");
    }

//    public void generateSquare(Tilemap tm, int size) {
//        int signFix;
//        for (int y = -size; y <= size; ++y) {
//            signFix = y > 0 ? 1 : 0;
//            for (int x = -size; x <= size; ++x) {
//                tm.setRelativeTile(x - (y + signFix) / 2, y, new RegularTile(rand.nextInt(colorCount)));
//            }
//        }
//    }

//    public void generateDiamond(Tilemap tm, int size) {
//        int fixSign;
//        int absY;
//        int sizeY = size * 2;
//        for (int y = -sizeY; y <= sizeY; ++y) {
//            absY = Math.abs(y);
//            fixSign = y < 0 ? absY : 0;
//            for (int x = -size; x <= size - absY; ++x) {
//                tm.setRelativeTile(x + fixSign, y, new RegularTile(rand.nextInt(colorCount)));
//            }
//        }
//    }

//    public void generateSquareSkewed(Tilemap tm, int size, boolean flipX) {
//        if (flipX) {
//            for (int y = -size; y <= size; ++y) {
//                for (int x = -size; x <= size; ++x) {
//                    tm.setRelativeTile(x - y, y, new RegularTile(rand.nextInt(colorCount)));
//                }
//            }
//        } else {
//            for (int y = -size; y <= size; ++y) {
//                for (int x = -size; x <= size; ++x) {
//                    tm.setRelativeTile(x, y, new RegularTile(rand.nextInt(colorCount)));
//                }
//            }
//        }
//    }

//    public void generateTriangle(Tilemap tm, int size, boolean flipY) {
//        int sizeY = size * 2;
//        if (flipY) {
//            for (int y = -sizeY; y <= size; ++y) {
//                for (int x = -size - y; x <= size; ++x) {
//                    tm.setRelativeTile(x, y, new RegularTile(rand.nextInt(colorCount)));
//                }
//            }
//        } else {
//            for (int y = -size; y <= sizeY; ++y) {
//                for (int x = -size; x <= size - y; ++x) {
//                    tm.setRelativeTile(x, y, new RegularTile(rand.nextInt(colorCount)));
//                }
//            }
//        }
//    }

//    public void generateStar(Tilemap tm, int size) {
//        generateTriangle(tm, size, false);
//        generateTriangle(tm, size, true);
//    }

//    public void reduceColorMatches(Tilemap tm, int max, boolean strict) {
//        int tilemapSize = tm.getTilemapSize();
//        BlueprintTile tmTile;
//        ArrayList<BlueprintTile> matches;
//        for (int y = 0; y < tilemapSize; ++y) {
//            for (int x = 0; x < tilemapSize; ++x) {
//                tmTile = tm.getAbsoluteTile(x, y);
//                if (tmTile == null) continue;
//
//                matches = match3.getColorMatchesFromTile(tmTile, tm);
//                if (strict) {
//                    while (matches.size() > max) {
//                        tmTile.setTile(new RegularTile(rand.nextInt(colorCount)));
//                        matches = match3.getColorMatchesFromTile(tmTile, tm);
//                    }
//                } else {
//                    if (matches.size() > max) {
//                        int color = tmTile.getTileID();
//                        int newColor = rand.nextInt(colorCount);
//                        while (newColor == color && colorCount != 1) {
//                            newColor = rand.nextInt(colorCount);
//                        }
//                        tmTile.setTile(new RegularTile(rand.nextInt(colorCount)));
//                    }
//                }
//            }
//        }
//    }

//    public void reduceColorMatches(Tilemap tm, int max, int numOfPasses) {
//        int tilemapSize = tm.getTilemapSize();
//        BlueprintTile tmTile;
//        ArrayList<BlueprintTile> matches;
//        int passesLeft;
//
//        for (int y = 0; y < tilemapSize; ++y) {
//            for (int x = 0; x < tilemapSize; ++x) {
//                tmTile = tm.getAbsoluteTile(x, y);
//                if (tmTile == null) continue;
//
//                passesLeft = numOfPasses;
//                matches = match3.getColorMatchesFromTile(tmTile, tm);
//                while (matches.size() > max && passesLeft > 0) {
//                    tmTile.setTile(new RegularTile(rand.nextInt(colorCount)));
//                    matches = match3.getColorMatchesFromTile(tmTile, tm);
//                    --passesLeft;
//                }
//            }
//        }
//    }

//    public void reduceCenterTileColorMatch(Tilemap tm, int max) {
//        reduceColorMatches(tm, max, false);
//    }

//    public void reduceCenterTileColorMatch(Tilemap tm, int max, boolean strict) {
//        BlueprintTile centerTile = tm.getRelativeTile(0, 0);
//        if (centerTile == null) return;
//
//        ArrayList<BlueprintTile> matches = match3.getColorMatchesFromTile(centerTile, tm);
//        int tries = 0;
//        while (matches.size() > max) {
//            if (tries == colorCount) {
//                if (strict) {
//                    BlueprintTile rngTile = matches.get(rand.nextInt(matches.size()));
//                    rngTile.setTile(new RegularTile(getNextColor(rngTile.getTileID())));
//                    tries = 0;
//                } else {
//                    return;
//                }
//            }
//            centerTile.setTile(new RegularTile(getNextColor(centerTile.getTileID())));
//            matches = match3.getColorMatchesFromTile(centerTile, tm);
//            ++tries;
//        }
//    }

//    private int getNextColor(int currentColor) {
//        return currentColor + 1 == colorCount ? 0 : currentColor + 1;
//    }

    private void reset() {
        for (int y = 0; y < blueprintSize; ++y) {
            for (int x = 0; x < blueprintSize; ++x) {
                BlueprintTile tile = blueprintMap[y][x];
                if (tile == null) continue;

                blueprintTilePool.free(tile);
                blueprintMap[y][x] = null;
            }
        }

        fixedTilesArrays.clear();
        tilemap = null;
        isBuilt = false;
        colorCount = 0;
        maxDistance = 0;

        debugEnabled = false;
        minRotSpeed = 0;
        maxRotSpeed = 0;
        isRotating = false;
    }

    private class ColorGroupContainer {
        private int groupColor;
        private ArrayList<BlueprintTile> list = new ArrayList<BlueprintTile>();

        public ColorGroupContainer(int colorId) {
            groupColor = colorId;
        }

        public void reset() {
            list.clear();
        }
    }

    private class BlueprintTile implements Pool.Poolable {
        int ID;
        int x;
        int y;
        int distance;

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
            distance = getTileDistance(x, y, 0, 0);
        }

        @Override
        public void reset() {
            ID = 0;
            x = 0;
            y = 0;
        }
    }
}
