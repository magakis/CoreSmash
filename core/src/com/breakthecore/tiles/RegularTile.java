package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.sound.SoundManager;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;
import com.breakthecore.tiles.TileContainer.Side;

import java.util.List;

public class RegularTile extends Tile implements Launchable, Matchable, Breakable {
    private SoundManager.SoundAsset destroySound;

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
            List<TilemapTile> altered = tmm.handleColorMatchesFor(newTile);
            if (pack.statsManager.isGameActive()) {
                tmm.destroyDisconnectedTiles(altered);
            }
        }
    }

    @Override
    public void onDestroy() {
        destroySound.play();
    }

    @Override
    public boolean matchesWith(int id) {
        return id == getID();
    }
}
