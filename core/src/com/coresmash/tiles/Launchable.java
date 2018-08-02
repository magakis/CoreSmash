package com.coresmash.tiles;

import com.coresmash.GameController;
import com.coresmash.tilemap.TilemapTile;

public interface Launchable {
    void onLaunch();

    void onCollide(MovingBall ball, TilemapTile tileHit, GameController controller);
}
