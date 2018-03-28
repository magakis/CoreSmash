package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

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

    private float m_cosT;
    private float m_sinT;

    public Tilemap(Vector2 pos, int size, int sideLength) {
        m_size = size;
        m_position = pos;
        m_sideLength = sideLength;
        m_sideLengthHalf = sideLength / 2.f;
        m_centerTileX = size / 2;
        m_centerTileY = (size * 3) / 2;
        m_tileList = new TilemapTile[size * 3][size];
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

    public void setRotation(float cosT, float sinT) {
        m_cosT = cosT; // updating those values must happen BEFORE updating the tiles
        m_sinT = sinT;
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
        hex.setRotation(m_cosT, m_sinT);
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
        if (tile != null) {
            tile.setPositionInTilemap(x, y);
            updateTilemapTile(tile);
        }
        m_tileList[m_centerTileY + y][m_centerTileX + x] = tile;
    }

    public void setTileLiteral(int x, int y, TilemapTile tile) {
        if (tile != null) {
            tile.setPositionInTilemap(x - m_centerTileX, y - m_centerTileY);
        }
        m_tileList[y][x] = tile;
    }

    public TilemapTile getTile(int x, int y) {
        return m_tileList[m_centerTileY + y][m_centerTileX + x];
    }
}