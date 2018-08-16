package com.coresmash.tiles;

import com.coresmash.GameController;
import com.coresmash.sound.SoundManager;
import com.coresmash.tilemap.TilemapManager;
import com.coresmash.tilemap.TilemapTile;

public class FireBall extends Tile implements Launchable {
    private SoundManager.SoundAsset explosionSound = SoundManager.get().getSoundAsset("bombExplosion");
    FireBall(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController controller) {
        TilemapManager.DestroyRadiusEffect effect = new TilemapManager.DestroyRadiusEffect();
        effect.setup(2, tileHit.getLayerId(), tileHit.getX(), tileHit.getY());
        effect.apply(controller.getBehaviourPack().tilemapManager);
        explosionSound.play();
    }
}
