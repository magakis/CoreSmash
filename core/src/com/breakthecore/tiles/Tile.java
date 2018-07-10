package com.breakthecore.tiles;

public class Tile {
    final private TileType type;

    public Tile(TileType type) {
        this.type = type;
    }

    public int getID() {
        return type.getID();
    }

    public TileType getTileType() {
        return type;
    }
}
