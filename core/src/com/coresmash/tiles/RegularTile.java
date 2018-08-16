package com.coresmash.tiles;

import com.coresmash.sound.SoundManager;
import com.coresmash.tilemap.TilemapManager;
import com.coresmash.tilemap.TilemapTile;
import com.coresmash.tiles.TileContainer.Side;

import java.util.List;

public class RegularTile extends com.coresmash.tiles.Tile implements Launchable, Matchable, Breakable {
    private SoundManager.SoundAsset destroySound;

    RegularTile(com.coresmash.tiles.TileType type) {
        super(type);
        destroySound = SoundManager.get().getSoundAsset("regularBallDestroy");
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(com.coresmash.tiles.MovingBall ball, com.coresmash.tilemap.TilemapTile tileHit, com.coresmash.GameController controller) {
        com.coresmash.GameController.BehaviourPack pack = controller.getBehaviourPack();
        com.coresmash.tilemap.TilemapManager tmm = pack.tilemapManager;
        Side[] sides = pack.collisionDetector.getClosestSides(tmm.getLayerRotation(tileHit.getLayerId()), pack.collisionDetector.getDirection(ball.getPositionInWorld(), tileHit.getPositionInWorld()));

        com.coresmash.tilemap.TilemapTile newTile = tmm.attachBall(ball.extractTile(), tileHit, sides);
        if (newTile != null) {
            List<com.coresmash.tilemap.TilemapTile> matched = tmm.getColorMatches(newTile);
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
