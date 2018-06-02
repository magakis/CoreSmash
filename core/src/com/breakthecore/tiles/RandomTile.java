package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapTile;

public class RandomTile extends Tile {
    private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(17);

    RandomTile() {
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController.BehaviourPowerPack pack) {

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
