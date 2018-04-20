package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.managers.StatsManager;

public abstract class AbstractLevel implements Level {
    private RoundEndListener roundEndListener;

    public AbstractLevel(RoundEndListener roundEndListener) {
        this.roundEndListener = roundEndListener;
    }

    @Override
    public void end(boolean roundWon, StatsManager statsManager) {
        roundEndListener.onRoundEnded(roundWon);
    }
}
