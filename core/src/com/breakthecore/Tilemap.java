package com.breakthecore;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.tiles.TilemapTile;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tilemap {
    private int m_size;
    private int m_sideLength;
    private float m_sideLengthHalf;
    private int m_centerTileX;
    private int m_centerTileY;
    private Vector2 m_position;
    private TilemapTile[][] m_tileList;

    private float m_rotDegrees;
    private float m_cosT;
    private float m_sinT;

    private int m_tileCount;

    public Tilemap(Vector2 pos, int size, int sideLength) {
        m_size = size;
        m_position = pos;
        m_sideLength = sideLength;
        m_sideLengthHalf = sideLength / 2.f;
        m_centerTileX = size / 2;
        m_centerTileY = (size * 3) / 2;
        m_tileList = new TilemapTile[size * 3][size];
        m_cosT = 1;
        m_sinT = 0;
    }

    public int getCenterTileXPos() {
        return m_centerTileX;
    }

    public int getCenterTileYPos() {
        return m_centerTileY;
    }

    public int getSize() {
        return m_size;
    }

    public float getRotDegrees() {
        return m_rotDegrees;
    }

    public void clear() {
        for (int y = 0; y < m_size * 3; ++y) {
            for (int x = 0; x < m_size; ++x) {
                m_tileList[y][x] = null;
            }
        }
        m_tileCount = 0;
        m_rotDegrees = 0;
        m_cosT = 1;
        m_sinT = 0;
    }

    public void rotate(float deg) {
        m_rotDegrees += deg;
        setRotation(m_rotDegrees);
    }

    public void setRotation(float deg) {
        m_rotDegrees = deg;
        float rotRad = (float) Math.toRadians(deg);
        m_cosT = (float) Math.cos(rotRad);
        m_sinT = (float) Math.sin(rotRad);

        for (TilemapTile[] arr : m_tileList) {
            for (TilemapTile hex : arr) {
                if (hex != null) {
                    updateTilemapTile(hex);
                }
            }
        }
    }

    public TilemapTile[][] getTilemapTiles() {
        return m_tileList;
    }

    public int getSideLength() {
        return m_sideLength;
    }

    public void updateTilemapTile(TilemapTile hex) {
        Vector2 tilePos = hex.getPositionInTilemap();
        float x = tilePos.x;
        float y = tilePos.y;

        float X_world, Y_world;
        float tileXDistance = m_sideLength + m_sideLengthHalf;
        float tileYDistance = m_sideLengthHalf;

        float xOffset = ((y) % 2 == 1) || ((y) % 2 == -1) ? m_sideLengthHalf * 1.5f : 0;

        X_world = m_position.x +
                (x * tileXDistance + xOffset) * m_cosT +
                (y * tileYDistance) * m_sinT;

        Y_world = m_position.y +
                (x * tileXDistance + xOffset) * -m_sinT +
                (y * tileYDistance) * m_cosT;

        hex.setPositionInWorld(X_world, Y_world);
    }

    public void setTile(int x, int y, TilemapTile tile) {
        if (tile == null) return;

        if (getTile(x, y) == null) {
            ++m_tileCount;
        }

        tile.setPositionInTilemap(x, y);
        updateTilemapTile(tile);

        m_tileList[m_centerTileY + y][m_centerTileX + x] = tile;
    }

    public void desrtoyTile(int tileX, int tileY) {
        TilemapTile t = getTile(tileX, tileY);
        if (t == null) return;
        t.notifyObservers(NotificationType.NOTIFICATION_TYPE_TILE_DESTROYED, null);
        t.emptyObserverList();
        emptyTileAt(tileX, tileY);
        --m_tileCount;
    }

    public int getTileCount() {
        return m_tileCount;
    }

    public void setTileLiteral(int x, int y, TilemapTile tile) {
        setTile(x - m_centerTileX, y - m_centerTileY, tile);
    }

    public TilemapTile getTile(int x, int y) {
        return m_tileList[m_centerTileY + y][m_centerTileX + x];
    }

    private void emptyTileAt(int x, int y) {
        m_tileList[y + m_centerTileY][x + m_centerTileX] = null;
    }

    public float getCosT() {
        return m_cosT;
    }

    public float getSinT() {
        return m_sinT;
    }

    public Vector2 getPositionInWorld() {
        return new Vector2(m_position);
    }
}