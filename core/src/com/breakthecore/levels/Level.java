package com.breakthecore.levels;

import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.tilemap.TilemapManager;

/**
 * Level is the interface used to manipulate the GameScreen for the needs of the different game levels.
 */

/* If StatManager forces a UserAccount in the future, the Level interface should probably force that too? */
public interface Level {
    void initialize(GameScreen.LevelTools levelTools);

    void update(float delta, TilemapManager tilemapManager);

    void end(boolean roundWon, StatsManager statsManager);

    int getLevelNumber();
}
