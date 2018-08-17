package com.coresmash.tilemap;

import com.coresmash.Coords2D;
import com.coresmash.Match3;
import com.coresmash.NotificationType;
import com.coresmash.Observable;
import com.coresmash.Observer;
import com.coresmash.managers.RenderManager;
import com.coresmash.tiles.Breakable;
import com.coresmash.tiles.RegularTile;
import com.coresmash.tiles.Tile;
import com.coresmash.tiles.TileContainer.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The TilemapManager is responsible for every interaction with the Tilemaps and provides the
 * only interface for them.
 * XXX: BAD IMPLEMENTATION!
 */

/* FIXME: 1/5/2018 TilemapManager should control _*EVERY*_ TilemapTile creation
 * TODO: Implement a better way of setting up Tilemaps and balancing them.
 *
 * The following has been implemented:
 * One possible solution would be to have the TilemapManager return a Tilemap Builder when a new
 * Tilemap is requested which will contain an array similar to the Tilemap in which I can simply place
 * the IDs and balance the map based on them. After I have applied the filter I want, I will instantiate
 * the Tilemap with that builder and create the tiles
 */
public class TilemapManager extends Observable implements TilemapCollection, Observer {
    private final Coords2D defTilemapPosition;
    private final Map worldMap;

    private List<TilemapTile> tileList;
    private List<TilemapTile> queuedForDeletion;
    private TilemapPathfinder pathfinder = new TilemapPathfinder();
    private TilemapBuilder tilemapBuilder = new TilemapBuilder();
    private Match3 match3 = new Match3();
    private int[] colorsAvailable = new int[10]; // XXX(22/4/2018): MagicValue 10 (Should ask TileIndex)

    public TilemapManager() {
        defTilemapPosition = new Coords2D(com.coresmash.WorldSettings.getWorldWidth() / 2, com.coresmash.WorldSettings.getWorldHeight() - com.coresmash.WorldSettings.getWorldHeight() / 4);
        worldMap = new Map(this);
        tileList = new ArrayList<>();
        queuedForDeletion = new ArrayList<>();
    }

    public TilemapTile getTilemapTile(int layer, int x, int y) {
        return worldMap.getTilemapTile(layer, x, y);
    }

    @Override
    public int layerCount() {
        return worldMap.layerCount();
    }

    public boolean layerExists(int layer) {
        return worldMap.layerExists(layer);
    }

    public List<TilemapTile> getTileList() {
        return Collections.unmodifiableList(tileList);
    }

    public Coords2D getDefTilemapPosition() {
        return defTilemapPosition;
    }

    public int[] getColorAmountsAvailable() {
        return colorsAvailable;
    }

    public float getLayerPositionX(int layer) {
        return worldMap.getLayerPositionX(layer);
    }

    public float getLayerPositionY(int layer) {
        return worldMap.getLayerPositionY(layer);
    }

    public void removeTile(TilemapTile tmTile) {
        removeTile(tmTile.getLayerId(), tmTile.getX(), tmTile.getY());
    }

    public float getLayerRotation(int layer) {
        return worldMap.getLayerRotation(layer);
    }

    public void removeTile(int layer, int x, int y) {
        TilemapTile tmTile = worldMap.removeTile(layer, x, y);
        if (tmTile == null) return;

        tileList.remove(tmTile);

        Tile removed = tmTile.getTile();
        //XXX : Magic Number 10
        if (removed.getID() < 10) {
            --colorsAvailable[removed.getID()];
        }

        if (removed instanceof Breakable) {
            ((Breakable) removed).onDestroy(tmTile, this);
        }

        if (layer == 0 && x == 0 && y == 0) {
            notifyObservers(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
        }
    }

    public int getCenterTileID() {
        return worldMap.getTilemapTile(0, 0, 0).getTileID();
    }

    /**
     * Finds an empty side from the coordinates specified and attach the tile provided.
     *
     * @returns Returns the TilemapTile that was placed or null.
     */
    public TilemapTile attachBall(Tile tile, TilemapTile tileHit, Side[] listSides) {
        for (Side side : listSides) {
            if (tileHit.getNeighbour(side) == null) {
                TilemapTile newTile = worldMap.placeTile(tileHit, tile, side);
                if (newTile != null && tile.getID() < 10) { // XXX(30/6/2018): magic value
                    ++colorsAvailable[tile.getID()];
                }
                tileList.add(newTile);
                return newTile;
            }
        }
        throw new RuntimeException("No empty side on collided tile");
    }

    public void destroyTiles(TilemapTile tile) {
        destroyTiles(tile.getLayerId(), tile.getX(), tile.getY());
    }

    public void destroyTiles(int layer, int x, int y) {
        TilemapTile tile = worldMap.getTilemapTile(layer, x, y);
        if (tile == null)
            throw new RuntimeException("Couldn't find tile at Layer:" + layer + " X: " + x + " Y:" + y);

        if (worldMap.isChained(layer)) {
            pathfinder.getDestroyableTiles(tile, queuedForDeletion);
            for (int i = 0; i < queuedForDeletion.size(); ++i) {
                removeTile(queuedForDeletion.get(i));
            }
        } else {
            removeTile(tile);
        }
    }

    /* Assumes that all destroyed tiles come from the *same* layer! */
    public void destroyTiles(List<TilemapTile> destroyList) {
        if (destroyList.size() == 0) return;

        boolean containsCenterTile = false;
        for (TilemapTile tile : destroyList) {
            if (tile.getX() == 0 && tile.getY() == 0 && tile.getLayerId() == 0) {
                containsCenterTile = true;
                break;
            }
        }

        if (containsCenterTile) {
            for (TilemapTile t : destroyList) {
                removeTile(t);
            }
        } else {
            if (worldMap.isChained(destroyList.get(0).getLayerId())) {
                pathfinder.getDestroyableTiles(destroyList, queuedForDeletion);
                for (int i = 0; i < queuedForDeletion.size(); ++i) {
                    removeTile(queuedForDeletion.get(i));
                }
            } else {
                for (TilemapTile t : destroyList) {
                    removeTile(t);
                }
            }
        }
    }

    public List<TilemapTile> getColorMatches(TilemapTile tile) {
        assert tile != null;
        ArrayList<TilemapTile> match = match3.getColorMatchesFromTile(tile);

        if (match.size() < 3) {
            if (match.size() == 1) {
                notifyObservers(NotificationType.NO_COLOR_MATCH, null);
            }
            return Collections.EMPTY_LIST;
        }
        return match;
    }


    public TilemapBuilder newLayer() {
        tilemapBuilder.startNewTilemap(worldMap.newLayer());
        return tilemapBuilder;
    }

    public void update(float delta) {
        worldMap.update(delta);
        queuedForDeletion.clear();
    }

    public void reset() {
        worldMap.reset();
        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }
        tileList.clear();
    }

    public void draw(RenderManager renderManager) {
        worldMap.draw(renderManager);
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case TILEMAP_INITIALIZED:
                List<TilemapTile> tiles = (List<TilemapTile>) ob;
                for (TilemapTile tile : tiles) {
                    if (tile.getTile() instanceof RegularTile) {
                        ++colorsAvailable[tile.getTileID()];
                    }
                }
                tileList.addAll(tiles);
                break;
            default:
                notifyObservers(type, ob);
        }
    }

    public interface TilemapEffect {
        void apply(TilemapManager manager);
    }
}
