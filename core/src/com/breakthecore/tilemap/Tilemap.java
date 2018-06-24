package com.breakthecore.tilemap;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
    private Coords2D defPosition;
    private Vector2 worldPosition;
    private Vector2 offset;
    private Vector2 origin;
    private int[] colorsAvailable;

    private boolean isTilemapInitilized;
    private boolean isChained;
    private boolean rotateCounterClockwise;
    private boolean autoRotationEnabled;
    private float rotation;
    private float originRotation;

    private int minMapRotationSpeed;
    private int maxMapRotationSpeed;
    private float mapSpeedDiff;

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
        worldPosition = new Vector2();
        origin = new Vector2();
        offset = new Vector2();
    }

    public Tilemap(int id, Coords2D defPosition) {
        this(id);
        this.defPosition = defPosition;
        worldPosition.set(defPosition.x, defPosition.y);
    }

    public static int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
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

    public List<TilemapTile> getTileList() {
        return Collections.unmodifiableList(tilemapTiles);
    }

    public Vector3 getWorldToTilemapCoords(Vector3 world) {
        world.x -= worldPosition.x;
        world.y -= worldPosition.y;

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

    public void putTilemapTile(TilemapTile tmTile, Tile tile, Side side) {
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
    }

    public void initialize(TilemapBuilder.TilemapBuilderInfo settings) {
        isChained = settings.isChained();
        rotateCounterClockwise = settings.isRotatingCCW();
        minRotationSpeed = settings.getMinRotSpeed();
        maxRotationSpeed = settings.getMaxRotSpeed();
        speedDiff = maxRotationSpeed - minRotationSpeed;

        minMapRotationSpeed = settings.getMapMinRotSpeed();
        maxMapRotationSpeed = settings.getMapMaxRotSpeed();
        mapSpeedDiff = maxMapRotationSpeed - minMapRotationSpeed;

        autoRotationEnabled = maxRotationSpeed != 0;
        isTilemapInitilized = true;
        origin.set(settings.getOriginX(), settings.getOriginY());
        offset.set(settings.getOffsetX(), settings.getOffsetY());
        updateWorldPosition();
        updateTilePositions();
    }

    public void update(float delta) {
        rotateOrigin(MathUtils.clamp(maxMapRotationSpeed - speedDiff * ((float) tilemapTiles.size() / initTileCount), minMapRotationSpeed, maxMapRotationSpeed) * delta);
        rotate(MathUtils.clamp(maxRotationSpeed - speedDiff * ((float) tilemapTiles.size() / initTileCount), minRotationSpeed, maxRotationSpeed) * delta);
        updateTilePositions();
    }

    private void rotateOrigin(float deg) {
        originRotation += Math.toRadians(deg);
        float cos = (float) Math.cos(originRotation);
        float sin = (float) Math.sin(originRotation);

        worldPosition.x = defPosition.x + origin.x
                + offset.x * cos
                + offset.y * sin;

        worldPosition.y = defPosition.y + origin.y +
                offset.x * -sin +
                offset.y * cos;

    }

    @Override
    public int compareTo(Tilemap tilemap) {
        return Integer.compare(groupID, tilemap.groupID);
    }

    void updateTilePositions() {
        for (TilemapTile tmTile : tilemapTiles) {
            updateTilemapTile(tmTile);
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

        worldPosition.set(defPosition.x, defPosition.y);
        origin.setZero();
        offset.setZero();

        minRotationSpeed = 0;
        maxRotationSpeed = 0;
        minMapRotationSpeed = 0;
        maxMapRotationSpeed = 0;
        mapSpeedDiff = 0;
        originRotation = 0;
        maxDistanceFromCenter = 0;
        isChained = false;
        isTilemapInitilized = false;
        speedDiff = 0;
        initTileCount = 0;
        rotation = 0;
        cos = 1;
        sin = 0;
    }

    void setMapPosition(float x, float y) {
        worldPosition.set(x, y);
        offset.set(x - (defPosition.x + origin.x), y - (defPosition.y + origin.y));
    }

    void setOrigin(float x, float y) {
        origin.set(x, y);
        updateWorldPosition();
    }

    void setOffset(float x, float y) {
        offset.set(x, y);
        updateWorldPosition();
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

    public float getPositionX() {
        return worldPosition.x;
    }

    public float getPositionY() {
        return worldPosition.y;
    }

    public float getOriginX() {
        return origin.x;
    }

    public float getOriginY() {
        return origin.y;
    }

    public float getOffsetX() {
        return offset.x;
    }

    public float getOffsetY() {
        return offset.y;
    }

    void updateWorldPosition() {
        worldPosition.set(defPosition.x, defPosition.y).add(origin).add(offset);
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

        X_world = worldPosition.x +
                (x * tileXDistance + y * tileXDistanceHalf) * cos +
                (y * tileYDistance) * sin;

        Y_world = worldPosition.y +
                (x * tileXDistance + y * tileXDistanceHalf) * -sin +
                (y * tileYDistance) * cos;

        tmTile.setPositionInWorld(X_world, Y_world);
    }

}