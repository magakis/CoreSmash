package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.tilemap.TilemapTile;

public interface Launchable {
    void onLaunch();

    void onCollide(MovingBall ball, TilemapTile tileHit, GameController controller);
}
