package com.breakthecore.tiles;

public class TileAttributes {
    private final TileType tileType;
    private final boolean isMatchable;
    private final boolean isPlaceable;

    private TileAttributes(Builder builder) {
        tileType = builder.tileType;
        isMatchable = builder.isMatchable;
        isPlaceable = builder.isPlaceable;
    }

    public TileType getTileType() {
        return tileType;
    }

    public boolean isMatchable() {
        return isMatchable;
    }

    public boolean isPlaceable() {
        return isPlaceable;
    }

    public static class Builder {
        private TileType tileType;
        private Boolean isMatchable;
        private Boolean isPlaceable;

        public Builder() {
        }

        public Builder setTileType(TileType type) {
            this.tileType = type;
            return this;
        }

        public Builder setMatchable(Boolean matchable) {
            isMatchable = matchable;
            return this;
        }

        public Builder setPlaceable(Boolean placeable) {
            isPlaceable = placeable;
            return this;
        }

        public TileAttributes build() {
            return new TileAttributes(this);
        }

        public void reset() {
            tileType = null;
            isMatchable = false;
            isPlaceable = false;
        }
    }
}
