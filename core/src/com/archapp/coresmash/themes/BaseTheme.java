package com.archapp.coresmash.themes;

import com.archapp.coresmash.tiles.TileType;

public class BaseTheme extends AbstractTheme {
    public BaseTheme() {
        setResourcesFor(TileType.REGULAR_BALL1.getID(), "RedBall", null);
        setResourcesFor(TileType.REGULAR_BALL2.getID(), "BlueBall", null);
        setResourcesFor(TileType.REGULAR_BALL3.getID(), "GreenBall", null);
        setResourcesFor(TileType.REGULAR_BALL4.getID(), "PurpleBall", null);
        setResourcesFor(TileType.REGULAR_BALL5.getID(), "OrangeBall", null);
        setResourcesFor(TileType.REGULAR_BALL6.getID(), "PinkBall", null);
        setResourcesFor(TileType.REGULAR_BALL7.getID(), "CyanBall", null);
        setResourcesFor(TileType.REGULAR_BALL8.getID(), "MaroonBall", null);

        setResourcesFor(TileType.RANDOM_REGULAR.getID(), "RandomRegularBall", null);
        setResourcesFor(TileType.WALL_BALL.getID(), "WallBall", null);
        setResourcesFor(TileType.BOMB_BALL.getID(), "BombBall", null);
        setResourcesFor(TileType.SPIKY_BALL.getID(), "SpikyBall", null);
        setResourcesFor(TileType.FIREBALL.getID(), "Fireball", null);
        setResourcesFor(TileType.COLORBOMB.getID(), "ColorBomb", null);
    }
}
