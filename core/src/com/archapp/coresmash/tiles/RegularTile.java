package com.archapp.coresmash.tiles;

import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.TileContainer.Side;

import java.util.List;

public class RegularTile extends com.archapp.coresmash.tiles.Tile implements Launchable, Matchable, Breakable {
    private SoundManager.SoundEffects destroySound;

    RegularTile(com.archapp.coresmash.tiles.TileType type) {
        super(type);
        destroySound = SoundManager.get().getSoundAsset("regularBallDestroy");
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(com.archapp.coresmash.tiles.MovingBall ball, com.archapp.coresmash.tilemap.TilemapTile tileHit, com.archapp.coresmash.GameController controller) {
        com.archapp.coresmash.GameController.BehaviourPack pack = controller.getBehaviourPack();
        com.archapp.coresmash.tilemap.TilemapManager tmm = pack.tilemapManager;
        Side[] sides = pack.collisionDetector.getClosestSides(tmm.getLayerRotation(tileHit.getLayerId()), pack.collisionDetector.getDirection(ball.getPositionInWorld(), tileHit.getPositionInWorld()));

        com.archapp.coresmash.tilemap.TilemapTile newTile = tmm.attachBall(ball.extractTile(), tileHit, sides);
        if (newTile != null) {
            List<com.archapp.coresmash.tilemap.TilemapTile> matched = tmm.getColorMatches(newTile);
            if (pack.statsManager.isGameActive()) {
                tmm.destroyTiles(matched);
            }
        }
    }

    @Override
    public void onDestroy(TilemapTile self, TilemapManager manager) {
        destroySound.play();
    }

    @Override
    public boolean matchesWith(int id) {
        return id == getID();
    }
}
