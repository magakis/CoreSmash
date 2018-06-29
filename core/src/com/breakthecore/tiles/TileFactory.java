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
            case RANDOM_BALL:
                return new RandomTile(id);
            case WALL_BALL:
                return new WallBall(id);
            case BOMB_BALL:
                return new BombTile(id);
            case SPIKY_BALL:
                return new SpikyBall(id);
            default:
                throw new RuntimeException("Not Implemented Tile!");
        }
    }

}
