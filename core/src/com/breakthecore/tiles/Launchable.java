package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapTile;

public interface Launchable {
    void onLaunch();

    void onCollide(MovingBall ball, TilemapTile tileHit, GameController controller);
}
