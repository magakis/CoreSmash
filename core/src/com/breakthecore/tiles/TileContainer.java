package com.breakthecore.tiles;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Michail on 18/3/2018.
 */

public abstract class TileContainer {
    public static final float[] s_verticesOnMiddleEdges = generateVertices(0);
    public static final float[] s_vertices = generateVertices(30);
    private Tile tile;

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

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public boolean hasTile() {return tile != null;}

    public int getTileID() {
        if (tile == null) throw new NullPointerException();
        return tile.getID();
    }

    public static Side getOppositeSide(Side side) {
        switch (side) {
            case BOTTOM_RIGHT:
                return Side.TOP_LEFT;
            case BOTTOM_LEFT:
                return Side.TOP_RIGHT;
            case LEFT:
                return Side.RIGHT;
            case TOP_LEFT:
                return Side.BOTTOM_RIGHT;
            case TOP_RIGHT:
                return Side.BOTTOM_LEFT;
            case RIGHT:
                return Side.LEFT;
            default:
                throw new RuntimeException("Wrong side?(" + side + ")");
        }
    }

    public enum Side {BOTTOM_RIGHT, BOTTOM_LEFT, LEFT, TOP_LEFT, TOP_RIGHT, RIGHT}

}