package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.Tilemap;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.screens.GameScreen;

public class Level1 extends CampaignLevel implements Level {

    public Level1(RoundEndListener roundEndListener) {
        super(1,roundEndListener);
    }

    @Override
    public void initialize(StatsManager statsManager, TilemapManager tilemapManager, MovingTileManager movingTileManager) {
        Tilemap tm;
        TilemapManager.TilemapGenerator tilemapGenerator = tilemapManager.getTilemapGenerator();
        tilemapGenerator.setColorCount(5);
        tilemapManager.init(1);

        tm = tilemapManager.getTilemap(0);
        tm.setMinMaxSpeed(40, 70);
        tm.setAutoRotation(true);
        tilemapGenerator.generateRadius(tm, 3);
        tilemapGenerator.reduceColorMatches(tm, 3, 2);
        tilemapGenerator.balanceColorAmounts(tm);
        tilemapGenerator.reduceCenterTileColorMatch(tm, 2);
        tm.initialized();

        movingTileManager.setAutoEject(false);
        movingTileManager.setDefaultBallSpeed(15);
        movingTileManager.setColorCount(5);
        movingTileManager.enableControlledBallGeneration(tilemapManager);
        movingTileManager.initLauncher(3);

        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true,  tm.getTileCount()/2);
        statsManager.setSpecialBallCount(0);
    }

    @Override
    public void update(float delta, TilemapManager tmm) {

    }
}
