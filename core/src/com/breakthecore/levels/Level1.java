package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.screens.GameScreen;

public class Level1 extends CampaignLevel {
    public Level1(UserAccount user, RoundEndListener roundEndListener) {
        super(1, user, roundEndListener);
    }

    @Override
    public void initialize(StatsManager statsManager, TilemapManager tilemapManager, MovingTileManager movingTileManager) {

        TilemapBuilder builder = tilemapManager.newMap();
        builder.setMinMaxRotationSpeed(40, 70)
                .setColorCount(5)
                .generateRadius(3)
                .reduceColorMatches(3, 2)
                .balanceColorAmounts()
                .reduceCenterTileColorMatch(2, false)
                .build();

        movingTileManager.setAutoEject(false);
        movingTileManager.setDefaultBallSpeed(15);
        movingTileManager.setColorCount(5);
        movingTileManager.enableControlledBallGeneration(tilemapManager);
        movingTileManager.initLauncher(3);

        statsManager.setUserAccount(getUser());
        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true, tilemapManager.getTotalTileCount() / 2);
        statsManager.setSpecialBallCount(0);
    }

    @Override
    public void update(float delta, TilemapManager tmm) {

    }
}
