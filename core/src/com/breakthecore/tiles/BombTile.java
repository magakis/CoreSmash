package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapTile;

public class BombTile extends Tile implements Launchable {

    BombTile(int id) {
        super(id);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController.BehaviourPack pack) {
        int collidedTileX = tileHit.getX();
        int collidedTileY = tileHit.getY();

        for (int y = collidedTileY - 2; y < collidedTileY + 3; ++y) {
            for (int x = collidedTileX - 2; x < collidedTileX + 3; ++x) {
                if (Tilemap.getTileDistance(x, y, collidedTileX, collidedTileY) < 2) {
                    pack.tilemapManager.removeTile(tileHit.getGroupId(), x, y);
                }
            }
        }
    }
}
