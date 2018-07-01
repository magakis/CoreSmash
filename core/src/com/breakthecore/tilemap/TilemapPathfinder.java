package com.breakthecore.tilemap;

import com.breakthecore.tiles.Breakable;
import com.breakthecore.tiles.TileContainer.Side;
import com.breakthecore.tiles.TileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class TilemapPathfinder {
    private List<TilemapTile> altered;
    private List<TilemapTile> closed;
    private List<TilemapTile> opened;
    private List<TilemapTile> disconnected;
    private Comparator<TilemapTile> compDistance;

    TilemapPathfinder() {
        altered = new ArrayList<>();
        closed = new ArrayList<>();
        opened = new ArrayList<>();
        disconnected = new ArrayList<>();
        compDistance = new Comparator<TilemapTile>() {
            @Override
            public int compare(TilemapTile t0, TilemapTile t1) {
                return Integer.compare(t0.getDistanceFromCenter(), t1.getDistanceFromCenter());
            }
        };
    }

    public List<TilemapTile> getDisconnectedTiles(List<TilemapTile> matched) {
        disconnected.clear();
        altered.clear();
        disconnected.addAll(matched);

        for (TilemapTile tmTile : matched) {
            // If the center tile is matched return only the matches as the disconnected tiles
            if (tmTile.getDistanceFromCenter() == 0) {
                return disconnected;
            }

            for (Side side : Side.values()) {
                TilemapTile neighbour = tmTile.getNeighbour(side);
                if (neighbour != null && !altered.contains(neighbour) &&
                        (neighbour.getTile().getAttributes().getTileType() == TileType.REGULAR || neighbour.getTile() instanceof Breakable)) {
                    altered.add(neighbour);
                }
            }
        }

        for (TilemapTile origin : altered) {
            if (disconnected.contains(origin)) continue;

            if (!isConnected(origin)) {
                disconnected.addAll(closed);
            }
        }

        return disconnected;
    }

    private boolean isConnected(TilemapTile tmTile) {
        closed.clear();
        opened.clear();
        opened.add(tmTile);
        do {
            if (opened.get(0).getDistanceFromCenter() == 0) return true;
            addSurroundingTiles(opened.get(0));
            Collections.sort(opened, compDistance);
        } while (opened.size() != 0);

        return false;
    }

    private void addSurroundingTiles(TilemapTile tmTile) {
        opened.remove(tmTile);
        closed.add(tmTile);
        for (Side side : Side.values()) {
            TilemapTile neighbour = tmTile.getNeighbour(side);
            if (neighbour != null &&
                    (!closed.contains(neighbour) && !opened.contains(neighbour) && !disconnected.contains(neighbour)) &&
                    (neighbour.getTile().getAttributes().getTileType() == TileType.REGULAR || neighbour.getTile() instanceof Breakable)) {
                opened.add(neighbour);
            }
        }
    }
}
