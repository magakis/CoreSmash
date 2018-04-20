package com.breakthecore.levels;

import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.screens.GameScreen.LevelSettings;

/**
 * Level is the interface used to manipulate the GameScreen for the needs of the different game levels.
 * I have thought of two ways to implement this interface.
 * <p>
 * One:
 * <p>
 * I make all the variables in {@link LevelSettings} be objects and make each manager hold the values
 * that it cares about. Later, if I want to, I can alter these values through the Level interface and the
 * new values will reach the managers. <i>After thinking about it, simply changing the values is not
 * viable since there might be other values derived from these ones that also need to be updated.
 * <u>Managers have to provide an interface to apply those changes</u>.</i>
 * </p>
 * Two:
 * <p>
 * I remove every bit of "default" logic from the managers and gather all the logic of what happens
 * on every game update in the Level. Then the Level gets full control of how the level behaves
 * which means more flexibility but also more code(?).
 * </p>
 * </p>
 * <p>
 * I choose to implement the first case because I feel that there is too much logic that needs to be
 * manually implemented for each manager in each level. I would rather try to make the interfaces of
 * the managers more configurable than trying to brake everything apart and re-assemble it each time
 * in each Level
 * </p>
 */

public interface Level {
    void initialize(LevelSettings levelSettings, TilemapManager tilemapManager, MovingTileManager movingTileManager);

    void update(float delta, TilemapManager tilemapManager);

    void end(boolean roundWon, StatsManager statsManager);

    int getLevelNumber();
}
