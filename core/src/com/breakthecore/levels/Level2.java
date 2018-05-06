package com.breakthecore.levels;

import com.breakthecore.Launcher;
import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.screens.GameScreen;

public class Level2 extends CampaignLevel {
    public Level2(UserAccount user, RoundEndListener roundEndListener) {
        super(2, user, roundEndListener);
    }

    @Override
    public void initialize(GameScreen.LevelTools levelTools) {
        TilemapManager tilemapManager = levelTools.tilemapManager;
        MovingBallManager movingBallManager = levelTools.movingBallManager;
        StatsManager statsManager = levelTools.statsManager;
        Launcher launcher = levelTools.launcher;


        TilemapBuilder builder = tilemapManager.newMap();
        builder.setMinMaxRotationSpeed(40, 70)
                .setColorCount(5)
                .generateStar(2)
                .reduceColorMatches(3, 2)
                .balanceColorAmounts()
                .reduceCenterTileColorMatch(2, false)
                .build();

        movingBallManager.setDefaultBallSpeed(15);
        launcher.setAutoEject(false);
        launcher.setLauncherSize(3);

        statsManager.setUserAccount(getUser());
        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true,  tilemapManager.getTotalTileCount()/3);
        statsManager.setSpecialBallCount(0);

    }

    @Override
    public void update(float delta, TilemapManager tilemapManager) {

    }
}
