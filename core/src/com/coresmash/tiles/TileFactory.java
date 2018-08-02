package com.coresmash.tiles;

public class TileFactory {

    /* Disable Instantiation of this class */
    private TileFactory() {
    }

    public static Tile createTile(TileType type) {
        switch (type) {
            case REGULAR_BALL1:
                return new RegularTile(type);
            case REGULAR_BALL2:
                return new RegularTile(type);
            case REGULAR_BALL3:
                return new RegularTile(type);
            case REGULAR_BALL4:
                return new RegularTile(type);
            case REGULAR_BALL5:
                return new RegularTile(type);
            case REGULAR_BALL6:
                return new RegularTile(type);
            case REGULAR_BALL7:
                return new RegularTile(type);
            case REGULAR_BALL8:
                return new RegularTile(type);
            case RANDOM_REGULAR:
                return new com.coresmash.tiles.RandomTile(type);
            case WALL_BALL:
                return new com.coresmash.tiles.WallBall(type);
            case SPIKY_BALL:
                return new SpikyBall(type);
            case FIREBALL:
                return new com.coresmash.tiles.FireBall(type);
            case COLORBOMB:
                return new com.coresmash.tiles.ColorBomb(type);
        }
        throw new RuntimeException("Not Implemented Tile!(" + type.name() + ")");
    }

    public static Tile getTileFromID(int id) {
        return createTile(TileType.getTileTypeFromID(id));
    }
}
