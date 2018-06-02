package com.breakthecore.tilemap;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.breakthecore.Coords2D;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileType;

import java.security.InvalidParameterException;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tilemap extends Observable {
    private int ID;
    private int tilesPerSide;
    private int tileSize;
    private int maxTileDistanceFromCenter;
    private int centerTile;
    private Coords2D screenPosition;
    private WorldToTilemap worldToTilemap;
    private TilemapTile[][] tilemapTileList;
    private int[] colorsAvailable;

    private boolean hadTilesDestroyed;
    private boolean isTilemapInitilized;

    private boolean rotateCounterClockwise;
    private boolean autoRotationEnabled;
    private float rotation;
    private int minRotationSpeed;
    private int maxRotationSpeed;
    private float speedDiff;

    private float cos;
    private float sin;

    private int initTileCount;
    private int tileCount;

    private Tilemap(int id) {
        ID = id;
        tilesPerSide = 30; // NOTE: If this is changed, change it also in pathfinder!
        tileSize = WorldSettings.getTileSize();
        centerTile = tilesPerSide / 2;
        tilemapTileList = new TilemapTile[tilesPerSide][tilesPerSide];
        colorsAvailable = new int[10]; // XXX(22/4/2018): Magic Value 10!
        cos = 1;
        sin = 0;
        worldToTilemap = new WorldToTilemap();
    }

    public int getMinRotationSpeed() {
        return minRotationSpeed;
    }

    public int getMaxRotationSpeed() {
        return maxRotationSpeed;
    }

    public Tilemap(int id, int posX, int posY) {
        this(id);
        screenPosition = new Coords2D(posX, posY);
    }

    public Tilemap(int id, Coords2D screenPos) {
        this(id);
        screenPosition = screenPos;
    }

    public TilemapTile[] getTileList() {
        TilemapTile[] list = new TilemapTile[tileCount];
        int listIndex = 0;

        for (int y = 0; y < tilesPerSide; ++y) {
            for (int x = 0; x < tilesPerSide; ++x) {
                if (tilemapTileList[y][x] != null) {
                    list[listIndex++] = tilemapTileList[y][x];
                }
            }
        }
        return list;
    }

    public int[] getColorAmountsAvailable() {
        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        for (TilemapTile[] arr : tilemapTileList) {
            for (TilemapTile t : arr) {
                if (t == null) continue;
                if (t.getTile().getTileType() == TileType.REGULAR) {
                    colorsAvailable[t.getTileID()]++;
                }
            }
        }

        return colorsAvailable;
    }

    public int getCenterTilePos() {
        return centerTile;
    }

    public boolean isCCWRotationEnabled() {
        return rotateCounterClockwise;
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

    public Coords2D getWorldToTilemapCoords(Vector3 worldCoords) {
        return worldToTilemap.convert(worldCoords);
    }

    public Coords2D getPositionInWorld() {
        return new Coords2D(screenPosition);
    }

    public TilemapTile getAbsoluteTile(int x, int y) {
        if (indexSafeGuard(x, y)) return null;
        return tilemapTileList[y][x];
    }

    public TilemapTile getRelativeTile(int x, int y) {
        if (indexSafeGuard(centerTile + x, centerTile + y)) return null;
        return tilemapTileList[centerTile + y][centerTile + x];
    }

    public void setCounterClockwiseRotation(boolean counterClockwise) {
        rotateCounterClockwise = counterClockwise;
    }

    public void setAutoRotation(boolean autoRotate) {
        autoRotationEnabled = autoRotate;
    }

    public void setMinMaxSpeed(float min, float max) {
        minRotationSpeed = (int) min;
        maxRotationSpeed = (int) max;
        speedDiff = max - min;
    }

    public int getMaxTileDistanceFromCenter() {
        return maxTileDistanceFromCenter;
    }

    public void setRelativeTile(int x, int y, Tile tile) {
        if (tile == null) throw new InvalidParameterException();
        if (indexSafeGuard(centerTile + x, centerTile + y)) return;

        TilemapTile tilemapTile = new TilemapTile(tile);
        tilemapTile.setPositionInTilemap(ID, x, y, centerTile);
        tilemapTile.setDistanceFromCenter(this);
        updateTilemapTile(tilemapTile);


        if (getRelativeTile(x, y) == null) {
            if (!isTilemapInitilized) {
                ++initTileCount;
                if (tilemapTile.getDistanceFromCenter() > maxTileDistanceFromCenter) {
                    maxTileDistanceFromCenter = tilemapTile.getDistanceFromCenter();
                }
            }
            ++tileCount;
        }

        tilemapTileList[centerTile + y][centerTile + x] = tilemapTile;

        if (tile.getID() < 8) {
            ++colorsAvailable[tile.getID()];
        }
    }

    public static int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
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

        for (TilemapTile[] arr : tilemapTileList) {
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

    public void destroyAbsoluteTile(int x, int y) {
        if (indexSafeGuard(x, y)) return;

        destroyRelativeTile(x - centerTile, y - centerTile);
    }

    public void destroyRelativeTile(int x, int y) {
        if (indexSafeGuard(centerTile + x, centerTile + y)) return;

        TilemapTile t = getRelativeTile(x, y);
        if (t == null) return;
        notifyObservers(NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED, t);
        t.clear();
        emptyRelativeTile(x, y);
        --tileCount;
        hadTilesDestroyed = true;
    }

    public void rotate(float deg) {
        if (rotateCounterClockwise)
            rotation -= deg;
        else
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
        maxTileDistanceFromCenter = 0;
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

    private boolean indexSafeGuard(int x, int y) {
        return x < 0 || x >= tilesPerSide ||
                y < 0 || y >= tilesPerSide;
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
        tilemapTileList[y + centerTile][x + centerTile] = null;
    }

    private void emptyAbsoluteTile(int x, int y) {
        tilemapTileList[y][x] = null;
    }

    private class WorldToTilemap {
        private Coords2D result = new Coords2D();

        public Coords2D convert(Vector3 world) {
            result.x = (int) world.x;
            result.y = (int) world.y;

            result.x -= screenPosition.x;
            result.y -= screenPosition.y;

            // XXX(11/5/2018): MAGIC VALUES .8f , .95f
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
    }
}