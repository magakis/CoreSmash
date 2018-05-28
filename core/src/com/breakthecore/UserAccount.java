package com.breakthecore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;


public class UserAccount {
    private String name;
    private int level;
    private int levelProgress;
    private int totalScore;
    private static final int[] expRequiredForLevelTable = new int[100];

    static {
        int baseExp = 500;
        expRequiredForLevelTable[0] = baseExp;
        for (int i = 1; i < expRequiredForLevelTable.length; ++i) {
            expRequiredForLevelTable[i] = (int) Math.pow(expRequiredForLevelTable[i - 1], 1.05f);
        }
    }

    public UserAccount() {
        Preferences prefs = Gdx.app.getPreferences("account");
        name = prefs.getString("username", "_error_");
        totalScore = prefs.getInteger("total_score", 0);

        int scoreLeft = totalScore;
        for (int i = 0; i < expRequiredForLevelTable.length; ++i) {
            if (scoreLeft < expRequiredForLevelTable[i]) {
                level = i + 1;
                levelProgress = scoreLeft;
                break;
            }
            scoreLeft -= expRequiredForLevelTable[i];
        }
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String name) {
        this.name = name;
        Gdx.app.getPreferences("account").putString("username", name);
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void saveScore(int score) {
        totalScore += score;
        levelProgress += score;
        if (levelProgress >= expRequiredForLevelTable[level-1]) {
            levelProgress -= expRequiredForLevelTable[level-1];
            ++level;
        }

        Preferences prefs = Gdx.app.getPreferences("account");
        prefs.putInteger("total_score", totalScore);
        prefs.flush();
    }

    public int getLevel() {
        return level;
    }

    public int getLevelProgress() {
        return levelProgress;
    }

    public int getExpForNextLevel() {
        return expRequiredForLevelTable[level - 1];
    }
}