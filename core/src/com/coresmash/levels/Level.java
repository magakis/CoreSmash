package com.coresmash.levels;

/**
 * Level is the interface used to manipulate the GameScreen for the needs of the different game levels.
 */

/* If StatManager forces a UserAccount in the future, the Level interface should probably force that too? */
public abstract class Level {
    public abstract void initialize(com.coresmash.GameController gameController);

    public abstract void update(float delta, com.coresmash.tilemap.TilemapManager tilemapManager);

    public abstract void end(com.coresmash.managers.StatsManager.GameStats stats);

    public abstract int getLevelNumber();
}
