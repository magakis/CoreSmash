package com.breakthecore.tiles;

public class Tile {
    final private int ballID;
    final private TileAttributes attributes;

    public Tile(int id) {
        ballID = id;
        attributes = TileIndex.get().getAttributesFor(id);
    }

    public int getID() {
        return ballID;
    }

    public TileAttributes getAttributes() {
        return attributes;
    }
}
