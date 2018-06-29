package com.breakthecore.levelbuilder;

import com.badlogic.gdx.math.Vector2;

public class MapSettings {
    Vector2 origin = new Vector2();
    Vector2 offset = new Vector2();
    int minMapSpeed;
    int maxMapSpeed;

    int minSpeed;
    int maxSpeed;
    boolean chained = true;
    boolean rotateCCW;
    int colorCount = 1;

    public Vector2 getOrigin() {
        return origin;
    }

    public Vector2 getOffset() {
        return offset;
    }

    public int getMaxMapSpeed() {
        return maxMapSpeed;
    }

    public int getMinMapSpeed() {
        return minMapSpeed;
    }

    public int getMinSpeed() {
        return minSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getColorCount() {
        return colorCount;
    }

    public boolean isChained() {
        return chained;
    }

    public boolean isRotateCCW() {
        return rotateCCW;
    }

    void reset() {
        origin.setZero();
        offset.setZero();
        minMapSpeed = 0;
        maxMapSpeed = 0;
        minSpeed = 0;
        maxSpeed = 0;
        chained = true;
        rotateCCW = false;
        colorCount = 1;
    }

    void copy(MapSettings from) {
        minMapSpeed = from.minMapSpeed;
        maxMapSpeed = from.maxMapSpeed;
        minSpeed = from.minSpeed;
        maxSpeed = from.maxSpeed;
        rotateCCW = from.rotateCCW;
        colorCount = from.colorCount;
        origin.set(from.origin);
        offset.set(from.offset);
        chained = from.isChained();
    }
}

