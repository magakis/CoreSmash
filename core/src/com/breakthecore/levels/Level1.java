package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.UserAccount;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.screens.GameScreen;

public class Level1 extends CampaignLevel{
    public Level1(UserAccount user, RoundEndListener roundEndListener) {
        super(1,user, roundEndListener);
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

        statsManager.setUserAccount(getUser());
        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true,  tm.getTileCount()/2);
        statsManager.setSpecialBallCount(0);
    }

    @Override
    public void update(float delta, TilemapManager tmm) {

    }
}
