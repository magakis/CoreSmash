package com.breakthecore.levelbuilder;

public class MapSettings {
    int minSpeed;
    int maxSpeed;
    boolean rotateCCW;
    int colorCount = 1;

    public int getMinSpeed() {
        return minSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getColorCount() {
        return colorCount;
    }

    public boolean isRotateCCW() {
        return rotateCCW;
    }

    void copy(MapSettings from) {
        minSpeed = from.minSpeed;
        maxSpeed = from.maxSpeed;
        rotateCCW = from.rotateCCW;
        colorCount = from.colorCount;
    }
}

