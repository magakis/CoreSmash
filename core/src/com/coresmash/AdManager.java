package com.coresmash;

public interface AdManager {
    void show();

    void showAdForReward(AdRewardListener listener);

    void hide();

    void toggle();

    interface AdRewardListener {
        void reward(String type, int amount);
    }
}
