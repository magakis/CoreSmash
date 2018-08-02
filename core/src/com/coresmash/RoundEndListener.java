package com.coresmash;

import com.coresmash.managers.StatsManager;

public interface RoundEndListener {
    void onRoundEnded(StatsManager.GameStats gameStats);
}
