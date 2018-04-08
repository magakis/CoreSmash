package com.breakthecore.tiles;

import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

/*
 * Rework this class into a special tile cause it's useless right now
 */

public class SpecialTile extends Tile {
    public SpecialTile() {
        super(TileType.REGULAR);
    }

    @Override
    public void onCollide(MovingTile mt, TilemapTile tt, TilemapManager tmm, CollisionManager cm) {

    }

    @Override
    public void update(float delta) {

    }
}
