package com.breakthecore;

import com.badlogic.gdx.math.MathUtils;
import com.breakthecore.tiles.TilemapTile;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tilemap {
    private int tilesPerSide;
    private int tileSize;
    private float tileSizeHalf;
    private int centerTile;
    private Coords2D screenPosition;
    private TilemapTile[][] listTilemapTiles;

    private boolean isInitilized;
    public boolean needsValidation;

    private float rotation;

    private boolean autoRotationEnabled;
    private float minRotationSpeed;
    private float maxRotationSpeed;
    private float speedDiff;

    private float cos;
    private float sin;

    private int initTileCount;
    private int tileCount;

    public Tilemap() {
        tilesPerSide = 30; // NOTE: If this is changed, change it also in pathfinder!
        screenPosition = new Coords2D(WorldSettings.getWorldWidth()/2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4);
        tileSize = WorldSettings.getTileSize();
        tileSizeHalf = tileSize / 2.f;
        centerTile = tilesPerSide / 2;
        listTilemapTiles = new TilemapTile[tilesPerSide][tilesPerSide];
        cos = 1;
        sin = 0;
    }

    public int getCenterTilePos() {
        return centerTile;
    }

    public int getTilesPerSide() {
        return tilesPerSide;
    }

    public int getSideLength() {
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
        speedDiff = max-min;
    }

    public void setTileLiteral(int x, int y, TilemapTile tile) {
        setTilemapTile(x - centerTile, y - centerTile, tile);
    }

    public void setTilemapTile(int x, int y, TilemapTile tile) {
        if (tile == null) return;

        if (getRelativeTile(x, y) == null) {
            if (!isInitilized){
                ++initTileCount;
            }
            ++tileCount;
        }

        tile.setPositionInTilemap(x, y, centerTile);

        updateTilemapTile(tile);

        listTilemapTiles[centerTile + y][centerTile + x] = tile;
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

    public void setInitialized(boolean state) {
        isInitilized = true;
    }

    public void destroyAbsoluteTile(int absX, int absY) {
        destroyRelativeTile(absX-centerTile, absY-centerTile);
    }

    public void destroyRelativeTile(int tileX, int tileY) {
        TilemapTile t = getRelativeTile(tileX, tileY);
        if (t == null) return;
        if (tileX == 0 && tileY == 0) {
            t.notifyObservers(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
        }
        t.notifyObservers(NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED, null);
        t.clear();
        emptyRelativeTile(tileX, tileY);
        --tileCount;
        needsValidation = true;
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
        minRotationSpeed = 0;
        maxRotationSpeed = 0;
        isInitilized = false;
        needsValidation = false;
        speedDiff = 0;
        initTileCount = 0;
        tileCount = 0;
        rotation = 0;
        cos = 1;
        sin = 0;
    }

    public void update(float delta) {
        if (autoRotationEnabled) {
            rotate(MathUtils.clamp(maxRotationSpeed - speedDiff * ((float)tileCount / initTileCount), minRotationSpeed, maxRotationSpeed) * delta);
        }
    }

    public void updateTilemapTile(TilemapTile hex) {
        Coords2D tilePos = hex.getRelativePosition();
        float x = tilePos.x;
        float y = tilePos.y;

        float X_world, Y_world;
        float tileXDistance = tileSize *.95f;
        float tileYDistance = tileSize *.85f;

        X_world = screenPosition.x +
                (x * tileXDistance + y * tileSizeHalf) * cos +
                (y * tileYDistance ) * sin;

        Y_world = screenPosition.y +
                (x * tileXDistance + y * tileSizeHalf) * -sin +
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