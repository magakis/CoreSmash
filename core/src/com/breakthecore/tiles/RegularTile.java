package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

public class RegularTile extends Tile {
    public RegularTile() {
        super(TileType.REGULAR);
        color = WorldSettings.getRandomInt(7);

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
