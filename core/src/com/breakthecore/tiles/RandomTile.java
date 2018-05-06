package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

public class RandomTile extends Tile {
    public static final int RandomTileID = TileDictionary.getIdOf(TileType.RANDOM_REGULAR);

    public RandomTile() {
        super(RandomTileID);
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tilemapTile, int index, TilemapManager tilemapManager, CollisionDetector collisionDetector) {

    }

    @Override
    public void update(float delta) {

    }
}
