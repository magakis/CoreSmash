package com.archapp.coresmash;

import com.archapp.coresmash.platform.AdManager;
import com.archapp.coresmash.platform.FeedbackMailHandler;
import com.archapp.coresmash.platform.GoogleGames;
import com.archapp.coresmash.platform.PlayerInfo;

public class PlatformSpecificManager {
    public AdManager adManager;
    public FeedbackMailHandler feedbackMailHandler;
    public GoogleGames googleGames;

    public PlatformSpecificManager() {
        adManager = new AdManager() {
            @Override
            public void show() {

            }

            @Override
            public void showAdForReward(AdRewardListener listener, VideoAdRewardType type) {
                listener.canceled();
            }

            @Override
            public void hide() {

            }

            @Override
            public void toggle() {

            }
        };

        feedbackMailHandler = new FeedbackMailHandler() {
            @Override
            public void createFeedbackMail() {

            }
        };

        googleGames = new GoogleGames() {
            PlayerInfo accountInfo = new PlayerInfo();

            @Override
            public boolean isSignedIn() {
                return false;
            }

            @Override
            public void signIn(OnRequestComplete callback) {
                callback.onComplete(false);
            }

            @Override
            public PlayerInfo getAccountInfo() {
                return accountInfo;
            }

            @Override
            public void addListener(PropertyChangeListener listener) {

            }

            @Override
            public void removeListener(PropertyChangeListener listener) {

            }

            @Override
            public void signOut() {

            }
        };
    }
}
