package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tilemap.effect.DestroyRadiusEffect;

public class BombBall extends Tile implements Breakable, CollisionInitiator {

    public BombBall() {
        super(TileType.BOMB_BALL);
    }

    @Override
    public void onDestroy(TilemapTile self, TilemapManager tmm) {
        DestroyRadiusEffect.newInstance(2, self.getLayerID(), self.getX(), self.getY())
                .apply(tmm);
        SoundManager.get().play(SoundManager.SoundTrack.FIREBALL_EXPLOSION);
    }

    @Override
    public boolean handleCollisionWith(TilemapTile self, MovingBall ball, GameController controller) {
        TilemapManager tmm = controller.getBehaviourPack().tilemapManager;
        ball.getLaunchable().onCollide(ball, self, controller);

        if (tmm.tileExists(self) && self.getTile() instanceof BombBall)
            onDestroy(self, tmm);

        return true;
    }
}
