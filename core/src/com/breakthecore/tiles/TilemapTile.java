package com.breakthecore.tiles;

import com.breakthecore.Coords2D;

public class TilemapTile extends TileContainer {
    private Coords2D relativePositionInTilemap;
    private Coords2D absolutePositionInTilemap;

    public TilemapTile(Tile tile) {
        m_tile = tile;
        relativePositionInTilemap = new Coords2D();
        absolutePositionInTilemap = new Coords2D();
    }

    public Tile getTile() {
        return m_tile;
    }

    public Coords2D getAbsolutePositionInTilemap() {
        return absolutePositionInTilemap;
    }

    public Coords2D getRelativePositionInTilemap() {
        return relativePositionInTilemap;
    }

    public void setPositionInTilemap(int relativeX, int relativeY, int centerTile) {
        relativePositionInTilemap.set(relativeX, relativeY);
        absolutePositionInTilemap.set(relativeX+centerTile, relativeY+centerTile);
    }

    public void clear() {
        clearObserverList();
        relativePositionInTilemap.set(999,999);
        absolutePositionInTilemap.set(999,999);
    }

    public int getColor() {
        return m_tile.getColor();
    }

    int calcDistnanceFromCenter(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx+dy)));
    }

}
