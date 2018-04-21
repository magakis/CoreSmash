package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.managers.StatsManager;

public abstract class CampaignLevel implements Level {
    private RoundEndListener roundEndListener;
    private int idLevel;

    public CampaignLevel(int level, RoundEndListener roundEndListener) {
        this.roundEndListener = roundEndListener;
        idLevel = level;
    }

    @Override
    public void end(boolean roundWon, StatsManager statsManager) {
        roundEndListener.onRoundEnded(roundWon);
    }

    @Override
    public int getLevelNumber() {
        return idLevel;
    }
}
