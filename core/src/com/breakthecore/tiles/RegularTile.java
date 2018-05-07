package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.GameController;
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
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController.BehaviourPowerPack pack) {
        TilemapTile tile = pack.tilemapManager.attachBall(movingBall,tileHit,pack.collisionDetector);
        pack.tilemapManager.handleColorMatchesFor(tile);
    }

    @Override
    public void update(float delta) {

    }

}
