package com.breakthecore.managers;

import com.badlogic.gdx.utils.Array;
import com.breakthecore.Coords2D;
import com.breakthecore.Match3;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.Observer;
import com.breakthecore.Pathfinder;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.RegularTile;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileContainer;
import com.breakthecore.tiles.TilemapTile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * The TilemapManager is responsible for every interaction with the Tilemaps and provides the
 * only interface for them.
 * XXX: BAD IMPLEMENTATION!
 */

public class TilemapManager extends Observable implements Observer {
    /**
     * Holds the different independent layers(tilemaps). 0 is the main layer.
     */
    private final Tilemap[] listTilemaps;
    private final Coords2D tilemapPosition = new Coords2D(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4);
    private final int maxTilemapCount = 2;
    private int tilemapCount;

    private Pathfinder pathfinder = new Pathfinder(30);
    private TilemapGenerator tilemapGenerator = new TilemapGenerator(this);
    private Match3 match3 = new Match3();
    private int[] colorsAvailable = new int[10]; // XXX(22/4/2018): MagicValue 10

    public TilemapManager() {
        listTilemaps = new Tilemap[maxTilemapCount];

        for (int i = 0; i < maxTilemapCount; ++i) {
            listTilemaps[i] = new Tilemap(i, tilemapPosition);
        }
    }

    public int getTilemapCount() {
        return tilemapCount;
    }

    public int getTotalAmountOfTiles() {
        int res = 0;
        for (int i = 0; i < tilemapCount; ++i) {
            res += listTilemaps[i].getTileCount();
        }
        return res;
    }

    public Coords2D getTilemapPosition() {
        return tilemapPosition;
    }

    public Tilemap getTilemap(int layer) {
        if (layer >= tilemapCount && layer < 0) {
            throw new IndexOutOfBoundsException("Wrong layer index!");
        }
        return listTilemaps[layer];
    }

    public int[] getColorAmountsAvailable() {
        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        for (int tmIndex = 0; tmIndex < tilemapCount; ++tmIndex) {
            int[] listOfColorAmounts = listTilemaps[tmIndex].getColorAmountsAvailable();
            for (int i = 0; i < listOfColorAmounts.length; ++i) {
                colorsAvailable[i] += listOfColorAmounts[i];
            }
        }

        return colorsAvailable;
    }

    public TilemapGenerator getTilemapGenerator() {
        return tilemapGenerator;
    }

    /**
     * Finds an empty side from the coordinates specified and attach the tile provided.
     *
     * @return Returns whether it placed the tile.
     */
    public void attachTile(int layer, Tile tile, TilemapTile tileHit, TileContainer.Side[] listSides) {
        for (int i = 0; i < 6; ++i) {
            if (attachTile(layer, tile, tileHit, listSides[i])) {
                return;
            }
        }
    }

    private boolean attachTile(int layer, Tile tile, TilemapTile tileHit, TileContainer.Side side) {
        TilemapTile newTile = null;
        boolean placedNewTile = false;
        Tilemap tm = listTilemaps[layer];
        Coords2D tileHitPos = tileHit.getRelativePosition();

        switch (side) {
            case TOP_RIGHT:
                if (tm.getRelativeTile(tileHitPos.x, tileHitPos.y + 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setRelativeTile(tileHitPos.x, tileHitPos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case TOP_LEFT:
                if (tm.getRelativeTile(tileHitPos.x - 1, tileHitPos.y + 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setRelativeTile(tileHitPos.x - 1, tileHitPos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case RIGHT:
                if (tm.getRelativeTile(tileHitPos.x + 1, tileHitPos.y) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setRelativeTile(tileHitPos.x + 1, tileHitPos.y, newTile);
                    placedNewTile = true;
                }
                break;
            case LEFT:
                if (tm.getRelativeTile(tileHitPos.x - 1, tileHitPos.y) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setRelativeTile(tileHitPos.x - 1, tileHitPos.y, newTile);
                    placedNewTile = true;
                }
                break;
            case BOTTOM_LEFT:
                if (tm.getRelativeTile(tileHitPos.x, tileHitPos.y - 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setRelativeTile(tileHitPos.x, tileHitPos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
            case BOTTOM_RIGHT:
                if (tm.getRelativeTile(tileHitPos.x + 1, tileHitPos.y - 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setRelativeTile(tileHitPos.x + 1, tileHitPos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
        }
        if (placedNewTile) {
            handleColorMatches(newTile, tm);
            return true;
        }
        return false;
    }

    private void handleColorMatches(TilemapTile newTile, Tilemap tm) {
        ArrayList<TilemapTile> match = match3.getColorMatchesFromTile(newTile, tm);

        if (match.size() < 3) {
            if (match.size() == 1) {
                notifyObservers(NotificationType.NO_COLOR_MATCH, null);
            }
            return;
        }

        boolean centerTileDestroyed = false;
        for (TilemapTile t : match) {
            Coords2D pos = t.getRelativePosition();
            if (pos.x == 0 && pos.y == 0) {
                t.notifyObservers(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
                centerTileDestroyed = true;
            }
            tm.destroyRelativeTile(pos.x, pos.y);
        }

        if (!centerTileDestroyed) {
            removeDisconnectedTiles();
        }

        notifyObservers(NotificationType.SAME_COLOR_MATCH, match.size());
    }

    /**
     * Sets the number of Tilemaps that will be used for this game and resets them.
     */
    public void init(int tilemapCount) {
        if (tilemapCount > maxTilemapCount && tilemapCount < 1)
            throw new RuntimeException("tilemapCount out of bounds: " + tilemapCount);
        this.tilemapCount = tilemapCount;

        for (int i = 0; i < maxTilemapCount; ++i) {
            listTilemaps[i].reset();
        }
    }

    public void update(float delta) {
        for (int i = 0; i < tilemapCount; ++i) {
            listTilemaps[i].update(delta);
        }
    }

    public void reset() {
        tilemapCount = 0;
        listTilemaps[0].reset();
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        notifyObservers(type, ob);
    }

    private TilemapTile createTilemapTile(Tile tile) {
        TilemapTile res = new TilemapTile(tile);
        res.addObserver(this);
//        notifyObservers(NotificationType.NOTIFICATION_TYPE_NEW_TILE_CREATED, null);
        return res;
    }

    /**
     * Destroys tiles that are not connected to the center tile.
     * This check is only done on the 0 layer cause that is the only one that requires tiles to be connected to the center tile.
     */
    private void removeDisconnectedTiles() {
        TilemapTile tile;
        Tilemap tm = listTilemaps[0];
        for (int y = 0; y < tm.getTilemapSize(); ++y) {
            for (int x = 0; x < tm.getTilemapSize(); ++x) {
                tile = tm.getAbsoluteTile(x, y);
                if (tile != null) {
                    if (pathfinder.getPathToCenter(tile, tm) == null) {
                        // TODO(13/4/2018): I should put TilemapTiles in an Object pool
                        tm.destroyAbsoluteTile(x, y);
                    }
                }
            }
        }
    }

    public class TilemapGenerator {
        /* NOTE: All the shapes should use a relative coordinate system so that in the future they
         * can be used in arbitrary coordinates */
        private final int maxColorCount = 10;
        private TilemapManager tilemapManager;
        private Random rand;
        private int colorCount;
        private ColorGroupContainer[] colors;
        private Comparator<ColorGroupContainer> compSizes;
        private Comparator<TilemapTile> compDistance;

        private TilemapGenerator(TilemapManager tilemapManager) {
            this.tilemapManager = tilemapManager;
            rand = new Random();
            colors = new ColorGroupContainer[maxColorCount];
            for (int i = 0; i < maxColorCount; ++i) {
                colors[i] = new ColorGroupContainer(i);
            }

            compSizes = new Comparator<ColorGroupContainer>() {
                @Override
                public int compare(ColorGroupContainer o1, ColorGroupContainer o2) {
                    if (o1.groupColor < colorCount) {
                        if (o2.groupColor < colorCount) {
                            return o1.list.size() < o2.list.size() ? -1 : 1;
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

            compDistance = new Comparator<TilemapTile>() {
                @Override
                public int compare(TilemapTile o1, TilemapTile o2) {
                    return o1.getDistanceFromCenter() > o2.getDistanceFromCenter() ? 1 : -1;
                }
            };
        }

        public void setColorCount(int colorCount) {
            this.colorCount = colorCount;
        }

        public void setRandomSeed(long seed) {
            rand.setSeed(seed);
        }

        public void generateRadius(Tilemap tm, int radius) {
            for (int y = -radius - 1; y < radius + 1; ++y) {
                for (int x = -radius - 1; x < radius + 1; ++x) {
                    if (tm.getTileDistance(x, y, 0, 0) <= radius) {
                        tm.setRelativeTile(x, y, createTilemapTile(new RegularTile(rand.nextInt(colorCount))));
                    }
                }
            }
        }

        public void generateSquare(Tilemap tm, int size) {
            int signFix;
            for (int y = -size; y <= size; ++y) {
                signFix = y > 0 ? 1 : 0;
                for (int x = -size; x <= size; ++x) {
                    tm.setRelativeTile(x - (y + signFix) / 2, y, createTilemapTile(new RegularTile(rand.nextInt(colorCount))));
                }
            }
        }

        public void generateDiamond(Tilemap tm, int size) {
            int fixSign;
            int absY;
            int sizeY = size * 2;
            for (int y = -sizeY; y <= sizeY; ++y) {
                absY = Math.abs(y);
                fixSign = y < 0 ? absY : 0;
                for (int x = -size; x <= size - absY; ++x) {
                    tm.setRelativeTile(x + fixSign, y, createTilemapTile(new RegularTile(rand.nextInt(colorCount))));
                }
            }
        }

        public void generateSquareSkewed(Tilemap tm, int size, boolean flipX) {
            if (flipX) {
                for (int y = -size; y <= size; ++y) {
                    for (int x = -size; x <= size; ++x) {
                        tm.setRelativeTile(x - y, y, createTilemapTile(new RegularTile(rand.nextInt(colorCount))));
                    }
                }
            } else {
                for (int y = -size; y <= size; ++y) {
                    for (int x = -size; x <= size; ++x) {
                        tm.setRelativeTile(x, y, createTilemapTile(new RegularTile(rand.nextInt(colorCount))));
                    }
                }
            }
        }

        public void generateTriangle(Tilemap tm, int size, boolean flipY) {
            int sizeY = size * 2;
            if (flipY) {
                for (int y = -sizeY; y <= size; ++y) {
                    for (int x = -size - y; x <= size; ++x) {
                        tm.setRelativeTile(x, y, createTilemapTile(new RegularTile(rand.nextInt(colorCount))));
                    }
                }
            } else {
                for (int y = -size; y <= sizeY; ++y) {
                    for (int x = -size; x <= size - y; ++x) {
                        tm.setRelativeTile(x, y, createTilemapTile(new RegularTile(rand.nextInt(colorCount))));
                    }
                }
            }
        }

        public void generateStar(Tilemap tm, int size) {
            tilemapGenerator.generateTriangle(tm, size, false);
            tilemapGenerator.generateTriangle(tm, size, true);
        }

        public void reduceColorMatches(Tilemap tm, int max, boolean strict) {
            int tilemapSize = tm.getTilemapSize();
            TilemapTile tmTile;
            ArrayList<TilemapTile> matches;
            for (int y = 0; y < tilemapSize; ++y) {
                for (int x = 0; x < tilemapSize; ++x) {
                    tmTile = tm.getAbsoluteTile(x, y);
                    if (tmTile == null) continue;

                    matches = match3.getColorMatchesFromTile(tmTile, tm);
                    if (strict) {
                        while (matches.size() > max) {
                            tmTile.getTile().setColor(rand.nextInt(colorCount));
                            matches = match3.getColorMatchesFromTile(tmTile, tm);
                        }
                    } else {
                        if (matches.size() > max) {
                            int color = tmTile.getColor();
                            int newColor = rand.nextInt(colorCount);
                            while (newColor == color && colorCount != 1) {
                                newColor = rand.nextInt(colorCount);
                            }
                            tmTile.getTile().setColor(rand.nextInt(colorCount));
                        }
                    }
                }
            }
        }

        public void reduceColorMatches(Tilemap tm, int max, int numOfPasses) {
            int tilemapSize = tm.getTilemapSize();
            TilemapTile tmTile;
            ArrayList<TilemapTile> matches;
            int passesLeft;

            for (int y = 0; y < tilemapSize; ++y) {
                for (int x = 0; x < tilemapSize; ++x) {
                    tmTile = tm.getAbsoluteTile(x, y);
                    if (tmTile == null) continue;

                    passesLeft = numOfPasses;
                    matches = match3.getColorMatchesFromTile(tmTile, tm);
                    while (matches.size() > max && passesLeft > 0) {
                        tmTile.getTile().setColor(rand.nextInt(colorCount));
                        matches = match3.getColorMatchesFromTile(tmTile, tm);
                        --passesLeft;
                    }
                }
            }
        }

        public void balanceColorAmounts(Tilemap tm) {
            resetColorGroupContainers();
            fillColorGroupContainers(tm);
            int aver = tm.getTileCount() / colorCount;
            int maxIndex = colorCount-1;

            Arrays.sort(colors, compSizes);

            while (colors[0].list.size() < aver) {
                ColorGroupContainer cgcLeastFilled = colors[0];
                ColorGroupContainer cgcMostFilled = colors[maxIndex];

                TilemapTile tmTile = cgcMostFilled.list.get(rand.nextInt(cgcMostFilled.list.size()));
                cgcMostFilled.list.remove(tmTile);

                tmTile.getTile().setColor(cgcLeastFilled.groupColor);
                cgcLeastFilled.list.add(tmTile);

                Arrays.sort(colors, compSizes);
            }
        }

        public void forceEachColorOnEveryRadius(Tilemap tm) {
            resetColorGroupContainers();
            fillColorGroupContainers(tm);

            for (ColorGroupContainer cgc : colors) {
                Collections.sort(cgc.list, compDistance);
            }

            int maxTileDistanceFromCenter = tm.getMaxTileDistanceFromCenter()+1;
            int distanceToSearchFor;

            startLoop:
            for (ColorGroupContainer cgc : colors) {
                distanceToSearchFor = 1;
                while (distanceToSearchFor < maxTileDistanceFromCenter) {
                    if (cgc.list.size() == 0) break;
                    for (int listIndex = 0; listIndex < cgc.list.size(); ++listIndex) {
                        TilemapTile tmTile = cgc.list.get(listIndex);
                        int distance = tmTile.getDistanceFromCenter();

                        if (distance > distanceToSearchFor || listIndex == cgc.list.size()-1 && distance < distanceToSearchFor) {
                            TilemapTile tileToSwapA = null;
                            int prevDistance = -1;

                            for (int i = 0; i < cgc.list.size(); ++i) {
                                int curDistance = cgc.list.get(i).getDistanceFromCenter();
                                if (prevDistance == curDistance) {
                                    tileToSwapA = cgc.list.get(i);
                                    break;
                                } else {
                                    prevDistance = curDistance;
                                }
                            }

                            if (tileToSwapA == null) break startLoop;

                            ColorGroupContainer innerCgc = null;
                            TilemapTile tileToSwapB = null;

                            loopB:
                            for (ColorGroupContainer color : colors) {
                                innerCgc = color;
                                if (cgc == innerCgc) continue;

                                prevDistance = -1;
                                for (int i = 0; i < innerCgc.list.size(); ++i) {
                                    int curDistance = innerCgc.list.get(i).getDistanceFromCenter();
                                    if (prevDistance == curDistance && curDistance == distanceToSearchFor) {
                                        tileToSwapB = innerCgc.list.get(i);
                                        break loopB;
                                    } else {
                                        prevDistance = curDistance;
                                    }
                                }
                            }

                            if (tileToSwapB == null) break startLoop;

                            int tmpColor = tileToSwapB.getColor();
                            tileToSwapB.getTile().setColor(tileToSwapA.getColor());
                            tileToSwapA.getTile().setColor(tmpColor);

                            cgc.list.remove(tileToSwapA);
                            cgc.list.add(tileToSwapB);

                            innerCgc.list.remove(tileToSwapB);
                            innerCgc.list.add(tileToSwapA);

                            Collections.sort(cgc.list, compDistance);
                            Collections.sort(innerCgc.list, compDistance);

                            ++distanceToSearchFor;
                        } else if (distance == distanceToSearchFor){
                            ++distanceToSearchFor;
                            break;
                        }
                    }
                }
            }
        }

        public void reduceCenterTileColorMatch(Tilemap tm, int max) {
            reduceColorMatches(tm, max, false);
        }

        public void reduceCenterTileColorMatch(Tilemap tm, int max, boolean strict) {
            TilemapTile centerTile = tm.getRelativeTile(0, 0);
            if (centerTile == null) return;

            ArrayList<TilemapTile> matches = match3.getColorMatchesFromTile(centerTile, tm);
            int tries = 0;
            while (matches.size() > max) {
                if (tries == colorCount) {
                    if (strict) {
                        TilemapTile rngTile = matches.get(rand.nextInt(matches.size()));
                        rngTile.getTile().setColor(getNextColor(rngTile.getColor()));
                        tries = 0;
                    } else {
                        return;
                    }
                }
                centerTile.getTile().setColor(getNextColor(centerTile.getColor()));
                matches = match3.getColorMatchesFromTile(centerTile, tm);
                ++tries;
            }
        }

        private int getNextColor(int currentColor) {
            return currentColor + 1 == colorCount ? 0 : currentColor + 1;
        }

        private void fillColorGroupContainers(Tilemap tm) {
            resetColorGroupContainers();
            TilemapTile tmTile;
            for (int y = 0; y < tm.getTilemapSize(); ++y) {
                for (int x = 0; x < tm.getTilemapSize(); ++x) {
                    tmTile = tm.getAbsoluteTile(x, y);
                    if (tmTile == null) continue;

                    colors[tmTile.getColor()].list.add(tmTile);
                }
            }
        }

        private void resetColorGroupContainers() {
            for (int i = 0; i < 10; ++i) {
                colors[i].reset(i);
            }
        }

        private class ColorGroupContainer {
            int groupColor;
            ArrayList<TilemapTile> list;

            public ColorGroupContainer(int colorId) {
                groupColor = colorId;
                list = new ArrayList<TilemapTile>();
            }

            public void reset(int color) {
                groupColor = color;
                list.clear();
            }
        }
    }
}
