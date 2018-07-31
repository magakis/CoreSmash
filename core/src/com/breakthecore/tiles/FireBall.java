package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.sound.SoundManager;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

import java.util.ArrayList;
import java.util.List;

public class FireBall extends Tile implements Launchable {
    private SoundManager.SoundAsset explosionSound = SoundManager.get().getSoundAsset("bombExplosion");
    private static List<TilemapTile> toDestroy = new ArrayList<>();

    FireBall(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController controller) {
        int collidedTileX = tileHit.getX();
        int collidedTileY = tileHit.getY();

        toDestroy.clear();

        TilemapManager tmm = controller.getBehaviourPack().tilemapManager;
        for (int y = collidedTileY - 3; y < collidedTileY + 4; ++y) {
            for (int x = collidedTileX - 3; x < collidedTileX + 4; ++x) {
                if (Tilemap.getTileDistance(x, y, collidedTileX, collidedTileY) < 3) {
                    TilemapTile tmTile = tmm.getTilemapTile(tileHit.getLayerId(), x, y);
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
