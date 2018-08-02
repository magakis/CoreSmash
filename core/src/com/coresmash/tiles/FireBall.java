package com.coresmash.tiles;

import com.coresmash.sound.SoundManager;

import java.util.ArrayList;
import java.util.List;

public class FireBall extends Tile implements com.coresmash.tiles.Launchable {
    private SoundManager.SoundAsset explosionSound = SoundManager.get().getSoundAsset("bombExplosion");
    private static List<com.coresmash.tilemap.TilemapTile> toDestroy = new ArrayList<>();

    FireBall(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall movingBall, com.coresmash.tilemap.TilemapTile tileHit, com.coresmash.GameController controller) {
        int collidedTileX = tileHit.getX();
        int collidedTileY = tileHit.getY();

        toDestroy.clear();

        com.coresmash.tilemap.TilemapManager tmm = controller.getBehaviourPack().tilemapManager;
        for (int y = collidedTileY - 3; y < collidedTileY + 4; ++y) {
            for (int x = collidedTileX - 3; x < collidedTileX + 4; ++x) {
                if (com.coresmash.tilemap.Tilemap.getTileDistance(x, y, collidedTileX, collidedTileY) < 3) {
                    com.coresmash.tilemap.TilemapTile tmTile = tmm.getTilemapTile(tileHit.getLayerId(), x, y);
                    if (tmTile != null) {
                        toDestroy.add(tmTile);
                    }
                }
            }
        }

//        if (controller.getBehaviourPack().statsManager.isGameActive()) {
        tmm.destroyTiles(toDestroy);
//        }

        explosionSound.play();
    }
}
