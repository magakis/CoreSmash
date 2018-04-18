package com.breakthecore.levels;

import com.breakthecore.managers.TilemapManager;
import com.breakthecore.screens.GameScreen.LevelSettings;

public interface Level {
    void initialize(LevelSettings levelSettings, TilemapManager tilemapManager);
    void update(float delta, TilemapManager tilemapManager);
    void end(boolean roundWon);
    int getLevelNumber();
}
