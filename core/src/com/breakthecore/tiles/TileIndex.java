package com.breakthecore.tiles;

import com.badlogic.gdx.utils.IntMap;

import java.util.ArrayList;
import java.util.List;

/* Singleton */
public class TileIndex {
    private static TileIndex instance = new TileIndex();
    private boolean frozen;

    private final IntMap<BallAttributes> attributes = new IntMap<>();

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

    public BallAttributes getAttributesFor(int id) {
        BallAttributes value = attributes.get(id);
        if (value == null) throw new RuntimeException("Unknown id: " + id);
        return value;
    }

    public TileType getTypeOf(int id) {
        BallAttributes attr = attributes.get(id);
        if (attr == null) throw new IllegalArgumentException("Error: Unknown ID(" + id + ")");

        return attr.getTileType();
    }

    public List<Integer> getAllPlaceableIDs() {
        List<Integer> result = new ArrayList<>();
        for (IntMap.Entry<BallAttributes> entry : attributes.entries()) {
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

    public void registerTile(BallAttributes attributes) {
        if (frozen) throw new IllegalStateException("TileIndex is frozen");

        int id = attributes.getID();
        BallAttributes slot = this.attributes.get(id);

        if (slot == null) {
            this.attributes.put(id, attributes);
        } else {
            throw new RuntimeException("Error: ID collision occurred (ID:" + id + ")");
        }
    }
}
