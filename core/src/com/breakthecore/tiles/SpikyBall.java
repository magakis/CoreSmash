package com.breakthecore.tiles;

import com.breakthecore.GameController;

public class SpikyBall extends Tile implements CollisionInitiator {
    public SpikyBall(int id) {
        super(id);
    }


    @Override
    public boolean handleCollisionWith(MovingBall ball, GameController controller) {
        controller.getBehaviourPack().statsManager.loseLife();
        return true; //Simply make the ball disappear
    }
}
