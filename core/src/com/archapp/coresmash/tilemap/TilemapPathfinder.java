package com.archapp.coresmash.tilemap;

import com.archapp.coresmash.tiles.Breakable;
import com.archapp.coresmash.tiles.TileContainer.Side;
import com.archapp.coresmash.tiles.TileType;

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

    // NOTE: current implementation is vulnerable to destroyLists that contains all balls around the center
    // tile effectively destroying every destroyable ball on the tilemap
    public void getDestroyableTiles(List<TilemapTile> destroyList, List<TilemapTile> output) {
        disconnected.clear();
        TilemapTile centerTile = null;

        for (TilemapTile tile : destroyList) {
            if (tile.getX() == 0 && tile.getY() == 0 && tile.getLayerId() == 0) {
                centerTile = tile;
                continue;
            }
            addDisconnectedTilesFrom(tile);
        }

        for (TilemapTile t : disconnected) {
            if (!output.contains(t))
                output.add(t);
        }

        // added at the end of the list cause when it is destroyed, it emmits that the center
        // tile has been destroyed and freezes StatManager preventing it from obtaining the last points
        if (centerTile != null) {
            if (!output.contains(centerTile))
                output.add(centerTile);
        }
    }

    public void getDestroyableTiles(TilemapTile destroyed, List<TilemapTile> output) {
        disconnected.clear();

        if (destroyed.getX() == 0 && destroyed.getY() == 0 && destroyed.getLayerId() == 0) {
            if (!output.contains(destroyed))
                output.add(destroyed);
            return;
        }

        addDisconnectedTilesFrom(destroyed);

        for (TilemapTile t : disconnected) {
            if (!output.contains(t))
                output.add(t);
        }
    }

    private void addDisconnectedTilesFrom(TilemapTile destroyed) {
        if (disconnected.contains(destroyed)) return;

        altered.clear();
        disconnected.add(destroyed);

        for (Side side : Side.values()) {
            TilemapTile neighbour = destroyed.getNeighbour(side);
            if (neighbour != null && !altered.contains(neighbour) &&
                    (neighbour.getTile().getTileType().getMajorType() == TileType.MajorType.REGULAR || neighbour.getTile() instanceof Breakable)) {
                altered.add(neighbour);
            }
        }


        for (TilemapTile origin : altered) {
            if (disconnected.contains(origin)) continue;

            if (!isConnected(origin)) {
                for (TilemapTile tile : closed) {
                    if (tile.getTile() instanceof Breakable) {
                        disconnected.add(tile);
                    }
                }
            }
        }

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
                    !closed.contains(neighbour) &&
                    !opened.contains(neighbour) &&
                    !disconnected.contains(neighbour)) {
                opened.add(neighbour);
            }
        }

    }
}