package com.coresmash.tiles;

import com.coresmash.GameController;
import com.coresmash.sound.SoundManager;
import com.coresmash.tilemap.TilemapTile;
import com.coresmash.tilemap.effect.DestroyRadiusEffect;

public class FireBall extends Tile implements Launchable {
    private static final SoundManager.SoundEffects explosionSound = SoundManager.get().getSoundAsset("explosion1");
    private static final SoundManager.SoundEffects launchSound = SoundManager.get().getSoundAsset("launch1");

    FireBall(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {
        launchSound.play();
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController controller) {
        DestroyRadiusEffect.newInstance(2, tileHit.getLayerId(), tileHit.getX(), tileHit.getY())
                .apply(controller.getBehaviourPack().tilemapManager);
        explosionSound.play();
    }
}
