package com.breakthecore.tiles;

import com.badlogic.gdx.utils.IntMap;

import java.util.ArrayList;
import java.util.List;

/* Singleton */
public class TileIndex {
    private static TileIndex instance = new TileIndex();
    private boolean frozen;

    private final IntMap<TileAttributes> attributes = new IntMap<>();

    /* Disable constructor */
    private TileIndex() {
    }

    public static TileIndex get() {
        return instance;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        frozen = true;
    }

    public TileAttributes getAttributesFor(int id) {
        TileAttributes value = attributes.get(id);
        if (value == null) throw new RuntimeException("Unknown id: " + id);
        return value;
    }

    public TileType getTypeOf(int id) {
        TileAttributes attr = attributes.get(id);
        if (attr == null) throw new IllegalArgumentException("Error: Unknown ID(" + id + ")");

        return attr.getTileType();
    }

    public int getIdOf(TileType type) {
        for (IntMap.Entry<TileAttributes> entry : attributes.entries()) {
            if (entry.value.getTileType() == type) {
                return entry.key;
            }
        }
        throw new RuntimeException("No entry found with type: " + type);
    }

    public List<Integer> getAllPlaceableIDs() {
        List<Integer> result = new ArrayList<>();
        for (IntMap.Entry<TileAttributes> entry : attributes.entries()) {
            if (entry.value.isPlaceable()) {
                result.add(entry.key);
            }
        }
        return result;
    }

    public List<Integer> getAllRegisteredIDs() {
        List<Integer> result = new ArrayList<>();
        IntMap.Keys keys = attributes.keys();
        while (keys.hasNext) {
            result.add(keys.next());
        }
        return result;
    }

    public void registerTile(TileAttributes attributes) {
        if (frozen) throw new IllegalStateException("TileIndex is frozen");

        int id = attributes.getID();
        TileAttributes slot = this.attributes.get(id);

        if (slot == null) {
            this.attributes.put(id, attributes);
        } else {
            throw new RuntimeException("Error: ID collision occurred (ID:" + id + ")");
        }
    }
}