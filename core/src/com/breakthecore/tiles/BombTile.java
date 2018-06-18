package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapTile;

public class BombTile extends Tile {
    private final TileAttributes ballAttr = TileIndex.get().getAttributesFor(19);

    BombTile() {
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController.BehaviourPowerPack pack) {
        int collidedTileX = tileHit.getX();
        int collidedTileY = tileHit.getY();

        for (int y = collidedTileY - 2; y < collidedTileY + 3; ++y) {
            for (int x = collidedTileX - 2; x < collidedTileX + 3; ++x) {
                if (Tilemap.getTileDistance(x, y, collidedTileX, collidedTileY) < 2) {
                    pack.tilemapManager.removeTile(tileHit.getGroupId(), x, y);
                }
            }
        }
    }

    @Override
    public void update(float delta) {

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

}
