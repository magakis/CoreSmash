package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

abstract public class Tile {
    private TileType m_type;
    protected int color;

    public Tile(TileType tileType) {
        m_type = tileType;
        color = 7; // 7 is white defined by RenderManager
    }

    abstract public void onCollide(MovingTile mt, TilemapTile tt, TilemapManager tmm, CollisionManager cm);

    abstract public void update(float delta);

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public TileType getType() {
        return m_type;
    }

    public enum TileType {
        REGULAR,
        BOMB
    }
}
