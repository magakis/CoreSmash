package com.breakthecore.tilemap;

import com.breakthecore.Coords2D;
import com.breakthecore.Match3;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.Observer;
import com.breakthecore.Pathfinder;
import com.breakthecore.WorldSettings;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.tiles.MovingBall;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileContainer;
import com.breakthecore.tiles.TileDictionary;
import com.breakthecore.tiles.TileFactory;

import java.util.ArrayList;
import java.util.Random;

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
public class TilemapManager extends Observable implements Observer {
    /**
     * Holds the different independent layers(tilemaps). 0 is the main layer.
     */
    private final Tilemap[] tilemap;
    private final Coords2D tilemapPosition = new Coords2D(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4);
    private final int maxTilemapCount = 2;

    /** This is only used by the TilemapBuilder which will become obsolete after level builder is setup. */
    @Deprecated
    private int tilemapCount;

    private Pathfinder pathfinder = new Pathfinder(30);
    private TilemapBuilder tilemapBuilder = new TilemapBuilder();
    private Match3 match3 = new Match3();
    private int[] colorsAvailable = new int[10]; // XXX(22/4/2018): MagicValue 10

    public TilemapManager() {
        tilemap = new Tilemap[maxTilemapCount];

        for (int i = 0; i < maxTilemapCount; ++i) {
            tilemap[i] = new Tilemap(i, tilemapPosition);
            tilemap[i].addObserver(this);
        }
    }

    public int getMaxTilemapCount() {
        return maxTilemapCount;
    }

    public int getTotalTileCount() {
        int res = 0;
        for (int i = 0; i < maxTilemapCount; ++i) {
            res += tilemap[i].getTileCount();
        }
        return res;
    }

    public int getTileCountFrom(int layer) {
        if (layer < 0 || layer >= maxTilemapCount)
            throw new IndexOutOfBoundsException("Layer '" + layer + "' doesn't exist");
        return tilemap[layer].getTileCount();
    }

    public Coords2D getTilemapPosition() {
        return tilemapPosition;
    }

    public Tilemap getTilemap(int layer) {
        if (layer >= maxTilemapCount && layer < 0) {
            throw new IndexOutOfBoundsException("Wrong layer index!");
        }
        return tilemap[layer];
    }

    public int[] getColorAmountsAvailable() {
        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        for (int tmIndex = 0; tmIndex < maxTilemapCount; ++tmIndex) {
            if (tilemap[tmIndex].getTileCount() == 0) continue;

            int[] listOfColorAmounts = tilemap[tmIndex].getColorAmountsAvailable();
            for (int i = 0; i < listOfColorAmounts.length; ++i) {
                colorsAvailable[i] += listOfColorAmounts[i];
            }
        }

        return colorsAvailable;
    }

    public void placeTile(int layer, int x, int y, int tileID) {
        if (layer >= maxTilemapCount && layer < 0) {
            throw new IndexOutOfBoundsException("Wrong layer index!");
        }
        tilemap[layer].setRelativeTile(x,y, TileFactory.getTileFromID(tileID));
    }

    public TilemapTile attachBall(MovingBall ball, TilemapTile tileHit, CollisionDetector collisionDetector) {
        Tilemap layer = tilemap[tileHit.getTilemapId()];
        TileContainer.Side[] sides = collisionDetector.getClosestSides(layer.getCos(), layer.getSin(), collisionDetector.getDirection(ball.getPositionInWorld(), tileHit.getPositionInWorld()));

        return attachTile(tileHit.getTilemapId(), ball.extractTile(), tileHit, sides);
    }

    //////////////////| GET RID OF |//////////////////
    /**
     * Finds an empty side from the coordinates specified and attach the tile provided.
     *
     * @returns Returns whether it placed the tile.
     */
    private TilemapTile attachTile(int layer, Tile tile, TilemapTile tileHit, TileContainer.Side[] listSides) {
        TilemapTile placedTile = null;
        for (int i = 0; i < 6; ++i) {
            placedTile = attachTile(layer, tile, tileHit, listSides[i]);
            if (placedTile == null) continue;
            break;
        }
        return placedTile;
    }

    private TilemapTile attachTile(int layer, Tile tile, TilemapTile tileHit, TileContainer.Side side) {
        Tilemap tm = tilemap[layer];
        Coords2D tileHitPos = tileHit.getRelativePosition();
        TilemapTile createdTilemapTile = null;

        switch (side) {
            case TOP_RIGHT:
                if (tm.getRelativeTile(tileHitPos.x, tileHitPos.y + 1) == null) {
                    tm.setRelativeTile(tileHitPos.x, tileHitPos.y + 1, tile);
                    createdTilemapTile = tm.getRelativeTile(tileHitPos.x, tileHitPos.y + 1);
                }
                break;
            case TOP_LEFT:
                if (tm.getRelativeTile(tileHitPos.x - 1, tileHitPos.y + 1) == null) {
                    tm.setRelativeTile(tileHitPos.x - 1, tileHitPos.y + 1, tile);
                    createdTilemapTile = tm.getRelativeTile(tileHitPos.x - 1, tileHitPos.y + 1);
                }
                break;
            case RIGHT:
                if (tm.getRelativeTile(tileHitPos.x + 1, tileHitPos.y) == null) {
                    tm.setRelativeTile(tileHitPos.x + 1, tileHitPos.y, tile);
                    createdTilemapTile = tm.getRelativeTile(tileHitPos.x + 1, tileHitPos.y);
                }
                break;
            case LEFT:
                if (tm.getRelativeTile(tileHitPos.x - 1, tileHitPos.y) == null) {
                    tm.setRelativeTile(tileHitPos.x - 1, tileHitPos.y, tile);
                    createdTilemapTile = tm.getRelativeTile(tileHitPos.x - 1, tileHitPos.y);
                }
                break;
            case BOTTOM_LEFT:
                if (tm.getRelativeTile(tileHitPos.x, tileHitPos.y - 1) == null) {
                    tm.setRelativeTile(tileHitPos.x, tileHitPos.y - 1, tile);
                    createdTilemapTile = tm.getRelativeTile(tileHitPos.x, tileHitPos.y - 1);
                }
                break;
            case BOTTOM_RIGHT:
                if (tm.getRelativeTile(tileHitPos.x + 1, tileHitPos.y - 1) == null) {
                    tm.setRelativeTile(tileHitPos.x + 1, tileHitPos.y - 1, tile);
                    createdTilemapTile = tm.getRelativeTile(tileHitPos.x + 1, tileHitPos.y - 1);
                }
                break;
        }

        return createdTilemapTile;
    }

    public void handleColorMatchesFor(TilemapTile newTile) {
        Tilemap tm = tilemap[newTile.getTilemapId()];
        ArrayList<TilemapTile> match = match3.getColorMatchesFromTile(newTile, tm);

        if (match.size() < 3) {
            if (match.size() == 1) {
                notifyObservers(NotificationType.NO_COLOR_MATCH, null);
            }
            return;
        }

        boolean centerTileDestroyed = false;
        for (TilemapTile t : match) {
            Coords2D pos = t.getRelativePosition();
            if (pos.x == 0 && pos.y == 0) {
                notifyObservers(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
                centerTileDestroyed = true;
            }
            tm.destroyRelativeTile(pos.x, pos.y);
        }

        if (!centerTileDestroyed) {
            removeDisconnectedTiles();
        }

        notifyObservers(NotificationType.SAME_COLOR_MATCH, match.size());
    }
    //////////////////|            |//////////////////

    public TilemapBuilder newLayer() {
        if (tilemapCount == maxTilemapCount) {
            throw new RuntimeException("Cannot initialize more tilemaps");
        }
        tilemapBuilder.startNewTilemap(tilemap[tilemapCount]);
        ++tilemapCount;
        return tilemapBuilder;
    }

    public void update(float delta) {
        for (int i = 0; i < maxTilemapCount; ++i) {
            tilemap[i].update(delta);
        }
    }

    public void reset() {
        tilemapCount = 0;
        for (int i = 0; i < maxTilemapCount; ++i) {
            tilemap[i].reset();
        }
    }

    public void draw(RenderManager renderManager) {
        for (int i = 0; i < maxTilemapCount; ++i) {
            if (tilemap[i].getTileCount() == 0) continue;
            renderManager.draw(tilemap[i]);
        }
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        notifyObservers(type, ob);
    }

    /**
     * Destroys tiles that are not connected to the center tile.
     * This check is only done on the 0 layer cause that is the only one that requires tiles to be connected to the center tile.
     */
    private void removeDisconnectedTiles() {
        TilemapTile tile;
        Tilemap tm = tilemap[0];
        for (int y = 0; y < tm.getTilemapSize(); ++y) {
            for (int x = 0; x < tm.getTilemapSize(); ++x) {
                tile = tm.getAbsoluteTile(x, y);
                if (tile != null) {
                    if (!TileDictionary.isBreakable(tile.getTileID())) continue; //skip unbreakables
                    if (pathfinder.getPathToCenter(tile, tm) == null) {
                        // TODO(13/4/2018): I should put TilemapTiles in an Object pool
                        tm.destroyAbsoluteTile(x, y);
                    }
                }
            }
        }
    }
}
