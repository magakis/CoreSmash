package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TilemapManager {
    private Tilemap tm;
    private CollisionManager m_collisionManager;

    private int tmpScore;
    private boolean isRotating = true;
    private float rotationDegrees = 0;
    private float rotationSpeed = 20;

    private Vector2 direction;

    public TilemapManager(Tilemap map) {
        tm = map;
        m_collisionManager = new CollisionManager();
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

    public Vector2 getDBDirection() {
        return direction;
    }
    // TODO: 3/28/2018 Handle cases where a tile actually exist on that side
    public void checkForCollision(List<MovingTile> list) {
        TilemapTile solidTile;

        for (MovingTile mt : list) {
            solidTile = m_collisionManager.findCollision(tm, mt);
            if (solidTile == null) continue;

            direction = new Vector2(mt.m_positionInWorld).sub(solidTile.getPositionInWorld());
            direction.nor();

            addTile(mt.getColor(), solidTile, m_collisionManager.getClosestSides(solidTile, direction));

            mt.dispose();
        }

    }

    public boolean addTile(int color, TilemapTile solidTile, Tile.Side[] sides) {
        for (int i = 0; i < 6; ++i) {
            if (addTile(color, solidTile, sides[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean addTile(int color, TilemapTile solidTile, Tile.Side side) {
        TilemapTile newTile = null;
        boolean placedNewTile = false;
        Vector2 tilePos = solidTile.getPositionInTilemap();
        int xOffset;
        switch (side) {
            case top:
                if (tm.getTile((int) tilePos.x, (int) tilePos.y + 2) == null) {
                    newTile = new TilemapTile(color);
                    tm.setTile((int) tilePos.x, (int) tilePos.y + 2, newTile);
                    placedNewTile = true;
                }
                break;
            case topLeft:
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y + 1) == null) {
                    newTile = new TilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case topRight:
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y + 1) == null) {
                    newTile = new TilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case bottom:
                if (tm.getTile((int) tilePos.x, (int) tilePos.y - 2) == null) {
                    newTile = new TilemapTile(color);
                    tm.setTile((int) tilePos.x, (int) tilePos.y - 2, newTile);
                    placedNewTile = true;
                }
                break;
            case bottomLeft:
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y - 1) == null) {
                    newTile = new TilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
            case bottomRight:
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y - 1) == null) {
                    newTile = new TilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
        }
        if (placedNewTile) {
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

    public int getTmpScore() {
        return tmpScore;
    }

}
