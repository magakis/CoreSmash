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

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.breakthecore.tiles.TileContainer.getOppositeSide;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tilemap extends Observable implements Comparable<Tilemap> {
    private int groupID;
    private int maxDistanceFromCenter;
    private List<TilemapTile> tilemapTiles;
    private Coords2D screenPosition;
    private Coords2D origin;
    private int[] colorsAvailable;

    private boolean isTilemapInitilized;
    private boolean isChained;
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
        colorsAvailable = new int[10]; // XXX(22/4/2018): Magic Value 10!
        cos = 1;
        dummyTile = new TilemapTile(null);
        tilemapTiles = new ArrayList<>();
        origin = new Coords2D();
    }

    public Tilemap(int id, Coords2D screenPos) {
        this(id);
        screenPosition = screenPos;
        origin.set(screenPos.x, screenPos.y);
    }

    public int getTileCount() {
        return tilemapTiles.size();
    }

    public float getRotation() {
        return rotation;
    }

    public boolean isChained() {
        return isChained;
    }

    public static int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
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

    public List<TilemapTile> getTileList() {
        return Collections.unmodifiableList(tilemapTiles);
    }

    public Vector3 getWorldToTilemapCoords(Vector3 world) {
        world.x -= screenPosition.x;
        world.y -= screenPosition.y;

        int tileSize = WorldSettings.getTileSize();
        // XXX(11/5/2018): MAGIC VALUES .8f , .95f
        float tileYDistance = tileSize * .8f;
        float tileXDistance = tileSize * .95f;
        float tileXDistanceHalf = tileXDistance / 2.f;

        float revX = world.x * cos + world.y * -sin;
        float revY = world.x * sin + world.y * cos;

        int y = roundClosestInt(revY / tileYDistance);
        int x = roundClosestInt((revX - y * tileXDistanceHalf) / tileXDistance);

        world.y = y;
        world.x = x;
        return world;
    }

    public TilemapTile getTilemapTile(int x, int y) {
        dummyTile.setCoordinates(x, y);
        Collections.sort(tilemapTiles);
        int index = Collections.binarySearch(tilemapTiles, dummyTile);
        return index >= 0 ? tilemapTiles.get(index) : null;
    }

    public void putTilemapTile(int x, int y, Tile tile) {
        TilemapTile slot = getTilemapTile(x, y);

        if (slot == null) {
            TilemapTile newTile = new TilemapTile(tile);
            newTile.setPositionInTilemap(groupID, x, y);
            updateTilemapTile(newTile); // update tile to set it's world position (Used in levelBuilder)
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
        int x = tmTile.getX();
        int y = tmTile.getY();
        if (tmTile.getNeighbour(side) == null) {
            switch (side) {
                case TOP_RIGHT:
                    putTilemapTile(x, y + 1, tile);
                    break;
                case TOP_LEFT:
                    putTilemapTile(x - 1, y + 1, tile);
                    break;
                case RIGHT:
                    putTilemapTile(x + 1, y, tile);
                    break;
                case LEFT:
                    putTilemapTile(x - 1, y, tile);
                    break;
                case BOTTOM_LEFT:
                    putTilemapTile(x, y - 1, tile);
                    break;
                case BOTTOM_RIGHT:
                    putTilemapTile(x + 1, y - 1, tile);
                    break;
            }
        } else {
            throw new RuntimeException("PFFF... JUST HOW?!");
        }
    }

    public void destroyTilemapTile(int x, int y) {
        TilemapTile tmTile = getTilemapTile(x, y);
        if (tmTile == null) return;

        notifyObservers(NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED, tmTile);
        tmTile.clear();
        tilemapTiles.remove(tmTile);
    }

    public void rotate(float deg) {
        if (rotateCounterClockwise)
            rotation -= Math.toRadians(deg);
        else
            rotation += Math.toRadians(deg);

        cos = (float) Math.cos(rotation);
        sin = (float) Math.sin(rotation);

        for (TilemapTile tmTile : tilemapTiles) {
            updateTilemapTile(tmTile);
        }
    }

    public void initialize(TilemapBuilder.TilemapBuilderInfo settings) {
        isChained = settings.isChained();
        rotateCounterClockwise = settings.isRotatingCCW();
        minRotationSpeed = settings.getMinRotSpeed();
        maxRotationSpeed = settings.getMaxRotSpeed();
        speedDiff = maxRotationSpeed - minRotationSpeed;
        autoRotationEnabled = settings.isRotating();
        isTilemapInitilized = true;

    }

    public void update(float delta) {
        if (autoRotationEnabled) {
            rotate(MathUtils.clamp(maxRotationSpeed - speedDiff * ((float) tilemapTiles.size() / initTileCount), minRotationSpeed, maxRotationSpeed) * delta);
        }
    }

    void reset() {
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
        isChained = false;
        isTilemapInitilized = false;
        speedDiff = 0;
        initTileCount = 0;
        rotation = 0;
        cos = 1;
        sin = 0;
    }

    void serializeBalls(XmlSerializer serializer, String namespace, String tagBall) throws IOException {
        for (TilemapTile tile : tilemapTiles) {
            serializer.startTag(namespace, tagBall);
            serializer.attribute(namespace, "id", String.valueOf(tile.getTile().getID()));
            serializer.attribute(namespace, "x", String.valueOf(tile.getX()));
            serializer.attribute(namespace, "y", String.valueOf(tile.getY()));
            serializer.endTag(namespace, tagBall);

        }
    }

    private int roundClosestInt(float n) {
        if (n > 0)
            return (int) Math.floor(n + .5f);
        return (int) Math.ceil(n - .5f);
    }

    private void attachNeighbours(TilemapTile tmTile) {
        int x = tmTile.getX();
        int y = tmTile.getY();

        attachNeighbourFor(tmTile, Side.TOP_LEFT, x - 1, y + 1);
        attachNeighbourFor(tmTile, Side.TOP_RIGHT, x, y + 1);
        attachNeighbourFor(tmTile, Side.RIGHT, x + 1, y);
        attachNeighbourFor(tmTile, Side.BOTTOM_RIGHT, x + 1, y - 1);
        attachNeighbourFor(tmTile, Side.BOTTOM_LEFT, x, y - 1);
        attachNeighbourFor(tmTile, Side.LEFT, x - 1, y);
    }

    private void attachNeighbourFor(TilemapTile tmTile, Side side, int x, int y) {
        TilemapTile neighbour = getTilemapTile(x, y);
        if (neighbour != null) {
            neighbour.setNeighbour(getOppositeSide(side), tmTile);
        }
        tmTile.setNeighbour(side, neighbour);
    }

    private void updateTilemapTile(TilemapTile tmTile) {
        float x = tmTile.getX();
        float y = tmTile.getY();
        float tileSize = WorldSettings.getTileSize();

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

    @Override
    public int compareTo(Tilemap tilemap) {
        return Integer.compare(groupID, tilemap.groupID);
    }

}