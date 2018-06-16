package com.breakthecore;

import com.breakthecore.tilemap.TilemapTile;
import com.breakthecore.tiles.TileContainer.Side;

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
        matched.add(tmTile);
        closed.add(tmTile);

        TilemapTile neighbour;
        //top_left
        neighbour = tmTile.getNeighbour(Side.TOP_LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (neighbour.getTileID() == tmTile.getTileID()) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //top_right
        neighbour = tmTile.getNeighbour(Side.TOP_RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (neighbour.getTileID() == tmTile.getTileID()) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //right
        neighbour = tmTile.getNeighbour(Side.RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (neighbour.getTileID() == tmTile.getTileID()) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //bottom_right
        neighbour = tmTile.getNeighbour(Side.BOTTOM_RIGHT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (neighbour.getTileID() == tmTile.getTileID()) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //bottom_left
        neighbour = tmTile.getNeighbour(Side.BOTTOM_LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (neighbour.getTileID() == tmTile.getTileID()) {
                addSurroundingColorMatches(neighbour);
            }
        }

        //left
        neighbour = tmTile.getNeighbour(Side.LEFT);
        if (neighbour != null && !closed.contains(neighbour)) {
            if (neighbour.getTileID() == tmTile.getTileID()) {
                addSurroundingColorMatches(neighbour);
            }
        }
    }

}
