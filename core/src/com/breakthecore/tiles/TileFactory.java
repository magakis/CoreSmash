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
            case WALL:
                return new WallBall();
            case BOMB:
                return new BombTile();
            default:
                return null;
        }
    }

}
