package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapTile;

public interface Tile {
    void onCollide(MovingBall movingBall, TilemapTile tilemapTile, GameController.BehaviourPowerPack pack);

    void update(float delta);

    boolean isMatchable();

    boolean isBreakable();

    boolean isPlaceable();

    TileType getTileType();

    int getID();

    TileAttributes getTileAttributes();
}
