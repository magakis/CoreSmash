package com.archapp.coresmash.tiles;

import java.util.Random;

public class ColorBomb extends Tile implements com.archapp.coresmash.tiles.Launchable {
    private static Random rand = new Random();

    public ColorBomb(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall ball, com.archapp.coresmash.tilemap.TilemapTile tileHit, com.archapp.coresmash.GameController controller) {
        int idToDestroy = tileHit.getTile() instanceof RegularTile ? tileHit.getTileID() : rand.nextInt(10);
        int collidedTileX = tileHit.getX();
        int collidedTileY = tileHit.getY();

        com.archapp.coresmash.tilemap.TilemapManager tmm = controller.getBehaviourPack().tilemapManager;

        for (int y = collidedTileY - 4; y < collidedTileY + 5; ++y) {
            for (int x = collidedTileX - 4; x < collidedTileX + 5; ++x) {
                if ((com.archapp.coresmash.tilemap.Tilemap.getTileDistance(x, y, collidedTileX, collidedTileY) < 4)) {
                    com.archapp.coresmash.tilemap.TilemapTile tmTile = tmm.getTilemapTile(tileHit.getLayerId(), x, y);
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
