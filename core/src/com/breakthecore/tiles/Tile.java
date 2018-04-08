package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.WorldSettings;
import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

import java.util.Collections;

abstract public class Tile {
    private TileType m_type;
    protected int m_color;

    public Tile(TileType tileType) {
        m_type = tileType;
        m_color = WorldSettings.getRandomInt(7);
    }

    abstract public void onCollide(MovingTile mt, TilemapTile tt, TilemapManager tmm, CollisionManager cm);

    abstract public void update(float delta);

    public int getColor() {
        return m_color;
    }

    public TileType getType() {
        return m_type;
    }

    public enum TileType {
        REGULAR,

    }
}
