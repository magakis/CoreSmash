package com.breakthecore.tiles;

import com.breakthecore.Coords2D;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

public class BombTile extends Tile {
    public BombTile(int id) {
        super(id);
    }

    @Override
    public void onCollide(MovingBall mt, TilemapTile tt, int layer, TilemapManager tmm, CollisionDetector cm) {
        Coords2D posT = tt.getRelativePosition();
        Tilemap tm = tmm.getTilemap(layer);

        int collidedTileX = posT.x;
        int collidedTileY = posT.y;

        for (int y = collidedTileY - 2; y < collidedTileY + 3; ++y) {
            for (int x = collidedTileX - 2; x < collidedTileX + 3; ++x) {
                if (tm.getTileDistance(x, y, collidedTileX, collidedTileY) < 2) {
                    tm.destroyRelativeTile(x, y);
                }
            }
        }
        mt.dispose();
    }

    @Override
    public void update(float delta) {

    }
}
