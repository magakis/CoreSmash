package com.breakthecore.tiles;

public class TileFactory {

    /* Disable Instantiation of this class */
    private TileFactory() {
    }

    public static Tile getTileFromID(int id) {
        TileType type = TileIndex.get().getAttributesFor(id).getTileType();

        switch (type) {
            case REGULAR:
                return new RegularTile(id);
            case RANDOM:
                return new RandomTile(id);
            case WALL:
                return new WallBall(id);
            case BOMB:
                return new BombTile(id);
            default:
                return null;
        }
    }

}
