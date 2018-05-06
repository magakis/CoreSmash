package com.breakthecore.levels;

import com.breakthecore.Launcher;
import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;

public class Level3 extends CampaignLevel {
    public Level3(UserAccount user, RoundEndListener roundEndListener) {
        super(3, user, roundEndListener);
    }

    @Override
    public void initialize(GameScreen.LevelTools levelTools) {
        TilemapManager tilemapManager = levelTools.tilemapManager;
        MovingBallManager movingBallManager = levelTools.movingBallManager;
        StatsManager statsManager = levelTools.statsManager;
        Launcher launcher = levelTools.launcher;


        TilemapBuilder builder = tilemapManager.newMap();

        builder.setColorCount(5)
                .loadMapFromFile("level3")
                .reduceColorMatches(2, 2)
                .balanceColorAmounts()
                .forceEachColorOnEveryRadius()
                .reduceCenterTileColorMatch(2, false)
                .setMinMaxRotationSpeed(40,70)
                .build();

        movingBallManager.setDefaultBallSpeed(15);
        launcher.setAutoEject(false);
        launcher.setLauncherSize(3);

        statsManager.setUserAccount(getUser());
        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true,  tilemapManager.getTotalTileCount()/4+4);
        statsManager.setSpecialBallCount(0);

    }

    @Override
    public void update(float delta, TilemapManager tilemapManager) {

    }
}
