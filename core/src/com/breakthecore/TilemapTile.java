package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

public class TilemapTile extends Tile {
    private Vector2 m_positionInTilemap;
    private float m_distanceFromCenter;

    public TilemapTile(float tilex , float tiley) {
        m_positionInTilemap = new Vector2(tilex, tiley);
        m_distanceFromCenter = calcDistnanceFromCenter(tilex , tiley);
    }

    public TilemapTile(float tilex, float tiley, int colorid) {
        this(tilex, tiley);
        setColor(colorid);
    }

    public TilemapTile(int color) {
        super(color);
        m_positionInTilemap = new Vector2();
    }

    public float getDistanceFromCenter() {
        return m_distanceFromCenter;
    }

    public Vector2 getPositionInTilemap() {
        return m_positionInTilemap;
    }

    public void setPositionInTilemap(float tilex, float tiley) {
        m_positionInTilemap.set(tilex, tiley);
    }

    //XXX: calcDistanceFromCenter is not accurate at all!...
    private float calcDistnanceFromCenter(float tileX, float tileY) {
        float result = 0;
        float xOffset = ((tileY) % 2 == 0)? 0 : 1f;

        result = (Math.round(Vector2.dst(tileX*2f+xOffset,tileY*0.5f,0,0)));
        return result;
    }
}
