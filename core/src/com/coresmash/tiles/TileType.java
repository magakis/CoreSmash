package com.coresmash.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum TileType {
    REGULAR_BALL1(0, true, MajorType.REGULAR),
    REGULAR_BALL2(1, true, MajorType.REGULAR),
    REGULAR_BALL3(2, true, MajorType.REGULAR),
    REGULAR_BALL4(3, true, MajorType.REGULAR),
    REGULAR_BALL5(4, true, MajorType.REGULAR),
    REGULAR_BALL6(5, true, MajorType.REGULAR),
    REGULAR_BALL7(6, true, MajorType.REGULAR),
    REGULAR_BALL8(7, true, MajorType.REGULAR),

    RANDOM_REGULAR(17, true, MajorType.EDITOR_ONLY),

    WALL_BALL(18, true, MajorType.SPECIAL),
    BOMB_BALL(19, true, MajorType.SPECIAL),
    SPIKY_BALL(20, true, MajorType.SPECIAL),

    FIREBALL(101, false, MajorType.POWERUP),
    COLORBOMB(102, false, MajorType.POWERUP),;

    TileType(int id, boolean placeable, MajorType majorType) {
        this.tileID = id;
        this.isPlaceable = placeable;
        this.majorType = majorType;
        if (isPlaceable) {
            Placeables.list.add(this);
        }
    }

    private int tileID;
    private boolean isPlaceable;
    private MajorType majorType;

    public MajorType getMajorType() {
        return majorType;
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

    public enum MajorType {
        REGULAR,
        EDITOR_ONLY,
        POWERUP,
        SPECIAL
    }

    private static final class Placeables {
        static final List<TileType> list = new ArrayList<>(11);
    }
}
