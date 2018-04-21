package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.Tilemap;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.screens.GameScreen;

public class Level2 extends CampaignLevel {
    public Level2(RoundEndListener roundEndListener) {
        super(2, roundEndListener);
    }

    @Override
    public void initialize(StatsManager statsManager, TilemapManager tilemapManager, MovingTileManager movingTileManager) {
        Tilemap tm;
        TilemapManager.TilemapGenerator tilemapGenerator = tilemapManager.getTilemapGenerator();
        tilemapManager.init(1);

        tm = tilemapManager.getTilemap(0);
        tm.setMinMaxSpeed(40, 70);
        tm.setAutoRotation(true);
        tilemapGenerator.generateSquareSkewed(tm, 3, false);
        tilemapGenerator.generateSquareSkewed(tm, 3, true);
        tilemapGenerator.generateDiamond(tm, 3);
        tilemapGenerator.balanceTilemap(tm);
        tm.initialized();

        movingTileManager.setAutoEject(false);
        movingTileManager.setDefaultBallSpeed(15);

        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true,  tm.getTileCount());
        statsManager.setSpecialBallCount(0);

    }

    @Override
    public void update(float delta, TilemapManager tilemapManager) {

    }
}
