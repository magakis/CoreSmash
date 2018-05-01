package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

abstract public class Tile {
    private TileType tileType;
    private int subData;

    public Tile(TileType tileType) {
        this.tileType = tileType;
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

    public int getSubData() {
        return subData;
    }

    public void setSubData(int data) {
        subData = data;
    }

    public TileType getType() {
        return tileType;
    }

    public enum TileType {
        REGULAR,
        BOMB
    }
}
