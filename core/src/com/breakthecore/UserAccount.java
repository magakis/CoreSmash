package com.breakthecore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;


public class UserAccount {
    private String name;
    private int totalScore;
    private int[] scores;
    private float[] dfcltys;

    public UserAccount() {
        scores = new int[5];
        dfcltys = new float[5];
        Preferences prefs = Gdx.app.getPreferences("account");
        name = prefs.getString("username", "_error_");

        for (int i = 0; i < 5; ++i) {
            scores[i] = prefs.getInteger("score" + i, 0);
            dfcltys[i] = prefs.getFloat("dfclty" + i, 0);
        }

        totalScore = prefs.getInteger("total_score", 0);
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

    public int getScore(int i) {
        if (i < 0 || i > 4) throw new ArrayIndexOutOfBoundsException("i is out of bounds. !(1-5)");
        return scores[i];
    }

    public float getScoreDificulty(int i) {
        if (i < 0 || i > 4) throw new ArrayIndexOutOfBoundsException("i is out of bounds. !(1-5)");
        return dfcltys[i];
    }

    public void addScore(int score, float dfclty) {
        Preferences prefs = Gdx.app.getPreferences("account");

        int scoreIndex = 0;
        while (score < scores[scoreIndex]) {
            ++scoreIndex;
        }

        for (int i = 4; i > scoreIndex; --i) {
            scores[i] = scores[i - 1];
            dfcltys[i] = dfcltys[i - 1];
            prefs.putInteger("score" + i, scores[i - 1]);
            prefs.putFloat("dfclty" + i, dfcltys[i - 1]);
        }

        scores[scoreIndex] = score;
        dfcltys[scoreIndex] = dfclty;
        totalScore += score;
        prefs.putInteger("score" + scoreIndex, score);
        prefs.putInteger("total_score", totalScore);
        prefs.putFloat("dfclty" + scoreIndex, dfclty);
        prefs.flush();
    }
}