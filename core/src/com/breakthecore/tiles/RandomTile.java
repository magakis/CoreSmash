package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionManager;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

public class RandomTile extends Tile {
    public static final int RandomTileID = TileDictionary.getIdOf(TileType.RANDOM_REGULAR);

    public RandomTile() {
        super(RandomTileID);
    }

    @Override
    public void onCollide(MovingTile movingTile, TilemapTile tilemapTile, int index, TilemapManager tilemapManager, CollisionManager collisionManager) {

    }

    @Override
    public void update(float delta) {

    }
}
