package com.archapp.coresmash.tiles;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tilemap.effect.DestroyRadiusEffectConditional;

public class ColorBomb extends Tile implements Launchable {

    public ColorBomb(TileType type) {
        super(type);
    }

    @Override
    public void onLaunch() {

    }

    @Override
    public void onCollide(MovingBall ball, final TilemapTile tileHit, GameController controller) {
        if (!tileHit.getTile().getTileType().getMajorType().equals(TileType.MajorType.REGULAR))
            return;

        DestroyRadiusEffectConditional.newInstance(4, tileHit.getLayerID(), tileHit.getX(), tileHit.getY(), new DestroyRadiusEffectConditional.Condition() {
            @Override
            public boolean isConditionMet(TilemapTile tile) {
                return tile.getTileID() == tileHit.getTileID() && !tile.isCenterTile();
            }
        }).apply(controller.getBehaviourPack().tilemapManager);
    }
}
