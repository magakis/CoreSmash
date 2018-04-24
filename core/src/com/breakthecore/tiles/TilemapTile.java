package com.breakthecore.tiles;

import com.breakthecore.Coords2D;
import com.breakthecore.Tilemap;

public class TilemapTile extends TileContainer {
    private Coords2D relativePositionInTilemap;
    private Coords2D absolutePositionInTilemap;
    private int distanceFromCenter;
    private int idTilemap;

    public TilemapTile(Tile tile) {
        m_tile = tile;
        relativePositionInTilemap = new Coords2D();
        absolutePositionInTilemap = new Coords2D();
    }

    public Tile getTile() {
        return m_tile;
    }

    public Coords2D getAbsolutePosition() {
        return absolutePositionInTilemap;
    }

    public Coords2D getRelativePosition() {
        return relativePositionInTilemap;
    }

    public int getColor() {
        return m_tile.getColor();
    }

    public int getTilemapId() {
        return idTilemap;
    }

    public int getDistanceFromCenter() {
        return distanceFromCenter;
    }

    public void setDistanceFromCenter(Tilemap tm) {
        this.distanceFromCenter = tm.getTileDistance(relativePositionInTilemap.x,relativePositionInTilemap.y, 0,0);
    }

    public void setPositionInTilemap(int relativeX, int relativeY, int centerTile) {
        relativePositionInTilemap.set(relativeX, relativeY);
        absolutePositionInTilemap.set(relativeX+centerTile, relativeY+centerTile);
    }

    public void setTilemapId(int id) {
        idTilemap = id;
    }

    public void clear() {
        clearObserverList();
        relativePositionInTilemap.set(999,999);
        absolutePositionInTilemap.set(999,999);
    }
}
