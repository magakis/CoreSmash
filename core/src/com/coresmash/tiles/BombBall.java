package com.coresmash.tiles;

import com.coresmash.GameController;
import com.coresmash.NotificationType;
import com.coresmash.sound.SoundManager;
import com.coresmash.tilemap.TilemapManager;
import com.coresmash.tilemap.TilemapTile;

public class BombBall extends Tile implements Breakable, CollisionInitiator {
    private static SoundManager.SoundAsset explosion = SoundManager.get().getSoundAsset("bombExplosion");

    public BombBall() {
        super(TileType.BOMB_BALL);
    }

    @Override
    public void onDestroy(TilemapTile self, TilemapManager tmm) {
        TilemapManager.DestroyRadiusEffect effect = new TilemapManager.DestroyRadiusEffect();
        effect.setup(2, self.getLayerId(), self.getX(), self.getY());
        effect.apply(tmm);
        explosion.play();
    }

    @Override
    public boolean handleCollisionWith(TilemapTile self, MovingBall ball, GameController controller) {
        controller.getBehaviourPack().tilemapManager.destroyTiles(self);
        if (ball.getTile().getTileType().getMajorType().equals(TileType.MajorType.REGULAR)) {
            controller.getBehaviourPack().statsManager.onNotify(NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED, null);
        }
        return true;
    }
}
