package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

abstract public class Tile {
    private int ID;

    public Tile(int id) {
        ID = id;
    }

    /* It's questionable whether I should just pass the GameScreen */
    abstract public void onCollide(
            MovingBall movingBall,
            TilemapTile tilemapTile,
            int index,
            TilemapManager tilemapManager,
            CollisionDetector collisionDetector
    );

    abstract public void update(float delta);

    public int getID() {
        return ID;
    }
}
