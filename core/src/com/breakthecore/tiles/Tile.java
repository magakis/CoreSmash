package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

abstract public class Tile {
    private int ID;

    public Tile(int id) {
        ID = id;
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

    public int getID() {
        return ID;
    }
}
