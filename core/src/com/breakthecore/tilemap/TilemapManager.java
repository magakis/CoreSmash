package com.breakthecore.tilemap;

import com.badlogic.gdx.math.Vector3;
import com.breakthecore.Coords2D;
import com.breakthecore.Match3;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.Observer;
import com.breakthecore.WorldSettings;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.tiles.MovingBall;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileContainer.Side;
import com.breakthecore.tiles.TileFactory;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private List<Tilemap> tilemaps;
    private int activeTilemaps;
    private final Coords2D defTilemapPosition;

    private TilemapPathfinder pathfinder = new TilemapPathfinder();
    private TilemapBuilder tilemapBuilder = new TilemapBuilder();
    private Match3 match3 = new Match3();
    private int[] colorsAvailable = new int[10]; // XXX(22/4/2018): MagicValue 10 (Should ask TileIndex)

    public TilemapManager() {
        tilemaps = new ArrayList<>();
        defTilemapPosition = new Coords2D(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4);
    }

    public TilemapTile getTilemapTile(int layer, int x, int y) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getTilemapTile(x, y);
    }

    public int getTotalTileCount() {
        int res = 0;
        for (Tilemap tilemap : tilemaps) {
            res += tilemap.getTileCount();
        }
        return res;
    }

    public int getTileCountFrom(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getTileCount();
    }

    public boolean layerExists(int layer) {
        if (layer < 0) throw new IllegalArgumentException("Requested layer was: " + layer);
        return layer < activeTilemaps;
    }

    public int getTilemapCount() {
        return activeTilemaps;
    }

    public Vector3 getWorldToLayerCoords(int layer, Vector3 world) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getWorldToTilemapCoords(world);
    }

    public boolean isTileEmpty(int layer, int x, int y) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getTilemapTile(x, y) == null;
    }

    public Coords2D getDefTilemapPosition() {
        return defTilemapPosition;
    }

    public TilemapTile checkForCollision(CollisionDetector detector, MovingBall mball) {
        TilemapTile result = null;
        for (int i = activeTilemaps - 1; i >= 0; --i) {
            result = detector.findCollision(tilemaps.get(i), mball);
            if (result != null) break;
        }
        return result;
    }

    public void serializeBalls(int layer, XmlSerializer serializer, String namespace, String tag) throws IOException {
        assertLayerIndex(layer);
        tilemaps.get(layer).serializeBalls(serializer, namespace, tag);
    }

    public int[] getColorAmountsAvailable() {
        for (int i = 0; i < colorsAvailable.length; ++i) {
            colorsAvailable[i] = 0;
        }

        int tilemapCount = tilemaps.size();
        for (int tmIndex = 0; tmIndex < tilemapCount; ++tmIndex) {
            if (tilemaps.get(tmIndex).getTileCount() == 0) continue;

            int[] listOfColorAmounts = tilemaps.get(tmIndex).getColorAmountsAvailable();
            for (int i = 0; i < listOfColorAmounts.length; ++i) {
                colorsAvailable[i] += listOfColorAmounts[i];
            }
        }

        return colorsAvailable;
    }

    /**
     * Every method using this check assumes the layer index has been checked prior calling it
     */
    private void assertLayerIndex(int layer) {
        if (layer < 0 || layer >= activeTilemaps)
            throw new IndexOutOfBoundsException("Layer '" + layer + "' doesn't exist");
    }

    public float getLayerPositionX(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getWorldPosition().x;
    }

    public float getLayerPositionY(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getWorldPosition().y;
    }

    public float getLayerOriginX(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOrigin().x;
    }

    public float getLayerOriginY(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOrigin().y;
    }

    public float getLayerOffsetX(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOffset().x;
    }

    public float getLayerOffsetY(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOffset().y;
    }

    public void setMapPosition(int layer, int x, int y) {
        assertLayerIndex(layer);
        tilemaps.get(layer).setMapPosition(x, y);
    }

    public void placeTile(int layer, int x, int y, int tileID) {
        assertLayerIndex(layer);
        tilemaps.get(layer).putTilemapTile(x, y, TileFactory.getTileFromID(tileID));
    }

    public void removeTile(int layer, int x, int y) {
        assertLayerIndex(layer);
        tilemaps.get(layer).destroyTilemapTile(x, y);
    }

    public int getCenterTileID() {
        return tilemaps.get(0).getTilemapTile(0, 0).getTileID();
    }

    public TilemapTile attachBall(MovingBall ball, TilemapTile tileHit, CollisionDetector collisionDetector) {
        Tilemap layer = tilemaps.get(tileHit.getGroupId());
        Side[] sides = collisionDetector.getClosestSides(layer.getRotation(), collisionDetector.getDirection(ball.getPositionInWorld(), tileHit.getPositionInWorld()));

        return attachTile(tileHit.getGroupId(), ball.extractTile(), tileHit, sides);
    }

    //////////////////| GET RID OF |//////////////////

    /**
     * Finds an empty side from the coordinates specified and attach the tile provided.
     *
     * @returns Returns whether it placed the tile.
     */
    private TilemapTile attachTile(int layer, Tile tile, TilemapTile tileHit, Side[] listSides) {
        assertLayerIndex(layer);
        for (Side side : listSides) {
            if (tileHit.getNeighbour(side) == null) {
                tilemaps.get(layer).attachTile(tileHit, tile, side);
                return tileHit.getNeighbour(side);
            }
        }
        throw new RuntimeException("No empty side on collided tile");
    }

    public void handleColorMatchesFor(TilemapTile newTile) {
        Objects.requireNonNull(newTile);
        Tilemap tm = tilemaps.get(newTile.getGroupId());
        ArrayList<TilemapTile> match = match3.getColorMatchesFromTile(newTile);

        if (match.size() < 3) {
            if (match.size() == 1) {
                notifyObservers(NotificationType.NO_COLOR_MATCH, null);
            }
            return;
        }

        List<TilemapTile> disconnected;
        if (tilemaps.get(newTile.getGroupId()).isChained()) {
            disconnected = pathfinder.getDisconnectedTiles(match);
        } else {
            disconnected = match;
        }

        for (TilemapTile t : disconnected) {
            int x = t.getX();
            int y = t.getY();
            if (x == 0 && y == 0) {
                notifyObservers(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
            }
            tm.destroyTilemapTile(x, y);
        }

        notifyObservers(NotificationType.SAME_COLOR_MATCH, match.size());
    }

    //////////////////|            |//////////////////
    public TilemapBuilder newLayer() {
        Tilemap tm;
        if (activeTilemaps < tilemaps.size()) {
            tm = tilemaps.get(activeTilemaps);
        } else {
            tm = new Tilemap(activeTilemaps, defTilemapPosition);
            tm.addObserver(this);
            tilemaps.add(tm);
        }

        ++activeTilemaps;
        tilemapBuilder.startNewTilemap(tm);
        return tilemapBuilder;
    }

    public void update(float delta) {
        for (Tilemap tilemap : tilemaps) {
            tilemap.update(delta);
        }
    }

    public void reset() {
        for (Tilemap tilemap : tilemaps) {
            tilemap.reset();
        }
        activeTilemaps = 0;
    }

    public void draw(RenderManager renderManager) {
        for (int i = 0; i < activeTilemaps; ++i) {
            renderManager.draw(tilemaps.get(i));
        }
    }

    public void draw(RenderManager renderManager, int layer) {
        assertLayerIndex(layer);
        if (tilemaps.get(layer).getTileCount() == 0) return;
        renderManager.draw(tilemaps.get(layer));
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        notifyObservers(type, ob);
    }

    public void forceRotateLayer(int layer, float degrees) {
        assertLayerIndex(layer);
        tilemaps.get(layer).rotate(degrees);
    }

}
