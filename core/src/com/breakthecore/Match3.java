package com.breakthecore;

import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapTile;

import java.util.ArrayList;
import java.util.List;

// TODO(21/4/2018): GET RID OF ALL THE LOGIC OF WHAT HAPPENS WHEN WHAT FROM THIS CLASS!
public class Match3 {
    private ArrayList<TilemapTile> match = new ArrayList<TilemapTile>();
    private ArrayList<TilemapTile> exclude = new ArrayList<TilemapTile>();

    public ArrayList<TilemapTile> getColorMatchesFromTile(TilemapTile tile, Tilemap tm) {
        match.clear();
        exclude.clear();
        addSurroundingColorMatches(tm, tile, match, exclude);
        return match;
    }

    private void addSurroundingColorMatches(Tilemap tm, TilemapTile tile, List<TilemapTile> match, List<TilemapTile> exclude) {
        Coords2D tpos = tile.getRelativePosition();
        int tx = tpos.x;
        int ty = tpos.y;

        TilemapTile tt;

        match.add(tile);
        exclude.add(tile);

        //top_left
        tt = tm.getRelativeTile(tx - 1, ty + 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //top_right
        tt = tm.getRelativeTile(tx, ty + 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //right
        tt = tm.getRelativeTile(tx + 1, ty);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //bottom_right
        tt = tm.getRelativeTile(tx + 1, ty - 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //bottom_left
        tt = tm.getRelativeTile(tx, ty - 1);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }

        //left
        tt = tm.getRelativeTile(tx - 1, ty);
        if (tt != null && !exclude.contains(tt)) {
            if (tt.getTileID() == tile.getTileID()) {
                addSurroundingColorMatches(tm, tt, match, exclude);
            }
        }
    }

}
