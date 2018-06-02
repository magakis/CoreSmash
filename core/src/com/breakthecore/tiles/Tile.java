package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapTile;

public abstract class Tile {
    public abstract void onCollide(MovingBall movingBall, TilemapTile tilemapTile, GameController.BehaviourPowerPack pack);

    public abstract void update(float delta);

    public abstract boolean isMatchable();

    public abstract boolean isBreakable();

    public abstract boolean isPlaceable();

    public abstract TileType getTileType();

    public abstract int getID();

    public abstract TileAttributes getTileAttributes();
}
