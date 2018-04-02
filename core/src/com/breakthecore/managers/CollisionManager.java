package com.breakthecore.managers;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.Tilemap;
import com.breakthecore.tiles.MovingTile;
import com.breakthecore.tiles.TileContainer;
import com.breakthecore.tiles.TilemapTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollisionManager {
    private ArrayList<DistanceSideStruct> m_collisionDisSide;
    private TileContainer.Side[] m_closestSidesOutput;
    private Comparator<DistanceSideStruct> m_disSideComp;
    private Vector2 direction;

    public CollisionManager() {
        direction = new Vector2();
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

        m_closestSidesOutput = new TileContainer.Side[6];
    }

    public TilemapTile findCollision(Tilemap tm, MovingTile moveTile) {
        float minDist;
        int sideHalf = tm.getSideLength() / 2;
        Vector2 movhexPos;
        TilemapTile[][] m_hexTiles = tm.getTilemapTiles();

        movhexPos = moveTile.getPositionInWorld();

        //HACK: Arbitrary value to decrease range and match better the texture
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

    public TileContainer.Side[] getClosestSides(float cosT, float sinT, Vector2 point) {
        float[] vertices = getVerticesOnMiddleEdges(cosT, sinT);
        DistanceSideStruct curr;

        float topLeft = Vector2.dst(vertices[0], vertices[1], point.x, point.y);
        curr = m_collisionDisSide.get(0);
        curr.distance = topLeft;
        curr.side = TileContainer.Side.topLeft;

        float top = Vector2.dst(vertices[2], vertices[3], point.x, point.y);
        curr = m_collisionDisSide.get(1);
        curr.distance = top;
        curr.side = TileContainer.Side.top;

        float topRight = Vector2.dst(vertices[4], vertices[5], point.x, point.y);
        curr = m_collisionDisSide.get(2);
        curr.distance = topRight;
        curr.side = TileContainer.Side.topRight;

        float bottomRight = Vector2.dst(vertices[6], vertices[7], point.x, point.y);
        curr = m_collisionDisSide.get(3);
        curr.distance = bottomRight;
        curr.side = TileContainer.Side.bottomRight;

        float bottom = Vector2.dst(vertices[8], vertices[9], point.x, point.y);
        curr = m_collisionDisSide.get(4);
        curr.distance = bottom;
        curr.side = TileContainer.Side.bottom;

        float bottomLeft = Vector2.dst(vertices[10], vertices[11], point.x, point.y);
        curr = m_collisionDisSide.get(5);
        curr.distance = bottomLeft;
        curr.side = TileContainer.Side.bottomLeft;

        Collections.sort(m_collisionDisSide, m_disSideComp);

        for (int i = 0; i < 6; ++i) {
            m_closestSidesOutput[i] = m_collisionDisSide.get(i).side;
        }

        return m_closestSidesOutput;
    }

    public float[] getVerticesOnMiddleEdges(float cosT, float sinT) {
        float[] result = new float[12];
        for (int i = 0; i < 12; i += 2) {
            result[i] = TileContainer.s_verticesOnMiddleEdges[i] * cosT + TileContainer.s_verticesOnMiddleEdges[i + 1] * sinT;
            result[i + 1] = TileContainer.s_verticesOnMiddleEdges[i] * -sinT + TileContainer.s_verticesOnMiddleEdges[i + 1] * cosT;
        }
        return result;
    }

    public void checkForCollision(MovingTileManager mtm, TilemapManager tmm) {

        TilemapTile solidTile;
        Tilemap tm = tmm.getTileMap();
        List<MovingTile> list = mtm.getActiveList();
        for (MovingTile mt : list) {
            solidTile = findCollision(tm, mt);
            if (solidTile == null) continue;

            direction.set(mt.getPositionInWorld()).sub(solidTile.getPositionInWorld());
            direction.nor();

            tmm.addTile(mt.extractTile(), solidTile, getClosestSides(tm.getCosT(), tm.getSinT(), direction));

            mt.dispose();
        }
        mtm.disposeInactive();
    }


    private class DistanceSideStruct {
        public float distance;
        public TileContainer.Side side;
    }
}
