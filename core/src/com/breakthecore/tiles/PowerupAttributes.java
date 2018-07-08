package com.breakthecore.tiles;

public class PowerupAttributes extends BallAttributes {
    private PowerupAttributes(Builder builder) {
        super(builder);
    }

    public static class Builder extends BallAttributes.Builder {
        PowerupType type;

        void setType(PowerupType type) {

        }
    }
}
