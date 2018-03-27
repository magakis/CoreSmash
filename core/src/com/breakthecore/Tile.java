package com.breakthecore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;


/**
 * Created by Michail on 18/3/2018.
 */

public class Tile {
    private static final float[] s_vertices = generateVertices(30);
    private static final float[] s_verticesOnMiddleEdges = generateVertices(0);

    private float[] m_verticesInDimension;

    private Vector2 m_positionInTilemap;
    private Vector2 m_positionInWorld;
    private int color;

    private float m_cosTheta;
    private float m_sinTheta;


    public Tile () {
        m_positionInTilemap = new Vector2();
        m_positionInWorld = new Vector2();
        m_verticesInDimension = new float[12];
        m_cosTheta = 1;
        m_sinTheta = 0;
    }

    public Tile(int color) {
        this();
        this.color = color;
    }

    public Tile(Vector2 tilePos) {
        this();
        m_positionInTilemap.set(tilePos);
    }

    public Tile(Vector2 tilePos, int color) {
        this(tilePos);
        this.color = color;
    }


    public int getColor() {return color;}

    private static float[] generateVertices(float atAngleDeg) {
        float[] result = new float[12];
        float angle_deg, angle_rad;

        for (int i = 0; i < 6; ++i) {
            angle_deg = 60 * i + 30 + atAngleDeg;
            angle_rad = (float)Math.PI / 180 * angle_deg;
            result[i * 2] = (float) -(Math.cos(angle_rad));
            result[i * 2 + 1] = (float) (Math.sin(angle_rad));
        }
        return result;
    }

    public Vector2 getPositionInWorld() {
        return m_positionInWorld;
    }

    public Vector2 getPositionInTilemap() {
        return m_positionInTilemap;
    }

    public float getCosTheta() {
        return m_cosTheta;
    }
    public float getSinTheta() {
        return m_sinTheta;
    }

    public void setPositionInTilemap(float x, float y) {
        m_positionInTilemap.x = x;
        m_positionInTilemap.y = y;
    }

    public void setPositionInWorld(float x, float y) {
        m_positionInWorld.set(x, y);
    }

    public void setRotation(float cosTheta, float sinTheta) {
        m_cosTheta = cosTheta;
        m_sinTheta = sinTheta;
    }

    public static float[] getVertices() {return s_vertices;}

    public float[] getVerticesOnMiddleEdges() {
        float[] result = new float[12];
        for (int i = 0; i < 12; i += 2) {
            result[i] = s_verticesOnMiddleEdges[i] * m_cosTheta + s_verticesOnMiddleEdges[i + 1] * m_sinTheta;
            result[i + 1] = s_verticesOnMiddleEdges[i] * -m_sinTheta + s_verticesOnMiddleEdges[i + 1] * m_cosTheta;
        }
        return result;
    }

    public float[] getVerticesOnMiddleEdges(float cos, float sin) {
        float[] result = new float[12];
        for (int i = 0; i < 12; i += 2) {
            result[i] = s_verticesOnMiddleEdges[i] * cos + s_verticesOnMiddleEdges[i + 1] * sin;
            result[i + 1] = s_verticesOnMiddleEdges[i] * -sin + s_verticesOnMiddleEdges[i + 1] * cos;
        }
        return result;
    }

/*
    public float[] getVerticesInWorld() {
        return getVerticesFromPoint(m_positionInWorld);
    }

    public float[] getVerticesFromPoint(Vector2 point) {
        return getVerticesFromPoint(point, m_sideLength);
    }

    public float[] getVerticesFromPoint(Vector2 point , float sideLen) {
        for (int i = 0; i < 12; ++i) {
            if (i % 2 == 0) { // x
                m_verticesInDimension[i] =
                        point.x+(s_vertices[i] * sideLen) * m_cosTheta +
                                (s_vertices[i + 1] * sideLen) * m_sinTheta;
            } else { // y
                m_verticesInDimension[i] =
                        point.y+(s_vertices[i - 1] * sideLen) * -m_sinTheta +
                                (s_vertices[i] * sideLen) * m_cosTheta;
            }
        }
        return m_verticesInDimension;
    }
*/
    public enum HexagonSide {bottomRight, bottom, bottomLeft, topLeft, top, topRight}

}