package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.TileContainer.Side;

import java.util.List;

public class RegularTile extends Tile implements Launchable, Matchable, Breakable {

    RegularTile(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {
    }

    @Override
    public void onCollide(MovingBall ball, TilemapTile tileHit, GameController controller) {
        GameController.BehaviourPack pack = controller.getBehaviourPack();
        TilemapManager tmm = pack.tilemapManager;
        Side[] sides = pack.collisionDetector.getClosestSides(tmm.getLayerRotation(tileHit.getLayerID()), pack.collisionDetector.getDirection(ball.getPositionInWorld(), tileHit.getPositionInWorld()));

        TilemapTile newTile = tmm.attachBall(ball.extractTile(), tileHit, sides);
        if (newTile != null) {
            List<TilemapTile> matched = tmm.getColorMatches(newTile);
            tmm.destroyTiles(matched);
        }
    }

    @Override
    public void onDestroy(TilemapTile self, TilemapManager manager) {
        SoundManager.get().play(SoundManager.SoundTrack.REGULAR_BALL_DESTROY);
    }

    @Override
    public boolean matchesWith(int id) {
        return id == getID();
    }
}
