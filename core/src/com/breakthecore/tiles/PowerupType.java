package com.breakthecore.tiles;

public enum PowerupType {
    FIREBALL(101),
    COLORBOMB(102);

    private int id;

    PowerupType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
