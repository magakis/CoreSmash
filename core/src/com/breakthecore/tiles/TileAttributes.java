package com.breakthecore.tiles;

public class TileAttributes {
    private static final Builder builder = new Builder();

    private final int ID;
    private final TileType tileType;
    private final boolean isPlaceable;

    private TileAttributes(Builder builder) {
        if (builder.tileType == null || builder.ID < 0)
            throw new IllegalStateException("Unfinished Attributes ID:" + builder.ID + ", Type:" + builder.tileType);

        tileType = builder.tileType;
        isPlaceable = builder.isPlaceable;
        ID = builder.ID;
    }

    public static Builder getBuilder() {
        return builder.reset();
    }

    public TileType getTileType() {
        return tileType;
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
        private boolean isPlaceable;

        public Builder() {
        }

        public Builder setType(TileType type) {
            this.tileType = type;
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

        public TileAttributes build() {
            if (ID < 0) throw new IllegalStateException("Invalid ID (" + ID + ")");
            return new TileAttributes(this);
        }

        public Builder reset() {
            tileType = null;
            isPlaceable = false;
            ID = -1;
            return this;
        }
    }
}
