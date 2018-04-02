package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

public class TilemapTile extends TileContainer {
    private Vector2 m_positionInTilemap;
    private int m_distanceFromCenter;

    public TilemapTile(float tilex , float tiley) {
        m_positionInTilemap = new Vector2(tilex, tiley);
        m_distanceFromCenter = calcDistnanceFromCenter(tilex , tiley);
    }

    public TilemapTile(float tilex, float tiley, Tile tile) {
        this(tilex, tiley);
        m_tile = tile;
    }

    public TilemapTile(Tile tile) {
        m_tile = tile;
        m_positionInTilemap = new Vector2();
    }

    public int getDistanceFromCenter() {
        return m_distanceFromCenter;
    }

    public Vector2 getPositionInTilemap() {
        return m_positionInTilemap;
    }

    public void setPositionInTilemap(float tilex, float tiley) {
        m_positionInTilemap.set(tilex, tiley);
        m_distanceFromCenter = calcDistnanceFromCenter(tilex, tiley);
    }

    public int getColor() {
        return m_tile.getColor();
    }

    //XXX: calcDistanceFromCenter is not accurate at all!...
    private int calcDistnanceFromCenter(float tileX, float tileY) {
        int result = 0;
        float xOffset = ((tileY) % 2 == 0)? 0 : 1f;

        result = (int) (Math.round(Vector2.dst(tileX * 2f + xOffset, tileY * 0.5f, 0, 0)));
        return result;
    }
}
