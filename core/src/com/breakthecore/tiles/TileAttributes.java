package com.breakthecore.tiles;

public class TileAttributes {
    private final TileType tileType;
    private final boolean isMatchable;

    private TileAttributes(Builder builder) {
        tileType = builder.tileType;
        isMatchable = builder.isMatchable;
    }

    public TileType getTileType() {
        return tileType;
    }

    public boolean isMatchable() {
        return isMatchable;
    }

    public static class Builder {
        private TileType tileType;
        private Boolean isMatchable;

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

        public TileAttributes build() {
            return new TileAttributes(this);
        }

        public void reset() {
            tileType = null;
            isMatchable = false;
        }
    }
}
