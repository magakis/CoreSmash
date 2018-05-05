package com.breakthecore.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TileDictionary {
    private static boolean isInitialized;
    private static final HashMap<Integer, TileAttributes> tileAttributesMap = new HashMap<>();

    private TileDictionary() {
    }

    /* I make use of an initialize() function instead of a static{} block for initialization because
     * later on I will probably have to read from a file and so I want this operation to happen at
     * known time instead of whenever this class gets first used.
     */
    public static void initialilze() {
        if (isInitialized)
            throw new RuntimeException("TileDictionary has already been initialized!");

        TileAttributes.Builder builder = new TileAttributes.Builder();

        TileAttributes attributeRegularTile = builder
                .setTileType(TileType.REGULAR)
                .setMatchable(true)
                .build();

        registerTile(0, attributeRegularTile);
        registerTile(1, attributeRegularTile);
        registerTile(2, attributeRegularTile);
        registerTile(3, attributeRegularTile);
        registerTile(4, attributeRegularTile);
        registerTile(5, attributeRegularTile);
        registerTile(6, attributeRegularTile);
        registerTile(7, attributeRegularTile);
        registerTile(8, attributeRegularTile);

        builder.reset();
        TileAttributes attributeRandomTile = builder
                .setTileType(TileType.RANDOM_REGULAR)
                .setMatchable(false)
                .build();

        registerTile(17, attributeRandomTile);

        isInitialized = true;
    }

    public static TileType getTypeOf(int id) {
        TileAttributes attr = tileAttributesMap.get(id);
        if (attr == null) throw new IllegalArgumentException("Error: Unknown ID(" + id + ")");

        return tileAttributesMap.get(id).getTileType();
    }

    public static int getIdOf(TileType type) {
        Set<Map.Entry<Integer, TileAttributes>> entries = tileAttributesMap.entrySet();

        for (Map.Entry<Integer, TileAttributes> entry : entries) {
            if (entry.getValue().getTileType() == type) {
                return entry.getKey();
            }
        }

        throw new RuntimeException("No entry found with type: " + type);
    }

    private static void registerTile(int id, TileAttributes attributes) {
        TileAttributes slot = tileAttributesMap.get(id);

        if (slot == null) {
            tileAttributesMap.put(id, attributes);
        } else {
            throw new RuntimeException("Errror: ID collision occured (ID:" + id + ")");
        }
    }
}
