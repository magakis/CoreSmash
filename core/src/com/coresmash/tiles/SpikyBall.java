package com.coresmash.tiles;

public class SpikyBall extends com.coresmash.tiles.Tile implements com.coresmash.tiles.CollisionInitiator {
    public SpikyBall(com.coresmash.tiles.TileType type) {
        super(type);
    }


    @Override
    public boolean handleCollisionWith(MovingBall ball, com.coresmash.GameController controller) {
        controller.getBehaviourPack().statsManager.loseLife();
        return true; //Simply make the ball disappear
    }
}
