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
        tilemapManager.setColorCount(5);
        tilemapManager.init(1);

        tm = tilemapManager.getTilemap(0);
        tm.setMinMaxSpeed(40, 70);
        tm.setAutoRotation(true);
        tilemapGenerator.generateStar(tm,2);
        tilemapGenerator.reduceColorMatches(tm, 3, 2);
        tilemapGenerator.reduceCenterTileColorMatch(tm, 2);
        tm.initialized();

        movingTileManager.setAutoEject(false);
        movingTileManager.setColorCount(5);
        movingTileManager.setDefaultBallSpeed(15);
        movingTileManager.initLauncher(3);

        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true,  tm.getTileCount()/3);
        statsManager.setSpecialBallCount(0);

    }

    @Override
    public void update(float delta, TilemapManager tilemapManager) {

    }
}
