package com.archapp.coresmash.levels;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.RoundEndListener;
import com.archapp.coresmash.UserAccount;
import com.archapp.coresmash.managers.RoundManager.GameStats;

public abstract class CampaignLevel extends Level {
    private RoundEndListener roundEndListener;
    private UserAccount user;
    private int level;

    public CampaignLevel(int level, UserAccount user, RoundEndListener roundEndListener) {
        this.roundEndListener = roundEndListener;
        this.level = level;
        this.user = user;
    }

    @Override
    public void initialize(GameController gameController) {
        gameController.getBehaviourPack().roundManager.setLevel(level, user.getUnlockedLevels());
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
