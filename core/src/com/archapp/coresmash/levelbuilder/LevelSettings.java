package com.archapp.coresmash.levelbuilder;

import com.archapp.coresmash.GameTarget;

import java.util.EnumSet;
import java.util.Set;

public class LevelSettings {
    public static final int DEFAULT_BALLSPEED = 15;
    public static final int DEFAULT_LAUNCHER_SIZE = 3;
    public static final Float DEFAULT_LAUNCHER_CD = .16f;

    public int
            livesLimit,
            movesLimit,
            timeLimit,
            ballSpeed = DEFAULT_BALLSPEED,
            launcherSize = DEFAULT_LAUNCHER_SIZE;
    public float launcherCooldown = DEFAULT_LAUNCHER_CD;
    public LevelParser.TargetScore targetScores;
    public Set<GameTarget> targets;

    public LevelSettings() {
        targetScores = new LevelParser.TargetScore();
        targets = EnumSet.noneOf(GameTarget.class);
    }

    void copy(LevelSettings from) {
        livesLimit = from.livesLimit;
        movesLimit = from.movesLimit;
        timeLimit = from.timeLimit;
        ballSpeed = from.ballSpeed;
        launcherSize = from.launcherSize;
        launcherCooldown = from.launcherCooldown;
        targetScores.set(from.targetScores);
    }

    void reset() {
        livesLimit = 0;
        movesLimit = 0;
        timeLimit = 0;
        ballSpeed = DEFAULT_BALLSPEED;
        launcherSize = DEFAULT_LAUNCHER_SIZE;
        launcherCooldown = DEFAULT_LAUNCHER_CD;
        targetScores.reset();
    }
}
