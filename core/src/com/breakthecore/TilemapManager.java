package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TilemapManager {
    private Tilemap tm;

    private int tmpScore;
    private boolean isRotating = true;
    private float rotationDegrees;
    private float rotationSpeed = 20;

    private ArrayList<DistanceSideStruct> m_collisionDisSide;
    private Comparator<DistanceSideStruct> m_disSideComp;

    public TilemapManager(Tilemap map) {
        tm = map;
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
    }

    public void update(float delta) {
        if (isRotating) {
            rotationDegrees += rotationSpeed * delta;
        }
        float rotRad = (float) Math.toRadians(rotationDegrees);

        float cosX = (float) Math.cos(rotRad);
        float sineX = (float) Math.sin(rotRad);

        tm.setRotation(cosX, sineX);
    }

    // TODO: 3/28/2018 Handle cases where a tile actually exist on that side
    public void checkForCollision(List<MovingTile> list) {
        TilemapTile collidedTile;
        float minDist;
        int sideHalf = tm.getSideLength() / 2;
        Vector2 movhexPos;
        Vector2 direction;
        TilemapTile[][] m_hexTiles = tm.getTilemapTiles();


        for (MovingTile movhex : list) {
            collidedTile = null;
            movhexPos = movhex.getPositionInWorld();
            minDist = sideHalf + sideHalf * movhex.getScale();


            collisionSearch:
            for (TilemapTile[] arr : m_hexTiles) {
                for (TilemapTile tile : arr) {
                    if (tile != null) {
                        if (movhexPos.dst(tile.getPositionInWorld()) < minDist) {
                            collidedTile = tile;
                            break collisionSearch;
                        }
                    }
                }
            }

            if (collidedTile == null)
                continue;

            direction = new Vector2(movhexPos).sub(collidedTile.getPositionInWorld());
            direction.nor();

            getClosestSides(collidedTile, direction);

            for (int i = 0; i < 6; ++i) {
                if (addTile(new TilemapTile(movhex.getColor()), collidedTile, m_collisionDisSide.get(i).side, tm.getSize() / 2)) {
                    break;
                }
            }
            movhex.dispose();
        }
    }

    public boolean addTile(TilemapTile newTile, TilemapTile solidTile, Tile.Side side, int centerTile) {
        boolean placedNewTile = false;
        Vector2 tilePos = solidTile.getPositionInTilemap();
        int xOffset;
        switch (side) {
            case top:
                if (tm.getTile((int) tilePos.x, (int) tilePos.y + 2) == null) {
                    newTile.setPositionInTilemap(tilePos.x, tilePos.y + 2);
                    tm.setTile((int) tilePos.x, (int) tilePos.y + 2, newTile);
                    placedNewTile = true;
                }
                break;
            case topLeft:
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y + 1) == null) {
                    newTile.setPositionInTilemap(tilePos.x + xOffset, tilePos.y + 1);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case topRight:
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y + 1) == null) {
                    newTile.setPositionInTilemap(tilePos.x + xOffset, tilePos.y + 1);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case bottom:
                if (tm.getTile((int) tilePos.x, (int) tilePos.y - 2) == null) {
                    newTile.setPositionInTilemap(tilePos.x, tilePos.y - 2);
                    tm.setTile((int) tilePos.x, (int) tilePos.y - 2, newTile);
                    placedNewTile = true;
                }
                break;
            case bottomLeft:
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y - 1) == null) {
                    newTile.setPositionInTilemap(tilePos.x + xOffset, tilePos.y - 1);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
            case bottomRight:
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y - 1) == null) {
                    newTile.setPositionInTilemap(tilePos.x + xOffset, tilePos.y - 1);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
        }
        if (placedNewTile) {
            tm.updateTilemapTile(newTile);
            checkForColorMatches(newTile);
            return true;
        }
        return false;
    }

    public void checkForColorMatches(TilemapTile tile) {
        ArrayList<TilemapTile> match = new ArrayList<TilemapTile>();
        ArrayList<TilemapTile> exclude = new ArrayList<TilemapTile>();

        addSurroundingColorMatches(tile, match, exclude);

        if (match.size() < 3) {
            if (match.size() == 1) {
                --tmpScore;
            }
            return;
        }

        for (TilemapTile t : match) {
            if (t.getPositionInTilemap().y == 0 && t.getPositionInTilemap().x == 0)
                tm.setTile(-9999, -9999, null); //CRASH!!
            tm.setTile((int) t.getPositionInTilemap().x, (int) t.getPositionInTilemap().y, null);
        }
        tmpScore += match.size();
    }

    //FIXME: Somehow I matched three on the outside and the middle tile got removed??!?
    //XXX: HORRIBLE CODE! DON'T READ OR YOUR BRAIN MIGHT CRASH! Blehh..
    public void addSurroundingColorMatches(TilemapTile tile, List<TilemapTile> match, List<TilemapTile> exclude) {
        boolean isLeft = tile.getPositionInTilemap().y % 2 == 0 ? true : false;
        int tx = (int) tile.getPositionInTilemap().x;
        int ty = (int) tile.getPositionInTilemap().y;

        TilemapTile tt;
        boolean flag = true;
        int x;

        match.add(tile);
        exclude.add(tile);


        for (int y = -2; y < 3; ++y) {
            if (y == 0) continue;
            if ((flag) && (y == -1 || y == +1)) {
                x = isLeft ? -1 : 1;
            } else {
                x = 0;
            }
            tt = tm.getTile(tx + x, ty + y);

            if (flag && y == 1) {
                flag = false;
                y = -2;
            }

            if (tt == null || exclude.contains(tt))
                continue;

            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }
    }

    public void getClosestSides(Tile hex, Vector2 point) {
        float[] vertices = hex.getVerticesOnMiddleEdges();
        DistanceSideStruct curr;

        float topLeft = Vector2.dst(vertices[0], vertices[1], point.x, point.y);
        curr = m_collisionDisSide.get(0);
        curr.distance = topLeft;
        curr.side = Tile.Side.topLeft;

        float top = Vector2.dst(vertices[2], vertices[3], point.x, point.y);
        curr = m_collisionDisSide.get(1);
        curr.distance = top;
        curr.side = Tile.Side.top;

        float topRight = Vector2.dst(vertices[4], point.x, vertices[5], point.y);
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
    }

    public int getTmpScore() {
        return tmpScore;
    }

    private class DistanceSideStruct {
        public float distance;
        public Tile.Side side;
    }
}
