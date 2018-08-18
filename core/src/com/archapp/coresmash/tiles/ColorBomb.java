package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.tilemap.Tilemap;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;

import java.util.Random;

public class ColorBomb extends Tile implements Launchable {
    private static Random rand = new Random();

    public ColorBomb(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall ball, TilemapTile tileHit, GameController controller) {
        int idToDestroy = tileHit.getTile() instanceof RegularTile ? tileHit.getTileID() : rand.nextInt(10);
        int collidedTileX = tileHit.getX();
        int collidedTileY = tileHit.getY();

        TilemapManager tmm = controller.getBehaviourPack().tilemapManager;

        for (int y = collidedTileY - 4; y < collidedTileY + 5; ++y) {
            for (int x = collidedTileX - 4; x < collidedTileX + 5; ++x) {
                if ((Tilemap.getTileDistance(x, y, collidedTileX, collidedTileY) < 4)) {
                    TilemapTile tmTile = tmm.getTilemapTile(tileHit.getLayerId(), x, y);
                    if (tmTile == null ||
                            (tmTile.getX() == 0 && tmTile.getY() == 0)) continue;
                    if (tmTile.getTileID() == idToDestroy) {
                        tmm.destroyTiles(tmTile);
                    }
                }
            }
        }
    }
}