package com.breakthecore.tiles;

import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tiles.TileContainer.Side;

public interface CollisionInitiator {
    boolean handleCollisionWith(MovingBall ball, Side[] sides, TilemapManager manager);
}
