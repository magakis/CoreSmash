package com.breakthecore;

import com.badlogic.gdx.math.Vector2;
import com.breakthecore.tiles.TilemapTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Pathfinder {
    ArrayList<TilemapTile> exclude;
    LinkedList<TilemapTile> active;
    ArrayList<TilemapTile> dead;
    Comparator<TilemapTile> tileValueComp;

    // TODO: 3/28/2018 Don't forget to clean those two lists after you are done
    public Pathfinder() {
        exclude = new ArrayList<TilemapTile>();
        active = new LinkedList<TilemapTile>();
        dead = new ArrayList<TilemapTile>();
        tileValueComp = new Comparator<TilemapTile>() {
            @Override
            public int compare(TilemapTile o1, TilemapTile o2) {
                return o1.getDistanceFromCenter() < o2.getDistanceFromCenter() ? -1 : 1;
            }
        };
    }

    public ArrayList<TilemapTile> getDeadTiles(Tilemap tm) {
        dead.clear();
        TilemapTile[][] map = tm.getTilemapTiles();
        for (TilemapTile[] arr : map) {
            for (TilemapTile t : arr) {
                if (t != null) {
                    if (!formPathToCenter(t, tm)) {
                        dead.add(t);
                    }
                }
            }
        }
        return dead;
    }

    public boolean formPathToCenter(TilemapTile tt, Tilemap tm) {
        TilemapTile curr = tt;
        active.clear();
        exclude.clear();

        active.add(tt);
        while (true) {
            if (curr.getDistanceFromCenter() == 0) return true;
            getSurroundingTiles(curr, tm, active, exclude);
            if (active.size() == 0) return false;
            Collections.sort(active, tileValueComp);
            curr = active.getFirst();
            exclude.add(curr);
            active.remove(curr);
        }
    }

    private void getSurroundingTiles(TilemapTile tmt, Tilemap tm, LinkedList<TilemapTile> active, ArrayList<TilemapTile> exclude) {
        Vector2 tpos = tmt.getPositionInTilemap();
        int tx = (int) tpos.x;
        int ty = (int) tpos.y;

        TilemapTile tt;

        //top_left
        tt = tm.getTile(tx - 1, ty + 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //top_right
        tt = tm.getTile(tx, ty + 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //right
        tt = tm.getTile(tx + 1, ty);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //bottom_right
        tt = tm.getTile(tx + 1, ty - 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }


        //bottom_left
        tt = tm.getTile(tx, ty - 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //left
        tt = tm.getTile(tx - 1, ty);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

    }
}
