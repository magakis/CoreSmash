package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.tilemap.Tilemap;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;

public class Level3 extends CampaignLevel {
    public Level3(UserAccount user, RoundEndListener roundEndListener) {
        super(3, user, roundEndListener);
    }

    @Override
    public void initialize(StatsManager statsManager, TilemapManager tilemapManager, MovingTileManager movingTileManager) {
        TilemapBuilder builder = tilemapManager.newMap();

        builder.setColorCount(1)
                .loadMapFromFile("level3")
                .balanceColorAmounts()
                .forceEachColorOnEveryRadius()
                .setMinMaxRotationSpeed(40,70)
                .build();

        movingTileManager.setAutoEject(false);
        movingTileManager.enableControlledBallGeneration(tilemapManager);
        movingTileManager.setDefaultBallSpeed(15);
        movingTileManager.initLauncher(3);

        statsManager.setUserAccount(getUser());
        statsManager.setGameMode(GameScreen.GameMode.CLASSIC);
        statsManager.setMoves(true,  20);
        statsManager.setSpecialBallCount(0);

    }

    @Override
    public void update(float delta, TilemapManager tilemapManager) {

    }
}
