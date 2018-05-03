package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

abstract public class Tile {
    private int ID;
    private int subData;

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

    public int getSubData() {
        return subData;
    }

    public void setID(int id) {
        ID = id;
    }

    public int getID() {
        return ID;
    }

    public void setSubData(int data) { subData = data;}
}
