package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.NotificationType;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tilemap.effect.DestroyRadiusEffect;

public class BombBall extends Tile implements Breakable, CollisionInitiator {
    private static SoundManager.SoundEffect explosion = SoundManager.get().getSoundAsset("explosion1");

    public BombBall() {
        super(TileType.BOMB_BALL);
    }

    @Override
    public void onDestroy(TilemapTile self, TilemapManager tmm) {
        DestroyRadiusEffect.newInstance(2, self.getLayerId(), self.getX(), self.getY())
                .apply(tmm);
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
