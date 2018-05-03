package com.breakthecore;

import com.breakthecore.managers.StatsManager;

public interface RoundEndListener {
    void onRoundEnded(boolean result, StatsManager statsManager);
}
