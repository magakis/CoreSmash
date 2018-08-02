package com.coresmash.tiles;

public interface CollisionInitiator {
    boolean handleCollisionWith(MovingBall ball, com.coresmash.GameController controller);
}
