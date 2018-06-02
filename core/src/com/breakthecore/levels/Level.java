package com.breakthecore.levels;

import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.tilemap.TilemapManager;

/**
 * Level is the interface used to manipulate the GameScreen for the needs of the different game levels.
 */

/* If StatManager forces a UserAccount in the future, the Level interface should probably force that too? */
public abstract class Level {
    public abstract void initialize(GameScreen.GameScreenController gameScreenController);

    public abstract void update(float delta, TilemapManager tilemapManager);

    public abstract void end(StatsManager statsManager);

    public abstract int getLevelNumber();
}
