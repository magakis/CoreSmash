package com.breakthecore.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum TileType {
    REGULAR_BALL1(0, true, Type.REGULAR),
    REGULAR_BALL2(1, true, Type.REGULAR),
    REGULAR_BALL3(2, true, Type.REGULAR),
    REGULAR_BALL4(3, true, Type.REGULAR),
    REGULAR_BALL5(4, true, Type.REGULAR),
    REGULAR_BALL6(5, true, Type.REGULAR),
    REGULAR_BALL7(6, true, Type.REGULAR),
    REGULAR_BALL8(7, true, Type.REGULAR),
    RANDOM_REGULAR(17, true, Type.EDITOR_ONLY),
    WALL_BALL(18, true, Type.SPECIAL),
    SPIKY_BALL(20, true, Type.SPECIAL),
    FIREBALL(101, false, Type.POWERUP),
    COLORBOMB(102, false, Type.POWERUP),;

    TileType(int id, boolean placeable, Type type) {
        this.tileID = id;
        this.isPlaceable = placeable;
        this.type = type;
        if (isPlaceable) {
            Placeables.list.add(this);
        }
    }

    private int tileID;
    private boolean isPlaceable;
    private Type type;

    public Type getType() {
        return type;
    }

    public static List<TileType> getAllPlaceables() {
        return Collections.unmodifiableList(Placeables.list);
    }

    public static TileType getTileTypeFromID(int id) {
        for (TileType t : values()) {
            if (t.tileID == id) return t;
        }
        throw new RuntimeException("Unknown ID:" + id);
    }

    public int getID() {
        return tileID;
    }

    public enum PowerupType {
        FIREBALL(TileType.FIREBALL),
        COLORBOMB(TileType.COLORBOMB);

        private TileType type;

        PowerupType(TileType type) {
            this.type = type;
        }

        public TileType getType() {
            return type;
        }
    }

    public enum Type {
        REGULAR,
        EDITOR_ONLY,
        POWERUP,
        SPECIAL
    }

    private static final class Placeables {
        static final List<TileType> list = new ArrayList<>(11);
    }
}
