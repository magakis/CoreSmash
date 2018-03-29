package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Michail on 18/3/2018.
 */

public class Tile extends Observable {
    private static final float[] s_vertices = generateVertices(30);
    private static final float[] s_verticesOnMiddleEdges = generateVertices(0);

    protected Vector2 m_positionInWorld;
    private int m_color;

    private float m_cosTheta;
    private float m_sinTheta;


    public Tile () {
        m_positionInWorld = new Vector2();
        m_cosTheta = 1;
        m_sinTheta = 0;
        m_color = 0;
    }

    public Tile(int color) {
        this();
        this.m_color = color;
    }

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

    public int getColor() {return m_color;}

    public void setColor(int colorId) {
        m_color = colorId;
    }

    public float getCosTheta() {
        return m_cosTheta;
    }

    public float getSinTheta() {
        return m_sinTheta;
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

    public void setPositionInWorld(float x, float y) {
        m_positionInWorld.set(x, y);
    }

    public void setRotation(float cosTheta, float sinTheta) {
        m_cosTheta = cosTheta;
        m_sinTheta = sinTheta;
    }

    public enum Side {bottomRight, bottom, bottomLeft, topLeft, top, topRight}
}