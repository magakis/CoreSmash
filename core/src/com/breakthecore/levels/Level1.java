package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.GameScreen.LevelSettings;

public class Level1 implements Level {
    private final RoundEndListener endListener;

    public Level1(RoundEndListener roundEndListener) {
        endListener = roundEndListener;
    }

    @Override
    public void initialize(LevelSettings levelSettings, TilemapManager tilemapManager) {
        initLevelSettings(levelSettings);
        tilemapManager.initTilemapCircle(tilemapManager.getTileMap(), 3);
        levelSettings.moveCount = tilemapManager.getTileMap().getTileCount();
    }

    @Override
    public void update(float delta, TilemapManager tmm) {

    }

    @Override
    public void end(boolean roundWon) {
        endListener.onRoundEnded(roundWon);
        // Round WON
    }

    @Override
    public int getLevelNumber() {
        return 1;
    }

    private void initLevelSettings(LevelSettings levelSettings) {
        levelSettings.minRotationSpeed = 40;
        levelSettings.maxRotationSpeed = 40;
        levelSettings.isMovesEnabled = true;
        levelSettings.moveCount = 50;
        levelSettings.ballSpeed = 15;
        levelSettings.gameMode = GameScreen.GameMode.CLASSIC;
        levelSettings.autoRotationEnabled = true;
    }
}
