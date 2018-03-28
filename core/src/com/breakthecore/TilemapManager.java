package com.breakthecore;

import com.badlogic.gdx.math.Vector2;
import com.sun.org.apache.xml.internal.utils.IntVector;

import java.util.ArrayList;
import java.util.List;

public class TilemapManager {
    private Tilemap tm;

    private int tmpScore;
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

            Tile.Side test = getClosestSide(collidedTile, direction);

            addTile(new TilemapTile(movhex.getColor()), collidedTile, test, tm.getSize() / 2);
            movhex.dispose();
        }
    }

    public void addTile(TilemapTile newTile, TilemapTile solidTile, Tile.Side side, int centerTile) {
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
        }
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

    public Tile.Side getClosestSide(Tile hex, Vector2 point) {
        float[] vertices = hex.getVerticesOnMiddleEdges();

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

    public int getTmpScore() {
        return tmpScore;
    }
}
