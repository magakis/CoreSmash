package com.coresmash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.coresmash.managers.StatsManager;
import com.coresmash.tiles.TileType;

import java.util.HashMap;
import java.util.Map;


public class UserAccount {
    private String name;
    private int lotteryCoins;
    private PowerupManager powerupsAvailable;
    private int unlockedLevels;
    private int userLevel;
    private int expProgress;
    private int totalScore;
    private static final int[] expTable = new int[100];

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
        lotteryCoins = prefs.getInteger("lottery_coins");

        int scoreLeft = totalScore;
        for (int i = 0; i < expTable.length; ++i) {
            if (scoreLeft < expTable[i]) {
                userLevel = i + 1;
                expProgress = scoreLeft;
                break;
            }
            scoreLeft -= expTable[i];
        }
        powerupsAvailable = new PowerupManager(prefs);
    }

    public int getLotteryCoins() {
        return lotteryCoins;
    }

    public boolean consumeLotteryCoin() {
        if (lotteryCoins > 0) {
            --lotteryCoins;
            Gdx.app.getPreferences("account").putInteger("lottery_coins", lotteryCoins).flush();
            return true;
        }
        return false;
    }

    public void addLotteryCoins(int amount) {
        assert amount > 0;
        lotteryCoins += amount;
        Gdx.app.getPreferences("account").putInteger("lottery_coins", lotteryCoins).flush();
    }

    public PowerupManager getSpecialBallsAvailable() {
        return powerupsAvailable;
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

    public void saveStats(StatsManager.GameStats stats) {
        int score = stats.getTotalScore();
        if (stats.isRoundWon()) { // WON
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
                    saveScore(score / 3);
                }
            } else {
                saveScore(stats.getTotalScore() / 5);
            }

            prefs.flush();
        } else { // LOST
            saveScore(stats.getTotalScore() / 10);
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

    public void addPowerup(TileType.PowerupType type, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Illegal amount: " + amount);

        powerupsAvailable.addPowerup(type, amount);
    }

    public void consumePowerup(TileType.PowerupType type) {
        powerupsAvailable.consumePowerup(type);
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

    // ============| POWERUP-MANAGER |============
    public static class PowerupManager {
        private Map<TileType.PowerupType, PowerupAmount> powerups;

        private PowerupManager(Preferences prefs) {
            powerups = new HashMap<>();
            for (TileType.PowerupType type : TileType.PowerupType.values()) {
                powerups.put(type, new PowerupAmount(prefs.getInteger(type.name())));
            }
        }

        public int getAmountOf(TileType.PowerupType type) {
            return powerups.get(type).amount;

        }

        public void consumePowerup(TileType.PowerupType type) {
            PowerupAmount amount = powerups.get(type);
            --amount.amount;
            Gdx.app.getPreferences("account")
                    .putInteger(type.name(), amount.amount)
                    .flush();
        }

        public void addPowerup(TileType.PowerupType type, int amount) {
            PowerupAmount pa = powerups.get(type);
            pa.amount += amount;
            Gdx.app.getPreferences("account")
                    .putInteger(type.name(), pa.amount)
                    .flush();
        }

        private static class PowerupAmount {
            int amount;

            PowerupAmount(int amount) {
                this.amount = amount;
            }
        }
    }
}