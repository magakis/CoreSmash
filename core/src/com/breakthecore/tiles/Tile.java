package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

abstract public class Tile {
    private int ID;

    public Tile(int id) {
        ID = id;
    }

    abstract public void onCollide(MovingBall movingBall, TilemapTile tilemapTile, GameController.BehaviourPowerPack pack);

    abstract public void update(float delta);

    public int getID() {
        return ID;
    }
}
