package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CollisionManager {
    private ArrayList<DistanceSideStruct> m_collisionDisSide;
    private Tile.Side[] m_closestSidesOutput;
    private Comparator<DistanceSideStruct> m_disSideComp;

    public CollisionManager() {
        m_collisionDisSide = new ArrayList<DistanceSideStruct>(6);
        for (int i = 0; i < 6; ++i) {
            m_collisionDisSide.add(new DistanceSideStruct());
        }

        m_disSideComp = new Comparator<DistanceSideStruct>() {
            @Override
            public int compare(DistanceSideStruct o1, DistanceSideStruct o2) {
                return o1.distance > o2.distance ? 1 : -1;
            }
        };

        m_closestSidesOutput = new Tile.Side[6];
    }

    public TilemapTile findCollision(Tilemap tm, MovingTile moveTile) {
        float minDist;
        int sideHalf = tm.getSideLength() / 2;
        Vector2 movhexPos;
        TilemapTile[][] m_hexTiles = tm.getTilemapTiles();

        movhexPos = moveTile.getPositionInWorld();

        //HACK: Arbitary value to decrease range and match better the texture
        minDist = sideHalf + sideHalf * moveTile.getScale() * 0.8f;

        for (TilemapTile[] arr : m_hexTiles) {
            for (TilemapTile tile : arr) {
                if (tile != null) {
                    if (movhexPos.dst(tile.getPositionInWorld()) < minDist) {
                        return tile;
                    }
                }
            }
        }
        return null;
    }

    public Tile.Side[] getClosestSides(Tile solidTile, Vector2 point) {
        float[] vertices = solidTile.getVerticesOnMiddleEdges();
        DistanceSideStruct curr;

        float topLeft = Vector2.dst(vertices[0], vertices[1], point.x, point.y);
        curr = m_collisionDisSide.get(0);
        curr.distance = topLeft;
        curr.side = Tile.Side.topLeft;

        float top = Vector2.dst(vertices[2], vertices[3], point.x, point.y);
        curr = m_collisionDisSide.get(1);
        curr.distance = top;
        curr.side = Tile.Side.top;

        float topRight = Vector2.dst(vertices[4], vertices[5], point.x, point.y);
        curr = m_collisionDisSide.get(2);
        curr.distance = topRight;
        curr.side = Tile.Side.topRight;

        float bottomRight = Vector2.dst(vertices[6], vertices[7], point.x, point.y);
        curr = m_collisionDisSide.get(3);
        curr.distance = bottomRight;
        curr.side = Tile.Side.bottomRight;

        float bottom = Vector2.dst(vertices[8], vertices[9], point.x, point.y);
        curr = m_collisionDisSide.get(4);
        curr.distance = bottom;
        curr.side = Tile.Side.bottom;

        float bottomLeft = Vector2.dst(vertices[10], vertices[11], point.x, point.y);
        curr = m_collisionDisSide.get(5);
        curr.distance = bottomLeft;
        curr.side = Tile.Side.bottomLeft;

        Collections.sort(m_collisionDisSide, m_disSideComp);

        for (int i = 0; i < 6; ++i) {
            m_closestSidesOutput[i] = m_collisionDisSide.get(i).side;
        }

        return m_closestSidesOutput;
    }

    private class DistanceSideStruct {
        public float distance;
        public Tile.Side side;
    }
}
