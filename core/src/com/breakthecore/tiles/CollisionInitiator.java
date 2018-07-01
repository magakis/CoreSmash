package com.breakthecore.tiles;

import com.breakthecore.GameController;

public interface CollisionInitiator {
    boolean handleCollisionWith(MovingBall ball, GameController controller);
}
