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

    private void getSurroundingTiles(TilemapTile tt, Tilemap tm, LinkedList<TilemapTile> active, ArrayList<TilemapTile> exclude) {
        Vector2 tpos = tt.getPositionInTilemap();
        boolean isLeft = tpos.y % 2 == 0 ? true : false;
        int tx = (int) tpos.x;
        int ty = (int) tpos.y;

        TilemapTile tmpt;
        boolean flag = true;
        int x;

        for (int y = -2; y < 3; ++y) {
            if (y == 0) continue;
            if ((flag) && (y == -1 || y == +1)) {
                x = isLeft ? -1 : 1;
            } else {
                x = 0;
            }
            tmpt = tm.getTile(tx + x, ty + y);

            if (flag && y == 1) {
                flag = false;
                y = -2;
            }

            if (tmpt == null || exclude.contains(tmpt) || active.contains(tmpt))
                continue;

            active.add(tmpt);
        }
    }
}
