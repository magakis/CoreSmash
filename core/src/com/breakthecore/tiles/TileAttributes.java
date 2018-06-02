package com.breakthecore.tiles;

public class TileAttributes {
    private static final Builder builder = new Builder();

    private final TileType tileType;
    private final int ID;
    private final boolean isMatchable;
    private final boolean isPlaceable;
    private final boolean isBreakable;

    private TileAttributes(Builder builder) {
        tileType = builder.tileType;
        isMatchable = builder.isMatchable;
        isPlaceable = builder.isPlaceable;
        isBreakable = builder.isBreakable;
        ID = builder.ID;
    }

    public static Builder getBuilder() {
        return builder.reset();
    }

    public TileType getTileType() {
        return tileType;
    }

    public boolean isBreakable() {return isBreakable; }

    public boolean isMatchable() {
        return isMatchable;
    }

    public boolean isPlaceable() {
        return isPlaceable;
    }

    public int getID() {
        return ID;
    }

    public static class Builder {
        private TileType tileType;
        private int ID;
        private boolean isMatchable;
        private boolean isPlaceable;
        private boolean isBreakable = true;

        public Builder() {
        }

        public Builder setTileType(TileType type) {
            this.tileType = type;
            return this;
        }

        public Builder setMatchable(boolean matchable) {
            isMatchable = matchable;
            return this;
        }

        public Builder setPlaceable(boolean placeable) {
            isPlaceable = placeable;
            return this;
        }

        public Builder setID(int id) {
            ID = id;
            return this;
        }

        public Builder setBreakable(boolean breakable) {
            isBreakable = breakable;
            return this;
        }

        public TileAttributes build() {
            if (ID < 0) throw new IllegalStateException("Invalid ID (" + ID + ")");
            return new TileAttributes(this);
        }

        public Builder reset() {
            tileType = null;
            isMatchable = false;
            isBreakable = true;
            isPlaceable = false;
            ID = -1;
            return this;
        }
    }
}
