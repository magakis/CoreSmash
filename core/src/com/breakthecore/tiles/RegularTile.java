package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.managers.CollisionManager;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

public class RegularTile extends Tile {
    public RegularTile(int id) {
        super(id);
        if (id < 0 || id > 16) throw new IllegalArgumentException("Not a Regular Tile ID: " + id);
    }

    @Override
    public void onCollide(MovingTile movingTile, TilemapTile tileHit, int layer, TilemapManager tilemapManager, CollisionManager collisionManager) {
        Tilemap tm = tilemapManager.getTilemap(layer);
        Vector2 direction = collisionManager.getDirection(movingTile.positionInWorld, tileHit.positionInWorld);

        tilemapManager.attachTile(
                layer,
                movingTile.extractTile(),
                tileHit,
                collisionManager.getClosestSides(tm.getCos(), tm.getSin(), direction)
        );

        movingTile.dispose();
    }

    @Override
    public void update(float delta) {

    }

}
