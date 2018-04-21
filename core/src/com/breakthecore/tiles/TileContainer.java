package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.Observable;

/**
 * Created by Michail on 18/3/2018.
 */

public class TileContainer extends Observable {
    public static final float[] s_verticesOnMiddleEdges = generateVertices(0);
    public static final float[] s_vertices = generateVertices(30);
    protected Tile m_tile;

    protected Vector2 positionInWorld;

    public TileContainer() {
        positionInWorld = new Vector2();
    }

    private static float[] generateVertices(float atAngleDeg) {
        float[] result = new float[12];
        float angle_deg, angle_rad;

        for (int i = 0; i < 6; ++i) {
            angle_deg = 60 * i + 30 + atAngleDeg;
            angle_rad = (float) Math.PI / 180 * angle_deg;
            result[i * 2] = (float) -(Math.cos(angle_rad));
            result[i * 2 + 1] = (float) (Math.sin(angle_rad));
        }
        return result;
    }

    public Vector2 getPositionInWorld() {
        return positionInWorld;
    }

    public void setPositionInWorld(float x, float y) {
        positionInWorld.set(x, y);
    }

    public void setTile(Tile tile) {
        m_tile = tile;
    }

    public enum Side {BOTTOM_RIGHT, BOTTOM_LEFT, LEFT, TOP_LEFT, TOP_RIGHT, RIGHT}

}