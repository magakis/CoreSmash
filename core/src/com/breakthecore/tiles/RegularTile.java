package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapTile;

public class RegularTile extends Tile {
    private TileAttributes ballAttr;

    public RegularTile(int id) {
        ballAttr = TileIndex.get().getAttributesFor(id);
    }

    @Override
    public boolean isMatchable() {
        return ballAttr.isMatchable();
    }

    @Override
    public boolean isBreakable() {
        return ballAttr.isBreakable();
    }

    @Override
    public boolean isPlaceable() {
        return ballAttr.isPlaceable();
    }

    @Override
    public TileType getTileType() {
        return ballAttr.getTileType();
    }

    @Override
    public int getID() {
        return ballAttr.getID();
    }

    @Override
    public TileAttributes getTileAttributes() {
        return ballAttr;
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController.BehaviourPowerPack pack) {
        TilemapTile tile = pack.tilemapManager.attachBall(movingBall, tileHit, pack.collisionDetector);
        assert (tile != null);
        pack.tilemapManager.handleColorMatchesFor(tile);
    }

    @Override
    public void update(float delta) {

    }
}
