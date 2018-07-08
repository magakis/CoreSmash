package com.breakthecore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.tiles.PowerupType;


public class UserAccount {
    private String name;
    private SpecialBallsAvailable ballsAvailable;
    private int unlockedLevels;
    private int userLevel;
    private int expProgress;
    private int totalScore;
    private static final int[] expTable = new int[100];

    private String SPECIAL_FIREBALL = "special_fireball";

    static {
        int baseExp = 500;
        expTable[0] = baseExp;
        for (int i = 1; i < expTable.length; ++i) {
            expTable[i] = (int) Math.pow(expTable[i - 1], 1.05f);
        }
    }

    public UserAccount() {
        Preferences prefs = Gdx.app.getPreferences("account");
        name = prefs.getString("username", "_error_");
        unlockedLevels = prefs.getInteger("unlocked_levels");
        totalScore = prefs.getInteger("total_score");

        int scoreLeft = totalScore;
        for (int i = 0; i < expTable.length; ++i) {
            if (scoreLeft < expTable[i]) {
                userLevel = i + 1;
                expProgress = scoreLeft;
                break;
            }
            scoreLeft -= expTable[i];
        }
        ballsAvailable = new SpecialBallsAvailable(prefs);
    }

    public SpecialBallsAvailable getSpecialBallsAvailable() {
        return ballsAvailable;
    }

    public int getUnlockedLevels() {
        return unlockedLevels;
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

    public void saveStats(StatsManager stats) {
        int score = stats.getScore();
        if (stats.getRoundOutcome()) { // WON
            Preferences prefs = Gdx.app.getPreferences("account");

            boolean levelUnlocked = false;
            if (stats.getLevel() > unlockedLevels) {
                prefs.putInteger("unlocked_levels", stats.getLevel());
                ++unlockedLevels;
                levelUnlocked = true;
            }

            if (score > stats.getTargetScore()) {
                prefs.putInteger("level" + stats.getLevel(), score);
                if (levelUnlocked) {
                    saveScore(score);
                } else {
                    saveScore(score / 5);
                }
            } else {
                saveScore(stats.getScore() / 10);
            }

            prefs.flush();
        } else { // LOST
            saveScore(stats.getScore() / 15);
        }
    }

    public int getLevel() {
        return userLevel;
    }

    public int getXPProgress() {
        return expProgress;
    }

    public int getExpForNextLevel() {
        return expTable[userLevel - 1];
    }

    public void addPowerup(PowerupType type, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Illegal amount: " + amount);

        Preferences prefs = Gdx.app.getPreferences("account");
        switch (type) {
            case FIREBALL:
                ballsAvailable.powerup.fireball += amount;
                prefs.putInteger(SPECIAL_FIREBALL, prefs.getInteger(SPECIAL_FIREBALL) + amount);
                break;
            default:
                throw new RuntimeException("Not implemented PowerUp: " + type);
        }
        prefs.flush();
    }

    public void consumePowerup(PowerupType type) {
        Preferences prefs = Gdx.app.getPreferences("account");
        switch (type) {
            case FIREBALL:
                --ballsAvailable.powerup.fireball;
                prefs.putInteger(SPECIAL_FIREBALL, prefs.getInteger(SPECIAL_FIREBALL) - 1);
                break;
            default:
                throw new RuntimeException("Not implemented PowerUp: " + type);
        }
        prefs.flush();
    }

    private void saveScore(int score) {
        totalScore += score;
        expProgress += score;
        if (expProgress >= expTable[userLevel - 1]) {
            expProgress -= expTable[userLevel - 1];
            ++userLevel;
        }

        Preferences prefs = Gdx.app.getPreferences("account");
        prefs.putInteger("total_score", totalScore);
        prefs.flush();
    }

    public static class SpecialBallsAvailable {
        private PowerupCountGroup powerup;

        private SpecialBallsAvailable(Preferences prefs) {
            powerup = new PowerupCountGroup();
            powerup.fireball = prefs.getInteger("special_fireball");
        }

        public int getAmountOf(PowerupType type) {
            switch (type) {
                case FIREBALL:
                    return powerup.fireball;
                default:
                    throw new RuntimeException("Not implemented PowerUp: " + type);
            }
        }
    }
}