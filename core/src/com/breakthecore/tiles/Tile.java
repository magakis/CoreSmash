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

    /* It's questionable whether I should just pass the GameScreen */
    abstract public void onCollide(
            MovingTile movingTile,
            TilemapTile tilemapTile,
            int index,
            TilemapManager tilemapManager,
            CollisionManager collisionManager
    );

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
