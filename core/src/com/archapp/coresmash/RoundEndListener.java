package com.archapp.coresmash;

import com.archapp.coresmash.managers.StatsManager;

public interface RoundEndListener {
    void onRoundEnded(StatsManager.GameStats gameStats);
}
