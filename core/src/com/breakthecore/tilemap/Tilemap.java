package com.breakthecore.tilemap;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.breakthecore.Coords2D;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileContainer.Side;
import com.breakthecore.tiles.TileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.breakthecore.tiles.TileContainer.getOppositeSide;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tilemap extends Observable {
    private int groupID;
    private int tileSize;
    private int maxDistanceFromCenter;
    private Coords2D screenPosition;
    private WorldToTilemap worldToTilemap;
    private List<TilemapTile> tilemapTiles;
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
    private TilemapTile dummyTile;

    private Tilemap(int id) {
        groupID = id;
        tileSize = WorldSettings.getTileSize();
        colorsAvailable = new int[10]; // XXX(22/4/2018): Magic Value 10!
        cos = 1;
        worldToTilemap = new WorldToTilemap();
        dummyTile = new TilemapTile(null);
        tilemapTiles = new ArrayList<>();
    }

    public Tilemap(int id, Coords2D screenPos) {
        this(id);
        screenPosition = screenPos;
    }

    public int getMinRotationSpeed() {
        return minRotationSpeed;
    }

    public int getMaxRotationSpeed() {
        return maxRotationSpeed;
    }

    public List<TilemapTile> getTileList() {
        return tilemapTiles;
    }

    public int[] getColorAmountsAvailable() {
        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        for (TilemapTile t : tilemapTiles) {
            if (t.getTile().getTileType() == TileType.REGULAR) {
                colorsAvailable[t.getTileID()]++;
            }
        }

        return colorsAvailable;
    }

    public boolean isCCWRotationEnabled() {
        return rotateCounterClockwise;
    }

    public int getId() {
        return groupID;
    }

    public int getTileCount() {
        return tilemapTiles.size();
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

    public TilemapTile getTilemapTile(int x, int y) {
        dummyTile.setCoordinates(x, y);
        Collections.sort(tilemapTiles);
        int index = Collections.binarySearch(tilemapTiles, dummyTile);
        return index >= 0 ? tilemapTiles.get(index) : null;
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

    public int getMaxDistanceFromCenter() {
        return maxDistanceFromCenter;
    }

    public void putTilemapTile(int x, int y, Tile tile) {
        TilemapTile slot = getTilemapTile(x, y);

        if (slot == null) {
            TilemapTile newTile = new TilemapTile(tile);
            newTile.setPositionInTilemap(groupID, x, y);
            attachNeighbours(newTile);

            if (!isTilemapInitilized) {
                ++initTileCount;
                if (newTile.getDistanceFromCenter() > maxDistanceFromCenter) {
                    maxDistanceFromCenter = newTile.getDistanceFromCenter();
                }
            }

            if (tile.getTileType() == TileType.REGULAR) {
                ++colorsAvailable[tile.getID()];
            }

            tilemapTiles.add(newTile);
        } else {
            throw new RuntimeException("I was too bored to implement but looks like I have to..");
        }
    }

    public void attachTile(TilemapTile tmTile, Tile tile, Side side) {
        Coords2D coords = tmTile.getCoords();
        if (tmTile.getNeighbour(side) == null) {
            switch (side) {
                case TOP_RIGHT:
                    putTilemapTile(coords.x, coords.y + 1, tile);
                    break;
                case TOP_LEFT:
                    putTilemapTile(coords.x - 1, coords.y + 1, tile);
                    break;
                case RIGHT:
                    putTilemapTile(coords.x + 1, coords.y, tile);
                    break;
                case LEFT:
                    putTilemapTile(coords.x - 1, coords.y, tile);
                    break;
                case BOTTOM_LEFT:
                    putTilemapTile(coords.x, coords.y - 1, tile);
                    break;
                case BOTTOM_RIGHT:
                    putTilemapTile(coords.x + 1, coords.y - 1, tile);
                    break;
            }
        } else {
            throw new RuntimeException("PFFF... JUST HOW?!");
        }
    }

    public static int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
    }

    private void updateRotation(float deg) {
        float rotRad = (float) Math.toRadians(deg);
        cos = (float) Math.cos(rotRad);
        sin = (float) Math.sin(rotRad);

        for (TilemapTile tmTile : tilemapTiles) {
            updateTilemapTile(tmTile);
        }
    }

    public void initialized() {
        isTilemapInitilized = true;
    }

    public void destroyTilemapTile(int x, int y) {
        TilemapTile tmTile = getTilemapTile(x, y);
        if (tmTile == null) return;

        notifyObservers(NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED, tmTile);
        tmTile.clear();
        tilemapTiles.remove(tmTile);
        hadTilesDestroyed = true;
    }

    public void rotate(float deg) {
        if (rotateCounterClockwise)
            rotation -= deg;
        else
            rotation += deg;
        updateRotation(rotation);
    }

    public void reset() {
        Iterator<TilemapTile> iter = tilemapTiles.iterator();
        while (iter.hasNext()) {
            TilemapTile t = iter.next();
            if (t == null) continue;
            t.clear();
            iter.remove();
        }

        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        minRotationSpeed = 0;
        maxRotationSpeed = 0;
        maxDistanceFromCenter = 0;
        isTilemapInitilized = false;
        speedDiff = 0;
        initTileCount = 0;
        rotation = 0;
        cos = 1;
        sin = 0;
    }

    public void update(float delta) {
        hadTilesDestroyed = false;
        if (autoRotationEnabled) {
            rotate(MathUtils.clamp(maxRotationSpeed - speedDiff * ((float) tilemapTiles.size() / initTileCount), minRotationSpeed, maxRotationSpeed) * delta);
        }
    }

    public boolean hadTilesDestroyed() {
        return hadTilesDestroyed;
    }

    private int roundClosestInt(float n) {
        if (n > 0)
            return (int) Math.floor(n + .5f);
        return (int) Math.ceil(n - .5f);
    }

    private void attachNeighbours(TilemapTile tmTile) {
        Coords2D coord = tmTile.getCoords();

        attachNeighbourFor(tmTile, Side.TOP_LEFT, coord.x - 1, coord.y + 1);
        attachNeighbourFor(tmTile, Side.TOP_RIGHT, coord.x, coord.y + 1);
        attachNeighbourFor(tmTile, Side.RIGHT, coord.x + 1, coord.y);
        attachNeighbourFor(tmTile, Side.BOTTOM_RIGHT, coord.x + 1, coord.y - 1);
        attachNeighbourFor(tmTile, Side.BOTTOM_LEFT, coord.x, coord.y - 1);
        attachNeighbourFor(tmTile, Side.LEFT, coord.x - 1, coord.y);
    }

    private void attachNeighbourFor(TilemapTile tmTile, Side side, int x, int y) {
        TilemapTile neighbour = getTilemapTile(x, y);
        if (neighbour != null) {
            neighbour.setNeighbour(getOppositeSide(side), tmTile);
        }
        tmTile.setNeighbour(side, neighbour);
    }

    private void updateTilemapTile(TilemapTile tmTile) {
        Coords2D tilePos = tmTile.getCoords();
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

        tmTile.setPositionInWorld(X_world, Y_world);
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