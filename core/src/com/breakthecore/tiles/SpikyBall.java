package com.breakthecore.tiles;

import com.breakthecore.tilemap.TilemapManager;

public class SpikyBall extends Tile implements CollisionInitiator {
    public SpikyBall(int id) {
        super(id);
    }


    @Override
    public boolean handleCollisionWith(MovingBall ball, TileContainer.Side[] sides, TilemapManager manager) {
        return true; //Simply make the ball disappear
    }
}
