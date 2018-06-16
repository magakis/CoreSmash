package com.breakthecore.tilemap;

import com.breakthecore.Coords2D;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileContainer;

public class TilemapTile extends TileContainer implements Comparable<TilemapTile> {
    final private Coords2D coords;
    final private NeighbourTiles neighbourTiles;
    private int distanceFromCenter;
    private int groupID;

    public TilemapTile(Tile tile) {
        setTile(tile);
        coords = new Coords2D();
        neighbourTiles = new NeighbourTiles();
    }

    public Coords2D getCoords() {
        return coords;
    }

    public int getGroupId() {
        return groupID;
    }

    public int getDistanceFromCenter() {
        return distanceFromCenter;
    }

    public void setNeighbour(TileContainer.Side side, TilemapTile neighbour) {
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

    public void setCoordinates(int x, int y) {
        coords.x = x;
        coords.y = y;
        distanceFromCenter = Tilemap.getTileDistance(coords.x, coords.y, 0, 0);
    }

    public void setPositionInTilemap(int tilemapID, int x, int y) {
        coords.set(x, y);
        groupID = tilemapID;
        distanceFromCenter = Tilemap.getTileDistance(coords.x, coords.y, 0, 0);
    }

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
        }
        setNeighbour(side, null);
    }


    public void clear() {
        coords.set(999, 999);
        detachNeighbours();
    }

    @Override
    /** Compares first their X values and if found equal compares their Y values */
    public int compareTo(TilemapTile tilemapTile) {
        int result = Integer.compare(coords.x, tilemapTile.coords.x);
        return result == 0 ? Integer.compare(coords.y, tilemapTile.coords.y) : result;
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
