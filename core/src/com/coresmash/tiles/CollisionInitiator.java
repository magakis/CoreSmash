package com.coresmash.tiles;

import com.coresmash.GameController;
import com.coresmash.tilemap.TilemapTile;

public interface CollisionInitiator {
    boolean handleCollisionWith(TilemapTile self, MovingBall ball, GameController controller);
}
