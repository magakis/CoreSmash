package com.coresmash.levels;

import com.coresmash.RoundEndListener;
import com.coresmash.UserAccount;
import com.coresmash.managers.StatsManager.GameStats;

public abstract class CampaignLevel extends Level {
    private RoundEndListener roundEndListener;
    private UserAccount user;
    private int level;

    public CampaignLevel(int level, UserAccount user, RoundEndListener roundEndListener) {
        this.roundEndListener = roundEndListener;
        this.level = level;
        this.user = user;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    @Override
    public void end(GameStats stats) {
        roundEndListener.onRoundEnded(stats);
    }

    @Override
    public int getLevelNumber() {
        return level;
    }
}
