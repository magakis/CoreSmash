package com.breakthecore.managers;

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
import java.util.Comparator;
import java.util.Random;

/**
 * The TilemapManager is responsible for every interaction with the Tilemaps and provides the
 * only interface for them.
 */

public class TilemapManager extends Observable implements Observer {
    /**
     * Holds the different independent layers(tilemaps). 0 is the main layer.
     */
    private final Tilemap[] listTilemaps;
    private final Coords2D tilemapPosition = new Coords2D(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4);
    private final int maxTilemapCount = 2;
    private int tilemapCount;
    private boolean baseLayerLostTiles;

    private Pathfinder pathfinder = new Pathfinder(30);
    private TilemapGenerator tilemapGenerator = new TilemapGenerator(this);
    private Match3 match3 = new Match3();


    public TilemapManager() {
        listTilemaps = new Tilemap[maxTilemapCount];

        for (int i = 0; i < maxTilemapCount; ++i) {
            listTilemaps[i] = new Tilemap(i, tilemapPosition);
        }
    }

    public int getTilemapCount() {
        return tilemapCount;
    }

    public int getTileDistance(int x1, int y1, int x2, int y2) {
        return pathfinder.getTileDistance(x1, y1, x2, y2);
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

    public TilemapGenerator getTilemapGenerator() {
        return tilemapGenerator;
    }

    /**
     * Finds an empty side from the coordinates specified and attach the tile provided.
     *
     * @return Returns whether it placed the tile.
     */
    public boolean attachTile(int layer, Tile tile, TilemapTile tileHit, TileContainer.Side[] listSides) {
        for (int i = 0; i < 6; ++i) {
            if (attachTile(layer, tile, tileHit, listSides[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean attachTile(int layer, Tile tile, TilemapTile tileHit, TileContainer.Side side) {
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

        for (TilemapTile t : match) {
            tm.destroyRelativeTile(t.getRelativePosition().x, t.getRelativePosition().y);
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
        if (baseLayerLostTiles) {
            removeDisconnectedTiles();
            baseLayerLostTiles = false;
        }

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
        /* In order to remove this logic and have the disconnected tiles destroyed the same frame,
         * there has to be a notification that will signal the end of any operation that might destroy
         * blocks and after which we can safely call to remove the disconnected tiles.
         *
         * Until then we should not use the TILE_DESTROYED notification as a signal to search for disconnnected
         * tiles cause that <u>will</u> create bugs! */
        if (type == NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED) {
            if (((TilemapTile) ob).getTilemapId() == 0) {
                baseLayerLostTiles = true;
            }
        }
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

    /* NOTE: All the shapes should use a relative coordinate system so that in the future they
     * can be used in arbitrary coordinates */
    public class TilemapGenerator {
        private final int maxColorCount = 10;
        private TilemapManager tilemapManager;
        private Random rand;
        private int colorCount;
        private ColorGroupContainer[] colors;
        private Comparator<ColorGroupContainer> compSizes;

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
                    return o1.list.size() > o2.list.size() ? 1 : -1;
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
                    if (pathfinder.getTileDistance(x, y, 0, 0) <= radius) {
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
            fillColorGroupContainers(tm);
            int aver = tm.getTileCount() / colorCount;
            int minIndex = maxColorCount - colorCount;
            int maxIndex = maxColorCount - 1;

            Arrays.sort(colors, compSizes);

            while (colors[minIndex].list.size() < aver) {
                ColorGroupContainer cgcLeastFilled = colors[minIndex];
                ColorGroupContainer cgcMostFilled = colors[maxIndex];

                TilemapTile tmTile = cgcMostFilled.list.get(rand.nextInt(cgcMostFilled.list.size()));
                cgcMostFilled.list.remove(tmTile);

                tmTile.getTile().setColor(cgcLeastFilled.groupColor);
                cgcLeastFilled.list.add(tmTile);

                Arrays.sort(colors, compSizes);
            }
        }


        public void reduceCenterTileColorMatch(Tilemap tm, int max) {
            reduceColorMatches(tm,max,false);
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
            for (int i = 0; i < colorCount; ++i) {
                colors[i].reset(i);
            }
        }

        private TilemapTile createTilemapTile(Tile tile) {
            TilemapTile res = new TilemapTile(tile);
            res.addObserver(tilemapManager);
            return res;
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
