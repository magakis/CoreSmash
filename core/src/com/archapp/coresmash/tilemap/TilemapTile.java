package com.archapp.coresmash.tilemap;

import com.archapp.coresmash.Coords2D;
import com.archapp.coresmash.tiles.Tile;
import com.archapp.coresmash.tiles.TileContainer;
import com.badlogic.gdx.utils.Pool;

import java.util.List;

public class TilemapTile extends TileContainer implements Comparable<TilemapTile>, Pool.Poolable {
    final private Coords2D coords;
    final private NeighbourTiles neighbourTiles;
    private int distanceFromCenter;
    private int layerID;
    float rotation;
    boolean dispose;

    // PUBLIC

    public TilemapTile() {
        coords = new Coords2D();
        neighbourTiles = new NeighbourTiles();
    }

    public TilemapTile(Tile tile) {
        this();
        setTile(tile);
    }

    public void reset() {
        super.reset();
        coords.set(0, 0);
        distanceFromCenter = 0;
        layerID = 0;
        rotation = 0;
        dispose = false;
    }

    public int getX() {
        return coords.x;
    }

    public int getY() {
        return coords.y;
    }

    public int getLayerID() {
        return layerID;
    }

    public int getDistanceFromCenter() {
        return distanceFromCenter;
    }

    public void addNeighboursToList(List<TilemapTile> list) {
        for (Side side : Side.values()) {
            TilemapTile t = getNeighbour(side);
            if (t != null) {
                list.add(t);
            }
        }
    }

    public float getRotation() {
        return rotation;
    }

    public void dispose() {
        dispose = true;
    }

    public TilemapTile getNeighbour(TileContainer.Side side) {
        switch (side) {
            case BOTTOM_RIGHT:
                return neighbourTiles.bottomRight;
            case BOTTOM_LEFT:
                return neighbourTiles.bottomLeft;
            case LEFT:
                return neighbourTiles.left;
            case TOP_LEFT:
                return neighbourTiles.topLeft;
            case TOP_RIGHT:
                return neighbourTiles.topRight;
            case RIGHT:
                return neighbourTiles.right;
            default:
                throw new RuntimeException("Wrong side?(" + side + ")");
        }
    }

    public boolean isCenterTile() {
        return coords.x == 0 && coords.y == 0 && layerID == 0;
    }

    public void setCoordinates(int x, int y) {
        coords.x = x;
        coords.y = y;
        distanceFromCenter = Tilemap.getTileDistance(coords.x, coords.y, 0, 0);
    }

    /**
     * Compares first their X values and if found equal compares their Y values
     */
    @Override
    public int compareTo(TilemapTile tilemapTile) {
        int result = Integer.compare(coords.x, tilemapTile.coords.x);
        return result == 0 ? Integer.compare(coords.y, tilemapTile.coords.y) : result;
    }

    // PRIVATE

    private void detachNeighbours() {
        clearNeighbour(Side.TOP_LEFT);
        clearNeighbour(Side.TOP_RIGHT);
        clearNeighbour(Side.RIGHT);
        clearNeighbour(Side.BOTTOM_RIGHT);
        clearNeighbour(Side.BOTTOM_LEFT);
        clearNeighbour(Side.LEFT);
    }

    private void clearNeighbour(Side side) {
        TilemapTile neighbour = getNeighbour(side);
        if (neighbour != null) {
            neighbour.setNeighbour(getOppositeSide(side), null);
            setNeighbour(side, null);
        }
    }

    // PROTECTED

    void setPositionInTilemap(int tilemapID, int x, int y) {
        coords.set(x, y);
        layerID = tilemapID;
        distanceFromCenter = Tilemap.getTileDistance(coords.x, coords.y, 0, 0);
    }

    void setNeighbour(TileContainer.Side side, TilemapTile neighbour) {
        switch (side) {
            case BOTTOM_RIGHT:
                neighbourTiles.bottomRight = neighbour;
                break;
            case BOTTOM_LEFT:
                neighbourTiles.bottomLeft = neighbour;
                break;
            case LEFT:
                neighbourTiles.left = neighbour;
                break;
            case TOP_LEFT:
                neighbourTiles.topLeft = neighbour;
                break;
            case TOP_RIGHT:
                neighbourTiles.topRight = neighbour;
                break;
            case RIGHT:
                neighbourTiles.right = neighbour;
                break;
        }
    }

    void clear() {
        detachNeighbours();
    }

    private static class NeighbourTiles {
        TilemapTile left;
        TilemapTile topLeft;
        TilemapTile topRight;
        TilemapTile right;
        TilemapTile bottomRight;
        TilemapTile bottomLeft;
    }
}
