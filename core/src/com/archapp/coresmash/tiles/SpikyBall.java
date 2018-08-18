package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.tilemap.TilemapTile;

public class SpikyBall extends Tile implements CollisionInitiator {
    public SpikyBall(TileType type) {
        super(type);
    }


    @Override
    public boolean handleCollisionWith(TilemapTile self, MovingBall ball, GameController controller) {
        controller.getBehaviourPack().statsManager.loseLife();
        return true; //Simply make the ball disappear
    }
}
