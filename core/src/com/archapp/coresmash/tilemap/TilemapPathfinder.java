package com.archapp.coresmash.tilemap;

import com.archapp.coresmash.tiles.Breakable;
import com.archapp.coresmash.tiles.TileContainer.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class TilemapPathfinder {
    private List<TilemapTile> altered;
    private List<TilemapTile> closed;
    private List<TilemapTile> opened;
    private List<TilemapTile> checked;
    private Comparator<TilemapTile> compDistance;

    TilemapPathfinder() {
        altered = new ArrayList<>();
        closed = new ArrayList<>();
        opened = new ArrayList<>();
        checked = new ArrayList<>();
        compDistance = new Comparator<TilemapTile>() {
            @Override
            public int compare(TilemapTile t0, TilemapTile t1) {
                return Integer.compare(t0.getDistanceFromCenter(), t1.getDistanceFromCenter());
            }
        };
    }

    // NOTE: current implementation is vulnerable to destroyLists that contains all balls around the center
    // tile effectively destroying every destroyable ball on the tilemap
    public void getDestroyableTiles(List<TilemapTile> output) {
        checked.clear();
        altered.clear();
        TilemapTile centerTile = null;

        for (TilemapTile tile : output) {
            if (tile.getX() == 0 && tile.getY() == 0 && tile.getLayerID() == 0) {
                centerTile = tile;
                continue;
            }

            for (Side side : Side.values()) {
                TilemapTile neighbour = tile.getNeighbour(side);
                if (neighbour != null && !altered.contains(neighbour) && (neighbour.getTile() instanceof Breakable)) {
                    altered.add(neighbour);
                }
            }
        }

        validateAlteredTiles(output);

        // added at the end of the list cause when it is destroyed, it emmits that the center
        // tile has been destroyed and freezes StatManager preventing it from obtaining the last points
        if (centerTile != null) {
            if (!output.contains(centerTile))
                output.add(centerTile);
        }
    }

    private void validateAlteredTiles(List<TilemapTile> output) {
        for (TilemapTile origin : altered) {
            if (output.contains(origin) || checked.contains(origin)) continue;

            if (isConnected(origin, output)) {
                checked.addAll(closed);
            } else {
                for (TilemapTile tile : closed) {
                    if (tile.getTile() instanceof Breakable) {
                        output.add(tile);
                    }
                }
            }
        }
    }

    private boolean isConnected(TilemapTile tmTile, List<TilemapTile> output) {
        closed.clear();
        opened.clear();
        opened.add(tmTile);
        do {
            if (opened.get(0).getDistanceFromCenter() == 0) return true;
            /* If it is connected to a checked tiles it means it is also connected */
            if (addSurroundingTiles(opened.get(0), output)) return true;

            Collections.sort(opened, compDistance);
        } while (opened.size() != 0);

        return false;
    }

    /**
     * Adds surrounding tiles and returns if it came across a checked neighbour
     */
    private boolean addSurroundingTiles(TilemapTile tmTile, List<TilemapTile> output) {
        opened.remove(tmTile);
        closed.add(tmTile);

        for (Side side : Side.values()) {
            TilemapTile neighbour = tmTile.getNeighbour(side);
            if (neighbour == null) continue;
            if (checked.contains(neighbour)) return true; // found a checked neighbour

            if (!closed.contains(neighbour) && !opened.contains(neighbour) && !output.contains(neighbour)) {
                opened.add(neighbour);
            }
        }
        return false;
    }
}
