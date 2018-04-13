package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.managers.CollisionManager;
import com.breakthecore.managers.TilemapManager;

public class RegularTile extends Tile {
    public RegularTile() {
        super(TileType.REGULAR);
        m_color = WorldSettings.getRandomInt(7);

    }

    @Override
    public void onCollide(MovingTile mt, TilemapTile st, TilemapManager tmm, CollisionManager cm) {
        Tilemap tm = tmm.getTileMap();
        Vector2 direction = cm.getDirection(mt.m_positionInWorld, st.m_positionInWorld);

        tmm.addTile(mt.extractTile(), st, cm.getClosestSides(tm.getCosT(), tm.getSinT(), direction));

        mt.dispose();
    }

    @Override
    public void update(float delta) {

    }

    public void setColor(int color) {
        m_color = color;
    }
}
