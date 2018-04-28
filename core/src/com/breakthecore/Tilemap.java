package com.breakthecore;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.breakthecore.tiles.RegularTile;
import com.breakthecore.tiles.TilemapTile;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tilemap {
    private int ID;
    private int tilesPerSide;
    private int tileSize;
    private int maxTileDistanceFromCenter;
    private float tileSizeHalf;
    private int centerTile;
    private Coords2D screenPosition;
    private TilemapTile[][] listTilemapTiles;
    private int[] colorsAvailable;

    private boolean hadTilesDestroyed;
    private boolean isTilemapInitilized;

    private float rotation;
    private boolean autoRotationEnabled;
    private float minRotationSpeed;
    private float maxRotationSpeed;
    private float speedDiff;

    private float cos;
    private float sin;

    private int initTileCount;
    private int tileCount;

    private Tilemap(int id) {
        ID = id;
        tilesPerSide = 30; // NOTE: If this is changed, change it also in pathfinder!
        tileSize = WorldSettings.getTileSize();
        tileSizeHalf = tileSize / 2.f;
        centerTile = tilesPerSide / 2;
        listTilemapTiles = new TilemapTile[tilesPerSide][tilesPerSide];
        colorsAvailable = new int[10]; // XXX(22/4/2018): Magic Value 10!
        cos = 1;
        sin = 0;
    }

    public Tilemap(int id, int posX, int posY) {
        this(id);
        screenPosition = new Coords2D(posX, posY);
    }

    public Tilemap(int id, Coords2D screenPos) {
        this(id);
        screenPosition = screenPos;
    }

    public int[] getColorAmountsAvailable() {
        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        for (TilemapTile[] arr : listTilemapTiles) {
            for (TilemapTile t : arr) {
                if (t == null) continue;
                colorsAvailable[t.getColor()]++;
            }
        }

        return colorsAvailable;
    }

    public int getCenterTilePos() {
        return centerTile;
    }

    public int getId() {
        return ID;
    }

    public int getTilemapSize() {
        return tilesPerSide;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getTileCount() {
        return tileCount;
    }

    public float getRotation() {
        return rotation;
    }

    public float getCos() {
        return cos;
    }

    public float getSin() {
        return sin;
    }

    public Coords2D getPositionInWorld() {
        return new Coords2D(screenPosition);
    }

    public TilemapTile getAbsoluteTile(int x, int y) {
        return listTilemapTiles[y][x];
    }

    public TilemapTile getRelativeTile(int x, int y) {
        return listTilemapTiles[centerTile + y][centerTile + x];
    }

    public void setAutoRotation(boolean autoRotate) {
        autoRotationEnabled = autoRotate;
    }

    public void setMinMaxSpeed(float min, float max) {
        minRotationSpeed = min;
        maxRotationSpeed = max;
        speedDiff = max - min;
    }

    public void setAbsoluteTile(int x, int y, TilemapTile tile) {
        setRelativeTile(x - centerTile, y - centerTile, tile);
    }

    public int getMaxTileDistanceFromCenter() {
        return maxTileDistanceFromCenter;
    }

    public void setRelativeTile(int x, int y, TilemapTile tile) {
        if (tile == null) return;

        tile.setPositionInTilemap(x, y, centerTile);
        tile.setTilemapId(ID);
        tile.setDistanceFromCenter(this);
        updateTilemapTile(tile);

        if (getRelativeTile(x, y) == null) {
            if (!isTilemapInitilized) {
                ++initTileCount;
                if (tile.getDistanceFromCenter() > maxTileDistanceFromCenter) {
                    maxTileDistanceFromCenter = tile.getDistanceFromCenter();
                }
            }
            ++tileCount;
        }

        listTilemapTiles[centerTile + y][centerTile + x] = tile;
        ++colorsAvailable[tile.getColor()];
    }

    public int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
    }

    public void setRotation(float deg) {
        rotation = deg;
        float rotRad = (float) Math.toRadians(deg);
        cos = (float) Math.cos(rotRad);
        sin = (float) Math.sin(rotRad);

        for (TilemapTile[] arr : listTilemapTiles) {
            for (TilemapTile hex : arr) {
                if (hex != null) {
                    updateTilemapTile(hex);
                }
            }
        }
    }

    public void initialized() {
        isTilemapInitilized = true;
    }

    public void destroyAbsoluteTile(int absX, int absY) {
        destroyRelativeTile(absX - centerTile, absY - centerTile);
    }

    public void destroyRelativeTile(int tileX, int tileY) {
        TilemapTile t = getRelativeTile(tileX, tileY);
        if (t == null) return;
        t.notifyObservers(NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED, t);
        t.clear();
        emptyRelativeTile(tileX, tileY);
        --tileCount;
        hadTilesDestroyed = true;
    }

    public void rotate(float deg) {
        rotation += deg;
        setRotation(rotation);
    }

    public void reset() {
        for (int y = 0; y < tilesPerSide; ++y) {
            for (int x = 0; x < tilesPerSide; ++x) {
                TilemapTile t = getAbsoluteTile(x, y);
                if (t == null) continue;
                t.clear();
                emptyAbsoluteTile(x, y);
                --tileCount;
            }
        }

        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        minRotationSpeed = 0;
        maxRotationSpeed = 0;
        isTilemapInitilized = false;
        speedDiff = 0;
        initTileCount = 0;
        tileCount = 0;
        rotation = 0;
        cos = 1;
        sin = 0;
    }

    public void update(float delta) {
        hadTilesDestroyed = false;
        if (autoRotationEnabled) {
            rotate(MathUtils.clamp(maxRotationSpeed - speedDiff * ((float) tileCount / initTileCount), minRotationSpeed, maxRotationSpeed) * delta);
        }
    }

    public boolean hadTilesDestroyed() {
        return hadTilesDestroyed;
    }

    public Coords2D worldToTilemapCoords(Vector3 worldCoords) {
        Coords2D result = new Coords2D();
        result.x = (int) worldCoords.x;
        result.y = (int) worldCoords.y;

        result.x -= screenPosition.x;
        result.y -= screenPosition.y;

        float tileYDistance = tileSize * .8f;
        float tileXDistance = tileSize * .95f;
        float tileXDistanceHalf = tileXDistance / 2.f;

        float revX = result.x * cos + result.y * -sin;
        float revY = result.x * sin + result.y * cos;

        int y = roundClosestInt(revY / tileYDistance);
        int x = roundClosestInt((revX - y * tileXDistanceHalf) / tileXDistance);

        result.y = y;
        result.x = x;
        return result;
    }

    private int roundClosestInt(float n) {
        if (n > 0)
            return (int) Math.floor(n + .5f);
        return (int) Math.ceil(n - .5f);
    }

    private void updateTilemapTile(TilemapTile hex) {
        Coords2D tilePos = hex.getRelativePosition();
        float x = tilePos.x;
        float y = tilePos.y;

        float X_world, Y_world;
        float tileXDistance = tileSize * .95f;
        float tileXDistanceHalf = tileXDistance / 2;
        float tileYDistance = tileSize * .80f;

        X_world = screenPosition.x +
                (x * tileXDistance + y * tileXDistanceHalf) * cos +
                (y * tileYDistance) * sin;

        Y_world = screenPosition.y +
                (x * tileXDistance + y * tileXDistanceHalf) * -sin +
                (y * tileYDistance) * cos;

        hex.setPositionInWorld(X_world, Y_world);
    }

    private void emptyRelativeTile(int x, int y) {
        listTilemapTiles[y + centerTile][x + centerTile] = null;
    }

    private void emptyAbsoluteTile(int x, int y) {
        listTilemapTiles[y][x] = null;
    }

}