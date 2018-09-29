package com.archapp.coresmash.levelbuilder;

public class LevelSettings {
    public static final int DEFAULT_BALLSPEED = 15;
    public static final int DEFAULT_LAUNCHER_SIZE = 3;
    public static final Float DEFAULT_LAUNCHER_CD = .16f;

    public int
            livesLimit,
            movesLimit,
            timeLimit,
            ballSpeed = DEFAULT_BALLSPEED,
            launcherSize = DEFAULT_LAUNCHER_SIZE,
            targetScoreOne,
            targetScoreTwo,
            targetScoreThree;
    public float launcherCooldown = DEFAULT_LAUNCHER_CD;

    void copy(LevelSettings from) {
        livesLimit = from.livesLimit;
        movesLimit = from.movesLimit;
        timeLimit = from.timeLimit;
        ballSpeed = from.ballSpeed;
        launcherSize = from.launcherSize;
        launcherCooldown = from.launcherCooldown;
        targetScoreOne = from.targetScoreOne;
        targetScoreTwo = from.targetScoreTwo;
        targetScoreThree = from.targetScoreThree;
    }

    void reset() {
        livesLimit = 0;
        movesLimit = 0;
        timeLimit = 0;
        ballSpeed = DEFAULT_BALLSPEED;
        launcherSize = DEFAULT_LAUNCHER_SIZE;
        launcherCooldown = DEFAULT_LAUNCHER_CD;
        targetScoreOne = 0;
        targetScoreTwo = 0;
        targetScoreThree = 0;
    }
}
