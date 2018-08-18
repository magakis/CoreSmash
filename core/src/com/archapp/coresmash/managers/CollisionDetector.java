package com.archapp.coresmash.managers;

import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.tilemap.Tilemap;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.MovingBall;
import com.archapp.coresmash.tiles.TileContainer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollisionDetector {
    private ArrayList<DistanceSideStruct> m_collisionDisSide;
    private TileContainer.Side[] m_closestSidesOutput;
    private Comparator<DistanceSideStruct> m_disSideComp;
    private Vector2 distance;
    private Vector2 direction;

    public CollisionDetector() {
        direction = new Vector2();
        distance = new Vector2();
        m_collisionDisSide = new ArrayList<>(6);
        for (int i = 0; i < 6; ++i) {
            m_collisionDisSide.add(new DistanceSideStruct());
        }

        m_disSideComp = new Comparator<DistanceSideStruct>() {
            @Override
            public int compare(DistanceSideStruct o1, DistanceSideStruct o2) {
                return Float.compare(o1.distance, o2.distance);
            }
        };

        m_closestSidesOutput = new TileContainer.Side[6];
    }

    public TilemapTile findCollision(List<TilemapTile> tilemapTileList, MovingBall ball) {
        int sideHalf = WorldSettings.getTileSize() / 2;
        //XXX(HACK): Arbitrary value to decrease range and match better the texture
        float minDist = sideHalf + sideHalf * ball.getScale() * 0.8f;

        for (TilemapTile tmTile : tilemapTileList) {
            distance.set(tmTile.getPositionInWorld()).sub(ball.getPositionInWorld());
            if (Math.abs(distance.x) <= minDist && Math.abs(distance.y) <= minDist) {
                return tmTile;
            }
        }
        return null;
    }

    public TilemapTile findCollision(Tilemap tm, MovingBall moveTile) {
        int sideHalf = WorldSettings.getTileSize() / 2;
        //XXX(HACK): Arbitrary value to decrease range and match better the texture
        float minDist = sideHalf + sideHalf * moveTile.getScale() * 0.8f;

        Vector2 movhexPos = moveTile.getPositionInWorld();


        for (TilemapTile tile : tm.getTileList()) {
            if (movhexPos.dst(tile.getPositionInWorld()) < minDist) {
                return tile;
            }
        }
        return null;
    }

    public TileContainer.Side[] getClosestSides(float rotation, Vector2 point) {

        float[] vertices = getVerticesOnMiddleEdges(rotation);
        DistanceSideStruct curr;

        float topLeft = Vector2.dst(vertices[0], vertices[1], point.x, point.y);
        curr = m_collisionDisSide.get(0);
        curr.distance = topLeft;
        curr.side = TileContainer.Side.TOP_LEFT;

        float top = Vector2.dst(vertices[2], vertices[3], point.x, point.y);
        curr = m_collisionDisSide.get(1);
        curr.distance = top;
        curr.side = TileContainer.Side.TOP_RIGHT;

        float topRight = Vector2.dst(vertices[4], vertices[5], point.x, point.y);
        curr = m_collisionDisSide.get(2);
        curr.distance = topRight;
        curr.side = TileContainer.Side.RIGHT;

        float bottomRight = Vector2.dst(vertices[6], vertices[7], point.x, point.y);
        curr = m_collisionDisSide.get(3);
        curr.distance = bottomRight;
        curr.side = TileContainer.Side.BOTTOM_RIGHT;

        float bottom = Vector2.dst(vertices[8], vertices[9], point.x, point.y);
        curr = m_collisionDisSide.get(4);
        curr.distance = bottom;
        curr.side = TileContainer.Side.BOTTOM_LEFT;

        float bottomLeft = Vector2.dst(vertices[10], vertices[11], point.x, point.y);
        curr = m_collisionDisSide.get(5);
        curr.distance = bottomLeft;
        curr.side = TileContainer.Side.LEFT;

        Collections.sort(m_collisionDisSide, m_disSideComp);

        for (int i = 0; i < 6; ++i) {
            m_closestSidesOutput[i] = m_collisionDisSide.get(i).side;
        }

        return m_closestSidesOutput;
    }

    public float[] getVerticesOnMiddleEdges(float rotRad) {
        float cosT = (float) Math.cos(rotRad);
        float sinT = (float) Math.sin(rotRad);

        float[] result = new float[12];
        for (int i = 0; i < 12; i += 2) {
            result[i] = TileContainer.s_vertices[i] * cosT + TileContainer.s_vertices[i + 1] * sinT;
            result[i + 1] = TileContainer.s_vertices[i] * -sinT + TileContainer.s_vertices[i + 1] * cosT;
        }
        return result;
    }

    public Vector2 getDirection(Vector2 pos, Vector2 origin) {
        direction.set(pos).sub(origin);
        direction.nor();
        return direction;
    }

    private class DistanceSideStruct {
        public float distance;
        public TileContainer.Side side;
    }
}
