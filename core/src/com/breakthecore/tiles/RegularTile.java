package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;

public class RegularTile extends Tile {
    public RegularTile(int id) {
        super(id);
        if (TileDictionary.getTypeOf(id) != TileType.REGULAR) throw new IllegalArgumentException("Not a Regular Tile ID: " + id);
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, int layer, TilemapManager tilemapManager, CollisionDetector collisionDetector) {
        Tilemap tm = tilemapManager.getTilemap(layer);
        Vector2 direction = collisionDetector.getDirection(movingBall.positionInWorld, tileHit.positionInWorld);

        tilemapManager.attachTile(
                layer,
                movingBall.extractTile(),
                tileHit,
                collisionDetector.getClosestSides(tm.getCos(), tm.getSin(), direction)
        );

        movingBall.dispose();
    }

    @Override
    public void update(float delta) {

    }

}
