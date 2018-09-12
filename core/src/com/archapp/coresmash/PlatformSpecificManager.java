package com.archapp.coresmash;

public class PlatformSpecificManager {
    public AdManager adManager;
    public FeedbackMailHandler feedbackMailHandler;

    public PlatformSpecificManager() {
        adManager = new AdManager() {
            @Override
            public void show() {

            }

            @Override
            public void showAdForReward(AdRewardListener listener, VideoAdRewardType type) {

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
    }
}
