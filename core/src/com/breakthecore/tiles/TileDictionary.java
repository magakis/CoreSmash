package com.breakthecore.tiles;

import com.badlogic.gdx.utils.IntMap;

import java.util.ArrayList;
import java.util.List;

public class TileDictionary {
    private static final IntMap<TileAttributes> tileAttributesMap = new IntMap<>();
    private static boolean isInitialized;

    private TileDictionary() {
    }

    public static void initialize() {
        isInitialized = true;
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static TileAttributes getAttributesFor(int id) {
        TileAttributes value = tileAttributesMap.get(id);
        if (value == null) throw new RuntimeException("Unknown id: " + id);
        return value;
    }

    public static TileType getTypeOf(int id) {
        TileAttributes attr = tileAttributesMap.get(id);
        if (attr == null) throw new IllegalArgumentException("Error: Unknown ID(" + id + ")");

        return attr.getTileType();
    }

    public static int getIdOf(TileType type) {
        for (IntMap.Entry<TileAttributes> entry : tileAttributesMap.entries()) {
            if (entry.value.getTileType() == type) {
                return entry.key;
            }
        }
        throw new RuntimeException("No entry found with type: " + type);
    }

    public static List<Integer> getAllPlaceableIDs() {
        List<Integer> result = new ArrayList<>();
        for (IntMap.Entry<TileAttributes> entry : tileAttributesMap.entries()) {
            if (entry.value.isPlaceable()) {
                result.add(entry.key);
            }
        }
        return result;
    }

    public static List<Integer> getAllRegisteredIDs() {
        List<Integer> result = new ArrayList<>();
        IntMap.Keys keys = tileAttributesMap.keys();
        while (keys.hasNext) {
            result.add(keys.next());
        }
        return result;
    }

    public static void registerTile(TileAttributes attributes) {
        int id = attributes.getID();
        TileAttributes slot = tileAttributesMap.get(id);

        if (slot == null) {
            tileAttributesMap.put(id, attributes);
        } else {
            throw new RuntimeException("Error: ID collision occurred (ID:" + id + ")");
        }
    }
}
