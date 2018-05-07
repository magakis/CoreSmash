package com.breakthecore.tiles;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TileDictionary {
    private static boolean isInitialized;
    private static final IntMap<TileAttributes> tileAttributesMap = new IntMap<>();

    private TileDictionary() {
    }

    /* I make use of an initialize() function instead of a static{} block for initialization because
     * later on I will probably have to read from a file and so I want this operation to happen at
     * known time instead of whenever this class gets first used.
     *
     * NOTE: Calling initialize() a second time is clearly a programmer error but I can't throw
     * in case the TileDictionary has already been initialized because when I "close" the app, it doesn't
     * necessarily get killed by the system and static values might remain in memory in which case
     * the isInitialized flag will already be set when the normal initialize() call happens.
     */
    public static void initialilze() {
        if (isInitialized) return;

        TileAttributes.Builder builder = new TileAttributes.Builder();

        TileAttributes attrRegularTile = builder
                .setTileType(TileType.REGULAR)
                .setMatchable(true)
                .setPlaceable(true)
                .build();

        registerTile(0, attrRegularTile);
        registerTile(1, attrRegularTile);
        registerTile(2, attrRegularTile);
        registerTile(3, attrRegularTile);
        registerTile(4, attrRegularTile);
        registerTile(5, attrRegularTile);
        registerTile(6, attrRegularTile);
        registerTile(7, attrRegularTile);

        builder.reset();
        TileAttributes attrNotMatchable = builder
                .setTileType(TileType.RANDOM_REGULAR)
                .setPlaceable(true)
                .setMatchable(false)
                .build();

        registerTile(17, attrNotMatchable);


        registerTile(19, builder.setTileType(TileType.WALL).setBreakable(false).build());

        builder.reset();
        TileAttributes attrBomb = builder
                .setTileType(TileType.BOMB)
                .build();

        registerTile(18, attrBomb);

        isInitialized = true;
    }

    public static TileType getTypeOf(int id) {
        TileAttributes attr = tileAttributesMap.get(id);
        if (attr == null) throw new IllegalArgumentException("Error: Unknown ID(" + id + ")");

        return attr.getTileType();
    }

    public static boolean isBreakable(int id) {
        TileAttributes attr = tileAttributesMap.get(id);
        if (attr == null) throw new IllegalArgumentException("Error: Unknown ID(" + id + ")");

        return attr.isBreakable();
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
        while(keys.hasNext) {
            result.add(keys.next());
        }
        return result;
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