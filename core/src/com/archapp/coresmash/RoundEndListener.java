package com.archapp.coresmash;

import com.archapp.coresmash.managers.RoundManager;

public interface RoundEndListener {
    void onRoundEnded(RoundManager.GameStats gameStats);
}
