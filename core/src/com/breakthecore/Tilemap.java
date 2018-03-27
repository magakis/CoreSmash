package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tilemap {
    private int m_size;
    private int m_sideLength;
    private Vector2 m_position;
    private TilemapTile[][] m_hexTiles;

    private float m_sideLengthHalf;
    private float rotationDegrees;
    private float rotationSpeed = 360 / 10.f;

    private boolean isRotating = true;

    public Tilemap(Vector2 pos, int size, int sideLength) {
        m_size = size;
        m_position = pos;
        m_sideLength = sideLength;
        m_sideLengthHalf = sideLength/2.f;

        m_hexTiles = new TilemapTile[size*3][size];


        rotationDegrees = 0;

        float[] vert = Tile.getVertices();
        for (int i = 0; i < 12; ++i) {
            vert[i] *= 30;
        }

    }

    public int getSize() {
        return m_size;
    }

    public Vector2 getWorldPosition() {
        return m_position;
    }

    public void setRotation(float cosT, float sinT) {
        for (TilemapTile[] arr : m_hexTiles) {
            for (TilemapTile hex : arr) {
                if (hex != null) {
                    updateTilemapTile(hex, cosT, sinT);
                }
            }
        }
    }

    public TilemapTile[][] getTilemapTiles() {
        return m_hexTiles;
    }

    public int getSideLength() {
        return m_sideLength;
    }

    public void updateTilemapTile(TilemapTile hex, float cos, float sin) {
        hex.setRotation(cos, sin);
        Vector2 tilePos = hex.getPositionInTilemap();
        float x = tilePos.x;
        float y = tilePos.y;

        float X_world, Y_world;
        float tileXDistance = m_sideLength + m_sideLengthHalf;
        float tileYDistance = m_sideLengthHalf;

        float xOffset = ((y) % 2 == 1) || ((y) % 2 == -1) ? m_sideLengthHalf*1.5f : 0;

        X_world = m_position.x +
                (x * tileXDistance + xOffset) * cos +
                (y * tileYDistance) * sin;

        Y_world = m_position.y +
                (x * tileXDistance + xOffset) * -sin +
                (y * tileYDistance) * cos;

        hex.setPositionInWorld(X_world, Y_world);
    }

}