package com.archapp.coresmash.tilemap;

import com.archapp.coresmash.Coords2D;
import com.archapp.coresmash.Match3;
import com.archapp.coresmash.NotificationType;
import com.archapp.coresmash.Observable;
import com.archapp.coresmash.Observer;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.managers.RenderManager;
import com.archapp.coresmash.tiles.AstronautBall;
import com.archapp.coresmash.tiles.Destroyable;
import com.archapp.coresmash.tiles.Tile;
import com.archapp.coresmash.tiles.TileContainer.Side;
import com.archapp.coresmash.tiles.TileType;

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
    private int[] colorsAvailable = new int[8]; // XXX(22/4/2018): MagicValue 7 (Should ask TileIndex)

    public TilemapManager() {
        defTilemapPosition = new Coords2D(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4);
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

    public float getLayerRotation(int layer) {
        return worldMap.getLayerRotation(layer);
    }

    public void removeTile(TilemapTile tmTile) {
        removeTile(tmTile.getLayerId(), tmTile.getX(), tmTile.getY());
    }

    /**
     * Ensure the provided list contains no null values
     */
    public void removeTile(List<TilemapTile> forRemoval) {
        worldMap.removeTile(forRemoval);

        for (TilemapTile tile : forRemoval) {
            handleTileRemoval(tile);
        }
    }

    public void removeTile(int layer, int x, int y) {
        TilemapTile tmTile = worldMap.removeTile(layer, x, y);
        if (tmTile == null) return;

        handleTileRemoval(tmTile);
    }

    private void handleTileRemoval(TilemapTile tile) {
        tileList.remove(tile);

        removeColorAvailable(tile);

        Tile removed = tile.getTile();
        if (removed instanceof Destroyable) {
            ((Destroyable) removed).onDestroy(tile, this);
        }

        if (tile.getLayerId() == 0 && tile.getX() == 0 && tile.getY() == 0) {
            notifyObservers(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
        }
    }

    public int getCenterTileID() {
        return worldMap.getTilemapTile(0, 0, 0).getTileID();
    }

    public Tile getCenterTile() {
        return worldMap.getTilemapTile(0, 0, 0).getTile();
    }

    /**
     * Finds an empty side from the coordinates specified and attach the tile provided.
     *
     * @returns Returns the TilemapTile that was placed or null.
     * @warning Uses TileID as index in an array. If IDs change in the furure, it will be a mess.
     */
    public TilemapTile attachBall(Tile tile, TilemapTile tileHit, Side[] listSides) {
        for (Side side : listSides) {
            if (tileHit.getNeighbour(side) == null) {
                TilemapTile newTile = worldMap.placeTile(tileHit, tile, side);
                if (newTile != null) { //
                    addColorAvailable(newTile);
                }
                tileList.add(newTile);
                return newTile;
            }
        }
        throw new RuntimeException("No empty side on collided tile");
    }

    public void destroyTiles(TilemapTile tile) {
        if (worldMap.isChained(tile.getLayerId())) {
            pathfinder.getDestroyableTiles(tile, queuedForDeletion);
            removeTile(queuedForDeletion);
        } else {
            removeTile(tile);
        }
    }

    public void destroyTiles(int layer, int x, int y) {
        TilemapTile tile = worldMap.getTilemapTile(layer, x, y);
        if (tile == null)
            throw new RuntimeException("Couldn't find tile at Layer:" + layer + " X: " + x + " Y:" + y);

        destroyTiles(tile);
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
            removeTile(destroyList);
        } else {
            if (worldMap.isChained(destroyList.get(0).getLayerId())) {
                pathfinder.getDestroyableTiles(destroyList, queuedForDeletion);
                removeTile(queuedForDeletion);
            } else {
                removeTile(destroyList);
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
                short astronautsFound = 0;
                for (TilemapTile tile : tiles) {
                    if (tile.getTile().getTileType().getMajorType() == TileType.MajorType.ASTRONAUT)
                        ++astronautsFound;
                    else
                        addColorAvailable(tile);
                }
                if (astronautsFound > 0)
                    notifyObservers(NotificationType.ASTRONAUTS_FOUND, astronautsFound);
                tileList.addAll(tiles);
                break;
            default:
                notifyObservers(type, ob);
        }
    }

    private void addColorAvailable(TilemapTile newTile) {
        Tile tile = newTile.getTile();
        switch (tile.getTileType().getMajorType()) {
            case REGULAR:
                ++colorsAvailable[tile.getID()];
                break;
            case ASTRONAUT:
                ++colorsAvailable[((AstronautBall) tile).getMatchID()];
                break;
        }
    }

    private void removeColorAvailable(TilemapTile removedTile) {
        Tile tile = removedTile.getTile();
        switch (tile.getTileType().getMajorType()) {
            case REGULAR:
                --colorsAvailable[tile.getID()];
                break;
            case ASTRONAUT:
                --colorsAvailable[((AstronautBall) tile).getMatchID()];
                break;
        }
    }

    public interface TilemapEffect {
        void apply(TilemapManager manager);
    }
}
