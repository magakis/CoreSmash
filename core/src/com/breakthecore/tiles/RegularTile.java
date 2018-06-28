package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

public class RegularTile extends Tile implements Launchable {

    RegularTile(int id) {
        super(id);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall ball, TilemapTile tileHit, GameController.BehaviourPack pack) {
        TilemapManager tmm = pack.tilemapManager;
        TilemapTile newTile = tmm.attachBall(ball, tileHit, pack.collisionDetector);
        tmm.handleColorMatchesFor(newTile);
    }

}
