package com.breakthecore.tiles;

import com.breakthecore.Coords2D;
import com.breakthecore.Tilemap;

public class TilemapTile extends TileContainer {
    private Coords2D relativePositionInTilemap;
    private Coords2D absolutePositionInTilemap;
    private int distanceFromCenter;
    private int idTilemap;

    public TilemapTile(Tile tile) {
        setTile(tile);
        relativePositionInTilemap = new Coords2D();
        absolutePositionInTilemap = new Coords2D();
    }

    public Coords2D getAbsolutePosition() {
        return absolutePositionInTilemap;
    }

    public Coords2D getRelativePosition() {
        return relativePositionInTilemap;
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

    public void setPositionInTilemap(int tilemapID, int relativeX, int relativeY, int centerTile) {
        relativePositionInTilemap.set(relativeX, relativeY);
        absolutePositionInTilemap.set(relativeX+centerTile, relativeY+centerTile);
        idTilemap = tilemapID;
    }

    public void clear() {
        relativePositionInTilemap.set(999,999);
        absolutePositionInTilemap.set(999,999);
    }
}
