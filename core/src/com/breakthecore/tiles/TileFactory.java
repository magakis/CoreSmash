package com.breakthecore.tiles;

public class TileFactory {

    /* Disable Instantiation of this class */
    private TileFactory() {
    }

    public static Tile getTileFromID(int id) {
        BallAttributes ballAttr = TileIndex.get().getAttributesFor(id);

        switch (ballAttr.getTileType()) {
            case REGULAR:
                return new RegularTile(id);
            case RANDOM_BALL:
                return new RandomTile(id);
            case WALL_BALL:
                return new WallBall(id);
            case SPIKY_BALL:
                return new SpikyBall(id);
            case POWERUP:
                switch (ballAttr.getPowerupType()) {
                    case FIREBALL:
                        return new FireBall(id);
                }
        }

        throw new RuntimeException("Not Implemented Tile!(id:" + id + ", TileType:" + ballAttr.getTileType() + ", PowerupType:" + ballAttr.getPowerupType());
    }

}
