package com.breakthecore.managers;

import com.breakthecore.Coords2D;
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
import java.util.List;

/** The TilemapManager is responsible for every interaction with the Tilemaps and provides the
 *  only interface for them.
 */

public class TilemapManager extends Observable implements Observer {
    /** Holds the different independent layers(tilemaps). 0 is the main layer.*/
    private Tilemap[] listTilemaps;

    private int maxTilemapCount = 2;
    private int tilemapCount;

    private ArrayList<TilemapTile> match = new ArrayList<TilemapTile>();
    private ArrayList<TilemapTile> exclude = new ArrayList<TilemapTile>();
    private Pathfinder pathfinder = new Pathfinder( 30);

    public TilemapManager() {
        listTilemaps = new Tilemap[maxTilemapCount];

        for (int i = 0; i < maxTilemapCount; ++i) {
            listTilemaps[i] = new Tilemap();
        }
    }

    /** Sets the number of Tilemaps that will be used for this game and resets them*/
    public void init(int tilemapCount) {
        if (tilemapCount > maxTilemapCount && tilemapCount < 1) throw new RuntimeException("tilemapCount out of bounds: "+ tilemapCount);
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

    public Tilemap getTilemap(int layer) {
        if (layer >= tilemapCount && layer < 0) throw new IndexOutOfBoundsException("Wrong layer index!");
        return listTilemaps[layer];
    }

    /** Finds an empty side from the coordinates specified and attach the tile provided.
     * @return Returns whether it placed the tile.
     */
    public boolean attachTile(int layer, Tile tile, Coords2D coordsSolidTile, TileContainer.Side[] listSides) {
        for (int i = 0; i < 6; ++i) {
            if (attachTile(layer, tile, coordsSolidTile, listSides[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean attachTile(int layer, Tile tile, Coords2D relCoordsSolidTile, TileContainer.Side side) {
        TilemapTile newTile = null;
        boolean placedNewTile = false;
        Tilemap tm = listTilemaps[layer];

        switch (side) {
            case TOP_RIGHT:
                if (tm.getRelativeTile(relCoordsSolidTile.x, relCoordsSolidTile.y + 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( relCoordsSolidTile.x,  relCoordsSolidTile.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case TOP_LEFT:
                if (tm.getRelativeTile( relCoordsSolidTile.x - 1,  relCoordsSolidTile.y + 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( relCoordsSolidTile.x - 1,  relCoordsSolidTile.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case RIGHT:
                if (tm.getRelativeTile( relCoordsSolidTile.x + 1,  relCoordsSolidTile.y) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( relCoordsSolidTile.x + 1,  relCoordsSolidTile.y, newTile);
                    placedNewTile = true;
                }
                break;
            case LEFT:
                if (tm.getRelativeTile( relCoordsSolidTile.x - 1,  relCoordsSolidTile.y) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( relCoordsSolidTile.x - 1,  relCoordsSolidTile.y, newTile);
                    placedNewTile = true;
                }
                break;
            case BOTTOM_LEFT:
                if (tm.getRelativeTile( relCoordsSolidTile.x,  relCoordsSolidTile.y - 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( relCoordsSolidTile.x,  relCoordsSolidTile.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
            case BOTTOM_RIGHT:
                if (tm.getRelativeTile( relCoordsSolidTile.x + 1,  relCoordsSolidTile.y - 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( relCoordsSolidTile.x + 1,  relCoordsSolidTile.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
        }
        if (placedNewTile) {
            scanForColorMatches(tm, newTile);
            return true;
        }
        return false;
    }

    public void checkForDisconnectedTiles() {
        if (listTilemaps[0].needsValidation) {
            removeDisconnectedTiles();
        }
    }

    public TilemapTile createTilemapTile(Tile tile) {
        TilemapTile res = new TilemapTile(tile);
        res.addObserver(this);
        notifyObservers(NotificationType.NOTIFICATION_TYPE_NEW_TILE_CREATED, null);
        return res;
    }

    public void scanForColorMatches(Tilemap tm, TilemapTile tile) {
        match.clear();
        exclude.clear();

        addSurroundingColorMatches(tm, tile, match, exclude);

        if (match.size() < 3) {
            if (match.size() == 1) {
                notifyObservers(NotificationType.NO_COLOR_MATCH, null);
            }
            return;
        }

        for (TilemapTile t : match) {
            tm.destroyRelativeTile( t.getRelativePosition().x,  t.getRelativePosition().y);
        }
        notifyObservers(NotificationType.SAME_COLOR_MATCH, match.size());
    }

    public void addSurroundingColorMatches(Tilemap tm, TilemapTile tile, List<TilemapTile> match, List<TilemapTile> exclude) {
        Coords2D tpos = tile.getRelativePosition();
        int tx =  tpos.x;
        int ty =  tpos.y;

        TilemapTile tt;

        match.add(tile);
        exclude.add(tile);

        //top_left
        tt = tm.getRelativeTile(tx - 1, ty + 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //top_right
        tt = tm.getRelativeTile(tx, ty + 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //right
        tt = tm.getRelativeTile(tx + 1, ty);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //bottom_right
        tt = tm.getRelativeTile(tx + 1, ty - 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //bottom_left
        tt = tm.getRelativeTile(tx, ty - 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //left
        tt = tm.getRelativeTile(tx - 1, ty);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }
    }

    public void reset() {
        tilemapCount = 0;
        listTilemaps[0].reset();
    }

    public void initTilemapRadius(Tilemap tm, int radius) {
        tm.reset();
        TilemapTile dummy;
        Tile tile;

        // TODO(19/4/2018): Make color amount configurable
        ColorGroupContainer[] colors = new ColorGroupContainer[7];
        for (int i = 0; i < 7; ++i) {
            colors[i] = new ColorGroupContainer(i);
        }

        for (int y = -radius - 1; y < radius + 1; ++y) {
            for (int x = -radius - 1; x < radius + 1; ++x) {
                if (pathfinder.getTileDistance(x, y, 0, 0) <= radius) {
                    tile = new RegularTile();
                    colors[tile.getColor()].list.add(tile);
                    dummy = new TilemapTile(tile);
                    dummy.addObserver(this);
                    tm.setTilemapTile(x, y, dummy);
                }
            }
        }
        balanceTilemap(tm, colors);
    }

    public int getTilemapCount() {
        return tilemapCount;
    }

    private void balanceTilemap(Tilemap tm, ColorGroupContainer[] colors) {
        // XXX(4/3/2018): Rquires a better implementation!
        int totalTiles = tm.getTileCount();
        int med = totalTiles / 7;
        int donateMax, donorSize, size, need;
        int rand;
        RegularTile tile;

        Comparator<ColorGroupContainer> comp = new Comparator<ColorGroupContainer>() {
            @Override
            public int compare(ColorGroupContainer o1, ColorGroupContainer o2) {
                return o1.list.size() > o2.list.size() ? 1 : -1;
            }
        };

        int tilesPerSide = tm.getTilesPerSide();
        TilemapTile t;

        for (int y = 0; y < tilesPerSide; ++y) {
            for (int x = 0; x < tilesPerSide; ++x) {
                t = tm.getAbsoluteTile(x, y);
                if (t == null) continue;
                match.clear();
                exclude.clear();
                addSurroundingColorMatches(tm, t, match, exclude);
                if (match.size() > 2) {
                    // XXX(19/4/2018): check its type first through getType()?
                    tile = (RegularTile) match.get(match.size() / 2).getTile();
                    colors[tile.getColor()].list.remove(tile);
                    tile.setColor(WorldSettings.getRandomInt(7));
                    colors[tile.getColor()].list.add(tile);
                }
            }
        }

        Arrays.sort(colors, comp);
        size = colors[0].list.size();
        while (size < med) { //if it's -1 it crashes
            donorSize = colors[6].list.size();
            donateMax = donorSize - med;
            need = med - size;
            while (donateMax != 0 && need != 0) {
                rand = WorldSettings.getRandomInt(donorSize);
                tile = (RegularTile) colors[6].list.get(rand);
                tile.setColor(colors[0].groupColor);
                colors[0].list.add(tile);
                colors[6].list.remove(tile);
                --donateMax;
                --donorSize;
                --need;
            }

            Arrays.sort(colors, comp);
            size = colors[0].list.size();
        }
    }

    public int getTileDistance(int x1, int y1, int x2, int y2) {
        return pathfinder.getTileDistance(x1, y1, x2, y2);
    }

    /**Destroys tiles that are not connected to the center tile.
     * This check is only done on the 0 layer cause that is the only one that requires tiles to be connected to the center tile.
     */
    private void removeDisconnectedTiles() {
        TilemapTile tile;
        Tilemap tm = listTilemaps[0];
        for (int y = 0; y < tm.getTilesPerSide(); ++y) {
            for (int x = 0; x < tm.getTilesPerSide(); ++x) {
                tile = tm.getAbsoluteTile(x,y);
                if (tile != null) {
                    if (pathfinder.getPathToCenter(tile, tm) == null) {
                        // TODO(13/4/2018): I should put TilemapTiles in an Object pool
                        tm.destroyAbsoluteTile(x, y);
                    }
                }
            }
        }
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        notifyObservers(type, ob);
    }

    private class ColorGroupContainer {
        int groupColor;
        ArrayList<Tile> list;

        public ColorGroupContainer(int colorId) {
            groupColor = colorId;
            list = new ArrayList<Tile>();
        }
    }
}
