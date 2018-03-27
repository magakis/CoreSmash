package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class TilemapManager {
    private Tilemap tm;

    private boolean isRotating = true;
    private float rotationDegrees;
    private float rotationSpeed = 20;

    public TilemapManager(Tilemap map) {
        tm = map;
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

    public void checkForCollision(List<MovingTile> list) {
        TilemapTile collidedTile;
        float minDist;
        int sideHalf = tm.getSideLength()/2;
        Vector2 movhexPos;
        Vector2 direction;
        TilemapTile[][] m_hexTiles = tm.getTilemapTiles();


        for (MovingTile movhex : list){
            collidedTile = null;
            movhexPos = movhex.getPositionInWorld();
            minDist =  sideHalf + sideHalf * movhex.getScale();


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

            Tile.Side test = getClosestSide(collidedTile, direction);

            addTile(m_hexTiles, new TilemapTile(movhex.getColor()), collidedTile, test, tm.getSize()/2);
            movhex.dispose();
        }
    }

    public void addTile(TilemapTile[][] m_hexTiles, TilemapTile newHex, TilemapTile tile, Tile.Side side, int centerTile) {

        int xOffset;
        Vector2 tilePos;

        switch (side) {
            case top:
                tilePos = tile.getPositionInTilemap();
                if (m_hexTiles[(int) tilePos.y + centerTile + 2][(int) tilePos.x + centerTile] == null) {
                    newHex.setPositionInTilemap(tilePos.x, tilePos.y + 2);
                    m_hexTiles[(int) tilePos.y + centerTile + 2][(int) tilePos.x + centerTile] = newHex;
                }
                break;
            case topLeft:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y + 1);
                    m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
            case topRight:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y + 1);
                    m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
            case bottom:
                tilePos = tile.getPositionInTilemap();
                if (m_hexTiles[(int) tilePos.y + centerTile - 2][(int) tilePos.x + centerTile] == null) {
                    newHex.setPositionInTilemap(tilePos.x, tilePos.y - 2);
                    m_hexTiles[(int) tilePos.y + centerTile - 2][(int) tilePos.x + centerTile] = newHex;
                }
                break;
            case bottomLeft:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y - 1);
                    m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
            case bottomRight:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y - 1);
                    m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
        }
        tm.updateTilemapTile(newHex,tile.getCosTheta(), tile.getSinTheta());
        checkForColorMatches(m_hexTiles, newHex);
    }

    public void checkForColorMatches(TilemapTile[][] m_hexTiles, TilemapTile tile) {
        ArrayList<TilemapTile> match = new ArrayList<TilemapTile>();
        ArrayList<TilemapTile> exclude = new ArrayList<TilemapTile>();

        int centerTile = tm.getSize()/2;
        match.add(tile);
        exclude.add(tile);

        addSurroundingColorMatches(m_hexTiles,tile, match, exclude);

        if (match.size() < 3) return;

        for (TilemapTile t : match) {
            m_hexTiles[(int) t.getPositionInTilemap().y +centerTile][(int) t.getPositionInTilemap().x+centerTile] = null;
        }
    }

    //FIXME: Somehow I matched three on the outside and the middle tile got removed??!?
    //XXX: HORRIBLE CODE! DON'T READ OR YOUR BRAIN MIGHT CRASH! Blehh..
    public void addSurroundingColorMatches(TilemapTile[][] m_hexTiles, TilemapTile tile, List<TilemapTile> match, List<TilemapTile> exclude) {
        boolean isLeft = tile.getPositionInTilemap().y %2 == 0? true: false;
        int tx = (int) tile.getPositionInTilemap().x;
        int ty = (int) tile.getPositionInTilemap().y;
        int centerTile = tm.getSize()/2;

        TilemapTile tt;
        boolean flag = true;
        int x;

        for(int y = -2; y < 3; ++y) {
            if (y == 0) continue;
            if ((flag) && (y == -1 || y == +1)) {
                x = isLeft ? -1 : 1;
            } else {
                x = 0;
            }
            tt = m_hexTiles[ty+y+centerTile][tx+x+centerTile];

            if (flag && y == 1) {
                flag = false;
                y = -2;
            }

            if (tt == null || exclude.contains(tt))
                continue;

            exclude.add(tt);

            if(tt.getColor() == tile.getColor()) {
                match.add(tt);
                addSurroundingColorMatches(m_hexTiles,tt,match,exclude);
            }
        }
    }

    public Tile.Side getClosestSide(Tile hex, Vector2 point) {
        float[] vertices = hex.getVerticesOnMiddleEdges();

        Vector2 hPos = hex.getPositionInWorld();
        float sl = tm.getSideLength();

        float topLeft = Vector2.dst(vertices[0], vertices[1], point.x, point.y);
        float top = Vector2.dst(vertices[2], vertices[3], point.x, point.y);
        float topRight = Vector2.dst(vertices[4], point.x, vertices[5], point.y);
        float bottomRight = Vector2.dst(vertices[6], vertices[7], point.x, point.y);
        float bottom = Vector2.dst(vertices[8], vertices[9], point.x, point.y);
        float bottomLeft = Vector2.dst(vertices[10], vertices[11], point.x, point.y);

        float closestSideLength = bottomRight;
        Tile.Side closestSide = Tile.Side.bottomRight;

        if (top < closestSideLength) {
            closestSideLength = top;
            closestSide = Tile.Side.top;
        }
        if (bottomLeft < closestSideLength) {
            closestSideLength = bottomLeft;
            closestSide = Tile.Side.bottomLeft;
        }
        if (topLeft < closestSideLength) {
            closestSideLength = topLeft;
            closestSide = Tile.Side.topLeft;
        }
        if (bottom < closestSideLength) {
            closestSideLength = bottom;
            closestSide = Tile.Side.bottom;
        }
        if (topRight < closestSideLength) {
            closestSideLength = topRight;
            closestSide = Tile.Side.topRight;
        }

        return closestSide;
    }
}
