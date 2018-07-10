package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.sound.SoundManager;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;
import com.breakthecore.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class FireBall extends Tile implements Launchable {
    private SoundManager.SoundAsset explosionSound = SoundManager.get().getSoundAsset("bombExplosion");
    private static List<TilemapTile> alteredTiles = new ArrayList<>();

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

        alteredTiles.clear();

        TilemapManager tmm = controller.getBehaviourPack().tilemapManager;
        for (int y = collidedTileY - 2; y < collidedTileY + 3; ++y) {
            for (int x = collidedTileX - 2; x < collidedTileX + 3; ++x) {
                if (Tilemap.getTileDistance(x, y, collidedTileX, collidedTileY) < 2) {
                    TilemapTile tmTile = tmm.getTilemapTile(tileHit.getLayerId(), x, y);
                    if (tmTile != null) {
                        tmTile.addNeighboursToList(alteredTiles);
                        tmm.removeTile(tmTile);
                    }
                }
            }
        }

        if (controller.getBehaviourPack().statsManager.isGameActive()) {
            tmm.destroyDisconnectedTiles(alteredTiles);
        }

        explosionSound.play();
    }
}
