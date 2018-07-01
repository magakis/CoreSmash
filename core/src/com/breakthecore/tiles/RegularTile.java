package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;
import com.breakthecore.tiles.TileContainer.Side;

public class RegularTile extends Tile implements Launchable {

    RegularTile(int id) {
        super(id);
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
            tmm.handleColorMatchesFor(newTile);
        }
    }

}
