package com.coresmash.tiles;

import com.coresmash.GameController;
import com.coresmash.sound.SoundManager;
import com.coresmash.tilemap.TilemapManager;
import com.coresmash.tilemap.TilemapTile;

public class FireBall extends Tile implements Launchable {
    private SoundManager.SoundEffects explosionSound = SoundManager.get().getSoundAsset("explosion1");
    private SoundManager.SoundEffects launchSound = SoundManager.get().getSoundAsset("launch1");
    FireBall(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {
        launchSound.play();
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController controller) {
        TilemapManager.DestroyRadiusEffect effect = new TilemapManager.DestroyRadiusEffect();
        effect.setup(2, tileHit.getLayerId(), tileHit.getX(), tileHit.getY());
        effect.apply(controller.getBehaviourPack().tilemapManager);
        explosionSound.play();
    }
}
