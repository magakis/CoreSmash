package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

public class RandomTile extends Tile {
    public static final int RandomTileID = TileDictionary.getIdOf(TileType.RANDOM_REGULAR);

    public RandomTile() {
        super(RandomTileID);
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController.BehaviourPowerPack pack) {

    }

    @Override
    public void update(float delta) {

    }
}
