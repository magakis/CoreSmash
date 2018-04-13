package com.breakthecore.managers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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

public class TilemapManager extends Observable implements Observer {
    private Tilemap tm;

    private ArrayList<TilemapTile> match = new ArrayList<TilemapTile>();
    private ArrayList<TilemapTile> exclude = new ArrayList<TilemapTile>();
    private boolean isRotationEnabled = true;
    private boolean isTilemapValid = true;
    private float rotationDegrees = 0;
    private Pathfinder m_pathfinder;
    private float initTileCount;
    private float minRotSpeed = 0;
    private float maxRotSpeed = 0;

    private float maxRotAddedSpeed;
    private float rotationSpeed;

    public TilemapManager(Tilemap map) {
        tm = map;
        m_pathfinder = new Pathfinder(tm.getSize());
        maxRotAddedSpeed = maxRotSpeed - minRotSpeed;
    }

    public void update(float delta) {
        if (isRotationEnabled) {
            rotationSpeed = MathUtils.clamp(maxRotSpeed - maxRotAddedSpeed * (tm.getTileCount() / initTileCount), minRotSpeed, maxRotSpeed);
            rotationDegrees += rotationSpeed * delta;
            tm.setRotation(rotationDegrees);
        }
    }

    public void setMinMaxRotationSpeed(float min, float max) {
        minRotSpeed = min;
        maxRotSpeed = max;
        maxRotAddedSpeed = maxRotSpeed - minRotSpeed;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public Tilemap getTileMap() {
        return tm;
    }

    public void setAutoRotation(boolean autoRotation) {
        isRotationEnabled = autoRotation;
    }

    public boolean addTile(Tile tile, TilemapTile solidTile, TileContainer.Side[] sides) {
        for (int i = 0; i < 6; ++i) {
            if (addTile(tile, solidTile, sides[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean addTile(Tile tile, TilemapTile solidTile, TileContainer.Side side) {
        TilemapTile newTile = null;
        boolean placedNewTile = false;
        Coords2D tilePos = solidTile.getRelativePositionInTilemap();
        switch (side) {
            case TOP_RIGHT:
                if (tm.getRelativeTile(tilePos.x, tilePos.y + 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( tilePos.x,  tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case TOP_LEFT:
                if (tm.getRelativeTile( tilePos.x - 1,  tilePos.y + 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( tilePos.x - 1,  tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case RIGHT:
                if (tm.getRelativeTile( tilePos.x + 1,  tilePos.y) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( tilePos.x + 1,  tilePos.y, newTile);
                    placedNewTile = true;
                }
                break;
            case LEFT:
                if (tm.getRelativeTile( tilePos.x - 1,  tilePos.y) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( tilePos.x - 1,  tilePos.y, newTile);
                    placedNewTile = true;
                }
                break;
            case BOTTOM_LEFT:
                if (tm.getRelativeTile( tilePos.x,  tilePos.y - 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( tilePos.x,  tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
            case BOTTOM_RIGHT:
                if (tm.getRelativeTile( tilePos.x + 1,  tilePos.y - 1) == null) {
                    newTile = createTilemapTile(tile);
                    tm.setTilemapTile( tilePos.x + 1,  tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
        }
        if (placedNewTile) {
            scanForColorMatches(newTile);
            return true;
        }
        return false;
    }

    public void cleanTilemap() {
        if (!isTilemapValid) {
            removeDisconnectedTiles();
            isTilemapValid = true;
        }
    }

    public TilemapTile createTilemapTile(Tile tile) {
        TilemapTile res = new TilemapTile(tile);
        res.addObserver(this);
        notifyObservers(NotificationType.NOTIFICATION_TYPE_NEW_TILE_CREATED, null);
        return res;
    }

    public void scanForColorMatches(TilemapTile tile) {
        match.clear();
        exclude.clear();

        addSurroundingColorMatches(tile, match, exclude);

        if (match.size() < 3) {
            if (match.size() == 1) {
                notifyObservers(NotificationType.NO_COLOR_MATCH, null);
            }
            return;
        }

        for (TilemapTile t : match) {
            tm.destroyRelativeTile( t.getRelativePositionInTilemap().x,  t.getRelativePositionInTilemap().y);
        }
        notifyObservers(NotificationType.SAME_COLOR_MATCH, match.size());
    }

//    private void getColorMatches(TilemapTile tile) {
//        match.clear();
//        exclude.clear();
//
//        addSurroundingColorMatches(tile, match, exclude);
//    }

    public void addSurroundingColorMatches(TilemapTile tile, List<TilemapTile> match, List<TilemapTile> exclude) {
        Coords2D tpos = tile.getRelativePositionInTilemap();
        int tx =  tpos.x;
        int ty =  tpos.y;

        TilemapTile tt;

        match.add(tile);
        exclude.add(tile);

        //top_left
        tt = tm.getRelativeTile(tx - 1, ty + 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }

        //top_right
        tt = tm.getRelativeTile(tx, ty + 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }

        //right
        tt = tm.getRelativeTile(tx + 1, ty);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }

        //bottom_right
        tt = tm.getRelativeTile(tx + 1, ty - 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }

        //bottom_left
        tt = tm.getRelativeTile(tx, ty - 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }

        //left
        tt = tm.getRelativeTile(tx - 1, ty);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }
    }

    public void initTilemapCircle(Tilemap tm, int radius) {
        tm.clear();
        TilemapTile dummy;
        Tile tile;

        ColorGroupContainer[] colors = new ColorGroupContainer[7];
        for (int i = 0; i < 7; ++i) {
            colors[i] = new ColorGroupContainer(i);
        }

        for (int y = -radius - 1; y < radius + 1; ++y) {
            for (int x = -radius - 1; x < radius + 1; ++x) {
                if (getTileDistance(x, y, 0, 0) <= radius) {
                    tile = new RegularTile();
                    colors[tile.getColor()].list.add(tile);
                    dummy = new TilemapTile(tile);
                    dummy.addObserver(this);
                    tm.setTilemapTile(x, y, dummy);
                }
            }
        }

        initTileCount = tm.getTileCount();
        balanceTilemap(colors);
    }

    public int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
    }

    private void balanceTilemap(ColorGroupContainer[] colors) {
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

        TilemapTile[][] tiles = tm.getTilemapTiles();
        for (TilemapTile[] arr : tiles) {
            for (TilemapTile t : arr) {
                if (t == null) continue;
                match.clear();
                exclude.clear();
                addSurroundingColorMatches(t, match, exclude);
                if (match.size() > 2) {
                    // check its type first through getType()?
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

    public void fillEntireTilemap(Tilemap tm) {
        TileContainer[][] tiles = tm.getTilemapTiles();
        int center_tile = tm.getSize() / 2;
//        int oddOrEvenFix = tm.getSize() % 2 == 1 ? 1 : 0;
        int tmp = (tm.getSize()) / 2;
        for (int y = -tmp; y < tmp; ++y) {
            for (int x = -tm.getSize() / 2; x < tm.getSize() / 2; ++x) {
                tm.setTilemapTile(x, y, new TilemapTile(new RegularTile()));
            }
        }
    }

    public void removeDisconnectedTiles() {
        TilemapTile tile;
        for (int y = 0; y < tm.getSize(); ++y) {
            for (int x = 0; x < tm.getSize(); ++x) {
                tile = tm.getAbsoluteTile(x,y);
                if (tile != null) {
                    if (m_pathfinder.getPathToCenter(tile, tm) == null) {
                        // TODO(13/4/2018): I should put TilemapTiles in an Object pool
                        tm.destroyAbsoluteTile(x, y);
                    }
                }
            }
        }
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        if (type == NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED) {
            isTilemapValid = false;
        }
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
