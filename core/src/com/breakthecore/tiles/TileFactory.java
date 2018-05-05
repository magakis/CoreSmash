package com.breakthecore.tiles;

public class TileFactory {

    /* Disable Instantiation of this class */
    private TileFactory() {
    }

    public static Tile getTileFromID(int id) {
        TileType type = TileDictionary.getTypeOf(id);

        switch (type) {
            case REGULAR:
                return new RegularTile(id);
            case RANDOM_REGULAR:
                return new RandomTile();
            default:
                return null;
        }
    }

}
