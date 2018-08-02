package com.coresmash;

import com.coresmash.screens.GameScreen;

public class GameRoundSettings {
    public GameScreen.GameMode gameMode;
    public RoundEndListener onRoundEndListener;
    public int initRadius;
    public float minRotationSpeed;
    public float maxRotationSpeed;
    public int ballSpeed;
    public float launcherCooldown;
    public boolean autoEjectEnabled;
    public boolean autoRotationEnabled;

    public boolean isMovesEnabled;
    public int moveCount;

    public boolean isTimeEnabled;
    public int timeAmount;

    public boolean isLivesEnabled;
    public int livesAmount;

    public void reset() {
        gameMode = null;
        onRoundEndListener = null;

        initRadius = 0;
        minRotationSpeed = 0;
        maxRotationSpeed = 0;
        ballSpeed = 0;
        launcherCooldown = 0;

        isMovesEnabled = false;
        moveCount = 0;
        isTimeEnabled = false;
        timeAmount = 0;
        isLivesEnabled = false;
        livesAmount = 0;

        autoEjectEnabled = false;
        autoRotationEnabled = false;
    }
}