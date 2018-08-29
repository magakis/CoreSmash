package com.archapp.coresmash;

import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.Matchable;
import com.archapp.coresmash.tiles.TileContainer.Side;

import java.util.ArrayList;

public class Match3 {
    private ArrayList<TilemapTile> matched = new ArrayList<>();
    private ArrayList<TilemapTile> closed = new ArrayList<>();

    public ArrayList<TilemapTile> getColorMatchesFromTile(TilemapTile tile) {
        matched.clear();
        closed.clear();
        addSurroundingColorMatches(tile, tile.getTileID());
        return matched;
    }

    private void addSurroundingColorMatches(TilemapTile tmTile, int id) {
        closed.add(tmTile);
        matched.add(tmTile);

        TilemapTile neighbour;
        //top_left
        neighbour = tmTile.getNeighbour(Side.TOP_LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(id, neighbour)) {
                addSurroundingColorMatches(neighbour, id);
            }
        }

        //top_right
        neighbour = tmTile.getNeighbour(Side.TOP_RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(id, neighbour)) {
                addSurroundingColorMatches(neighbour, id);
            }
        }

        //right
        neighbour = tmTile.getNeighbour(Side.RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(id, neighbour)) {
                addSurroundingColorMatches(neighbour, id);
            }
        }

        //bottom_right
        neighbour = tmTile.getNeighbour(Side.BOTTOM_RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(id, neighbour)) {
                addSurroundingColorMatches(neighbour, id);
            }
        }

        //bottom_left
        neighbour = tmTile.getNeighbour(Side.BOTTOM_LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(id, neighbour)) {
                addSurroundingColorMatches(neighbour, id);
            }
        }

        //left
        neighbour = tmTile.getNeighbour(Side.LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (isMatching(id, neighbour)) {
                addSurroundingColorMatches(neighbour, id);
            }
        }
    }

    private boolean isMatching(int id, TilemapTile neighbour) {
        return neighbour.getTile() instanceof Matchable && ((Matchable) neighbour.getTile()).matchesWith(id);
    }
}
