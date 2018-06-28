package com.breakthecore;

import com.breakthecore.tilemap.TilemapTile;
import com.breakthecore.tiles.Matchable;
import com.breakthecore.tiles.TileContainer.Side;
import com.breakthecore.tiles.TileType;

import java.util.ArrayList;

public class Match3 {
    private ArrayList<TilemapTile> matched = new ArrayList<TilemapTile>();
    private ArrayList<TilemapTile> closed = new ArrayList<TilemapTile>();

    public ArrayList<TilemapTile> getColorMatchesFromTile(TilemapTile tile) {
        matched.clear();
        closed.clear();
        addSurroundingColorMatches(tile);
        return matched;
    }

    private void addSurroundingColorMatches(TilemapTile tmTile) {
        closed.add(tmTile);
        boolean isMatchable = false;
        if (tmTile.getTile().getAttributes().getTileType() != TileType.REGULAR) {
            if (tmTile.getTile() instanceof Matchable) {
                isMatchable = true;
            } else {
                return;
            }
        }
        matched.add(tmTile);

        TilemapTile neighbour;
        //top_left
        neighbour = tmTile.getNeighbour(Side.TOP_LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(isMatchable, tmTile, neighbour)) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //top_right
        neighbour = tmTile.getNeighbour(Side.TOP_RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(isMatchable, tmTile, neighbour)) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //right
        neighbour = tmTile.getNeighbour(Side.RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(isMatchable, tmTile, neighbour)) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //bottom_right
        neighbour = tmTile.getNeighbour(Side.BOTTOM_RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(isMatchable, tmTile, neighbour)) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //bottom_left
        neighbour = tmTile.getNeighbour(Side.BOTTOM_LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(isMatchable, tmTile, neighbour)) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //left
        neighbour = tmTile.getNeighbour(Side.LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(isMatchable, tmTile, neighbour)) {
                addSurroundingColorMatches(neighbour);
            }
        }
    }

    private boolean isMatching(boolean isMatchable, TilemapTile tmTile, TilemapTile neighbour) {
        if (neighbour instanceof Matchable) {
            if (isMatchable) {
                if (((Matchable) neighbour.getTile()).matchesWith(tmTile.getTileID()) &&
                        ((Matchable) tmTile.getTile()).matchesWith(neighbour.getTileID())) {
                    return true;
                }
            } else {
                if (((Matchable) neighbour.getTile()).matchesWith(tmTile.getTileID())) {
                    return true;
                }
            }
        } else {
            if (isMatchable) {
                if (((Matchable) tmTile.getTile()).matchesWith(neighbour.getTileID())) {
                    return true;
                }
            } else {
                /* It is impossible for some tile that can't be matched to pass this test cause the
                 * methods addSurroundingColorMatches() checks first whether the subject Tile can be
                 * matched at all. If a neighbour is found that can't be matched, it is
                 * guaranteed to fail on the following test.
                 */
                if (tmTile.getTileID() == neighbour.getTileID()) {
                    return true;
                }
            }
        }
        return false;
    }
}
