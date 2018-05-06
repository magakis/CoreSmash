package com.breakthecore;

import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapTile;

import java.util.ArrayList;
import java.util.List;

public class Match3 {
    private ArrayList<TilemapTile> matched = new ArrayList<TilemapTile>();
    private ArrayList<TilemapTile> closed = new ArrayList<TilemapTile>();

    public ArrayList<TilemapTile> getColorMatchesFromTile(TilemapTile tile, Tilemap tm) {
        matched.clear();
        closed.clear();
        addSurroundingColorMatches(tm, tile);
        return matched;
    }

    private void addSurroundingColorMatches(Tilemap tm, TilemapTile tile) {
        Coords2D tpos = tile.getRelativePosition();
        int tx = tpos.x;
        int ty = tpos.y;

        TilemapTile tt;

        matched.add(tile);
        closed.add(tile);

        //top_left
        tt = tm.getRelativeTile(tx - 1, ty + 1);
        if (tt != null && !closed.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt);
            }
        }

        //top_right
        tt = tm.getRelativeTile(tx, ty + 1);
        if (tt != null && !closed.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt);
            }
        }

        //right
        tt = tm.getRelativeTile(tx + 1, ty);
        if (tt != null && !closed.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt);
            }
        }

        //bottom_right
        tt = tm.getRelativeTile(tx + 1, ty - 1);
        if (tt != null && !closed.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt);
            }
        }

        //bottom_left
        tt = tm.getRelativeTile(tx, ty - 1);
        if (tt != null && !closed.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt);
            }
        }

        //left
        tt = tm.getRelativeTile(tx - 1, ty);
        if (tt != null && !closed.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt);
            }
        }
    }

}
