package com.archapp.coresmash.tilemap;

import com.archapp.coresmash.Coords2D;
import com.archapp.coresmash.Observer;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.tiles.Tile;
import com.archapp.coresmash.tiles.TileContainer.Side;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The TilemapManager is responsible for every interaction with the Tilemaps and provides the
 * only interface for them.
 * XXX: BAD IMPLEMENTATION!
 */

/** FIXME: 1/5/2018 TilemapManager should control _*EVERY*_ TilemapTile creation
 * TODO: Implement a better way of setting up Tilemaps and balancing them.
 *
 * The following has been implemented:
 * One possible solution would be to have the TilemapManager return a Tilemap Builder when a new
 * Tilemap is requested which will contain an array similar to the Tilemap in which I can simply place
 * the IDs and balance the map based on them. After I have applied the filter I want, I will instantiate
 * the Tilemap with that builder and create the tiles
 */
public class Map extends com.archapp.coresmash.Observable implements TilemapCollection {
    private final Coords2D defMapPosition;
    private int activeTilemaps;
    Observer tmObserver;
    private List<Tilemap> tilemaps;

    public Map() {
        this(null);
    }

    public Map(Observer observer) {
        tilemaps = new ArrayList<>();
        defMapPosition = new Coords2D(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4);
        tmObserver = observer;
    }

    @Override
    public TilemapTile getTilemapTile(int layer, int x, int y) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getTilemapTile(x, y);
    }

    public int getTileCountFrom(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getTileCount();
    }

    public float getLayerRotation(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getRotation();
    }

    @Override
    public boolean layerExists(int layer) {
        if (layer < 0) throw new IllegalArgumentException("Requested layer was: " + layer);
        return layer < activeTilemaps;
    }

    public Vector3 getWorldToLayerCoords(int layer, Vector3 world) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getWorldToTilemapCoords(world);
    }

    public boolean isTileEmpty(int layer, int x, int y) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getTilemapTile(x, y) == null;
    }

    public boolean isChained(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).isChained();
    }

    public float getDefPositionX() {
        return defMapPosition.x;
    }

    public float getDefPositionY() {
        return defMapPosition.y;
    }

    public void serializeBalls(int layer, XmlSerializer serializer, String namespace, String tag) throws IOException {
        assertLayerIndex(layer);
        tilemaps.get(layer).serializeBalls(serializer, namespace, tag);
    }

    @Override
    public float getLayerPositionX(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getPositionX();
    }

    @Override
    public float getLayerPositionY(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getPositionY();
    }

    public float getLayerOriginX(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOriginX();
    }

    public float getLayerOriginY(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOriginY();
    }

    public TilemapTile placeTile(TilemapTile tmTile, Tile tile, Side side) {
        TilemapTile newTile = tilemaps.get(tmTile.getLayerId()).putTilemapTile(tmTile, tile, side);
        return newTile;
    }

    public TilemapTile placeTile(int layer, int x, int y, Tile tile) {
        assertLayerIndex(layer);
        TilemapTile newTile = tilemaps.get(layer).putTilemapTile(x, y, tile);
        return newTile;
    }

    @Override
    public int layerCount() {
        return activeTilemaps;
    }

    public float getLayerOffsetX(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOffsetX();
    }

    public float getLayerOffsetY(int layer) {
        assertLayerIndex(layer);
        return tilemaps.get(layer).getOffsetY();
    }

    public int totalBallCount() {
        int res = 0;
        for (int i = 0; i < activeTilemaps; ++i) {
            res += tilemaps.get(i).getTileCount();
        }
        return res;
    }

    public void setMapPosition(int layer, int x, int y) {
        assertLayerIndex(layer);
        tilemaps.get(layer).setMapPosition(x, y);
    }

    public TilemapTile removeTile(int layer, int x, int y) {
        assertLayerIndex(layer);
        TilemapTile removed = tilemaps.get(layer).destroyTilemapTile(x, y);
        return removed;
    }

    public int getCenterTileID() {
        return tilemaps.get(0).getTilemapTile(0, 0).getTileID();
    }

    public void setOffset(int layer, float x, float y) {
        assertLayerIndex(layer);
        tilemaps.get(layer).setOffset(x, y);
    }

    public void setOffset(int layer, Vector2 offset) {
        setOffset(layer, offset.x, offset.y);
    }

    public void setOrigin(int layer, float x, float y) {
        assertLayerIndex(layer);
        tilemaps.get(layer).setOrigin(x, y);
    }

    public void setOrigin(int layer, Vector2 origin) {
        setOrigin(layer, origin.x, origin.y);
    }

    public void validate(int layer) {
        assertLayerIndex(layer);
        Tilemap tm = tilemaps.get(layer);
        tm.updateWorldPosition();
        tm.updateTilePositions();
    }

    public void validate() {
        for (int i = 0; i < activeTilemaps; ++i) {
            Tilemap tm = tilemaps.get(i);
            tm.updateWorldPosition();
            tm.updateTilePositions();
        }
    }

    public Tilemap newLayer() {
        if (activeTilemaps >= tilemaps.size()) {
            Tilemap tm = new Tilemap(activeTilemaps, defMapPosition);
            if (tmObserver != null) {
                tm.addObserver(tmObserver);
            }
            tilemaps.add(tm);
        }
        ++activeTilemaps;
        return tilemaps.get(activeTilemaps - 1);
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

    public void draw(com.archapp.coresmash.managers.RenderManager renderManager) {
        for (int i = 0; i < activeTilemaps; ++i) {
            renderManager.draw(tilemaps.get(i));
        }
    }

    public void draw(com.archapp.coresmash.managers.RenderManager renderManager, int layer) {
        assertLayerIndex(layer);
        if (tilemaps.get(layer).getTileCount() == 0) return;
        renderManager.draw(tilemaps.get(layer));
    }

    public void forceRotateLayer(int layer, float degrees) {
        assertLayerIndex(layer);
        tilemaps.get(layer).rotate(degrees);
    }

    /**
     * Every method using this check assumes the layer index has been checked prior calling it
     */
    private void assertLayerIndex(int layer) {
        if (layer < 0 || layer >= activeTilemaps)
            throw new IndexOutOfBoundsException("Layer '" + layer + "' doesn't exist");
    }
}

