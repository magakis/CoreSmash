package com.breakthecore.levels;

import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
import com.breakthecore.managers.StatsManager;

public abstract class CampaignLevel implements Level {
    private RoundEndListener roundEndListener;
    private UserAccount user;
    private int idLevel;

    public CampaignLevel(int level,UserAccount user, RoundEndListener roundEndListener) {
        this.roundEndListener = roundEndListener;
        idLevel = level;
        this.user = user;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    @Override
    public void end(StatsManager statsManager) {
        roundEndListener.onRoundEnded(statsManager);
    }

    @Override
    public int getLevelNumber() {
        return idLevel;
    }
}
