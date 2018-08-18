package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.tilemap.TilemapTile;

public interface CollisionInitiator {
    boolean handleCollisionWith(TilemapTile self, MovingBall ball, GameController controller);
}
