package com.coresmash.levelbuilder;

public class LevelSettings {
    public int lives;
    public int moves;
    public int time;
    public int ballSpeed = 5;
    public int launcherSize = 1;
    public float launcherCooldown;

    void copy(LevelSettings from) {
        lives = from.lives;
        moves = from.moves;
        time = from.time;
        ballSpeed = from.ballSpeed;
        launcherSize = from.launcherSize;
        launcherCooldown = from.launcherCooldown;
    }
}
