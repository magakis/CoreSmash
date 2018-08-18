package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.TileContainer.Side;

import java.util.List;

public class RegularTile extends Tile implements Launchable, Matchable, Breakable {
    private SoundManager.SoundEffects destroySound;

    RegularTile(TileType type) {
        super(type);
        destroySound = SoundManager.get().getSoundAsset("regularBallDestroy");
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall ball, TilemapTile tileHit, GameController controller) {
        GameController.BehaviourPack pack = controller.getBehaviourPack();
        TilemapManager tmm = pack.tilemapManager;
        Side[] sides = pack.collisionDetector.getClosestSides(tmm.getLayerRotation(tileHit.getLayerId()), pack.collisionDetector.getDirection(ball.getPositionInWorld(), tileHit.getPositionInWorld()));

        TilemapTile newTile = tmm.attachBall(ball.extractTile(), tileHit, sides);
        if (newTile != null) {
            List<TilemapTile> matched = tmm.getColorMatches(newTile);
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
