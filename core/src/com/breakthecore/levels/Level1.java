package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.Tilemap;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.screens.GameScreen.GameMode;
import com.breakthecore.screens.GameScreen.LevelSettings;

public class Level1 extends AbstractLevel implements Level {

    public Level1(RoundEndListener roundEndListener) {
        super(roundEndListener);
    }

    @Override
    public void initialize(LevelSettings levelSettings, TilemapManager tilemapManager, MovingTileManager movingTileManager) {
        tilemapManager.init(1);
        tilemapManager.initTilemapRadius(tilemapManager.getTilemap(0), 3);
        Tilemap tm = tilemapManager.getTilemap(0);

        tm.setAutoRotation(true);
        tm.setMinMaxSpeed(40, 40);
        tm.setInitialized(true);

        levelSettings.ballSpeed = 15;
        levelSettings.enableMoves(tilemapManager.getTilemap(0).getTileCount());
    }

    @Override
    public void update(float delta, TilemapManager tmm) {

    }

    @Override
    public void end(boolean roundWon, StatsManager statsManager) {
        super.end(roundWon, statsManager);
    }

    @Override
    public int getLevelNumber() {
        return 1;
    }

}
