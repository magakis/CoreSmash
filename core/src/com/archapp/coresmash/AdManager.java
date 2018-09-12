package com.archapp.coresmash;

public interface AdManager {
    void show();

    void showAdForReward(AdRewardListener listener, VideoAdRewardType type);

    void hide();

    void toggle();

    interface AdRewardListener {
        void reward(String type, int amount);
    }

    enum VideoAdRewardType {
        LOTTERY_COIN,
        EXTRA_LIFE,
        HEART
    }
}
