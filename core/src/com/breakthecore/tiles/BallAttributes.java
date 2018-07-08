package com.breakthecore.tiles;

public class BallAttributes {
    private static final Builder builder = new Builder();

    private final int ID;
    private final TileType tileType;
    private final PowerupType powerupType;
    private final boolean isPlaceable;

    BallAttributes(Builder builder) {
        if (builder.tileType == null || builder.ID < 0)
            throw new IllegalStateException("Unfinished Attributes ID:" + builder.ID + ", Type:" + builder.tileType);

        tileType = builder.tileType;
        isPlaceable = builder.isPlaceable;
        ID = builder.ID;
        powerupType = builder.powerupType;
    }

    public static Builder getBuilder() {
        return builder.reset();
    }

    public TileType getTileType() {
        return tileType;
    }

    public PowerupType getPowerupType() {
        return powerupType;
    }

    public boolean isPlaceable() {
        return isPlaceable;
    }

    public int getID() {
        return ID;
    }

    public static class Builder {
        private TileType tileType;
        private PowerupType powerupType;
        private int ID;
        private boolean isPlaceable;

        Builder() {
        }

        public Builder setPowerupType(PowerupType type) {
            powerupType = type;
            tileType = TileType.POWERUP;
            return this;
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

        public BallAttributes build() {
            if (ID < 0) throw new IllegalStateException("Invalid ID (" + ID + ")");
            if (tileType == null) throw new NullPointerException("TileType can't be null");
            if (powerupType != null && tileType != TileType.POWERUP)
                throw new IllegalStateException("Every powerup must have TileType POWERUP");
            return new BallAttributes(this);
        }

        public Builder reset() {
            tileType = null;
            powerupType = null;
            isPlaceable = false;
            ID = -1;
            return this;
        }
    }
}
