package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.Coords2D;
import com.breakthecore.Tilemap;
import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

public class BombTile extends Tile {
    public BombTile() {
        super(TileType.BOMB);
    }

    @Override
    public void onCollide(MovingTile mt, TilemapTile tt, TilemapManager tmm, CollisionManager cm) {
        Coords2D posT = tt.getRelativePositionInTilemap();
        Tilemap tm = tmm.getTileMap();

        int collidedTileX = posT.x;
        int collidedTileY = posT.y;

        for (int y = collidedTileY - 2; y < collidedTileY + 3; ++y) {
            for (int x = collidedTileX - 2; x < collidedTileX + 3; ++x) {
                if (tmm.getTileDistance(x, y, collidedTileX, collidedTileY) < 2) {
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
