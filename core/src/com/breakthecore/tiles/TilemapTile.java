package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;

public class TilemapTile extends TileContainer {
    private Vector2 m_positionInTilemap;
    private int m_distanceFromCenter;

    public TilemapTile(float tilex , float tiley) {
        m_positionInTilemap = new Vector2(tilex, tiley);
        m_distanceFromCenter = calcDistnanceFromCenter((int)tilex , (int)tiley, 0, 0);
    }

    public TilemapTile(float tilex, float tiley, Tile tile) {
        this(tilex, tiley);
        m_tile = tile;
    }

    public TilemapTile(Tile tile) {
        m_tile = tile;
        m_positionInTilemap = new Vector2();
    }

    public Tile getTile() {
        return m_tile;
    }

    public int getDistanceFromCenter() {
        return m_distanceFromCenter;
    }

    public Vector2 getPositionInTilemap() {
        return m_positionInTilemap;
    }

    public void setPositionInTilemap(float tilex, float tiley) {
        m_positionInTilemap.set(tilex, tiley);
        m_distanceFromCenter = calcDistnanceFromCenter((int)tilex, (int)tiley, 0, 0);
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
