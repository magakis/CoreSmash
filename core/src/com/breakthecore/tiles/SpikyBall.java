package com.breakthecore.tiles;

import com.breakthecore.GameController;

public class SpikyBall extends Tile implements CollisionInitiator {
    public SpikyBall(TileType type) {
        super(type);
    }


    @Override
    public boolean handleCollisionWith(MovingBall ball, GameController controller) {
        controller.getBehaviourPack().statsManager.loseLife();
        return true; //Simply make the ball disappear
    }
}
