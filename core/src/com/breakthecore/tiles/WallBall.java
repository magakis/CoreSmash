package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapTile;

public class WallBall extends Tile {
    public WallBall() {
        super(TileDictionary.getIdOf(TileType.WALL));
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tilemapTile, GameController.BehaviourPowerPack pack) {

    }

    @Override
    public void update(float delta) {

    }
}
