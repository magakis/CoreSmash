package com.coresmash;

import com.badlogic.gdx.Gdx;
import com.coresmash.managers.StatsManager;
import com.coresmash.tiles.TileType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.coresmash.CurrencyType.HEARTS;
import static com.coresmash.CurrencyType.LOTTERY_COINS;


public class UserAccount {
    private final String PREFS_NAME = "account";

    private String name;
    private CurrenciesManager currencies;
    private PowerupManager powerups;
    private PersistentInt unlockedLevels;
    private int userLevel;
    private int progressForLevel;
    private PersistentInt totalProgress;
    private static final int[] expTable = new int[100];

    static {
        int baseExp = 500;
        expTable[0] = baseExp;
        for (int i = 1; i < expTable.length; ++i) {
            expTable[i] = (int) Math.pow(expTable[i - 1], 1.05f);
        }
    }

    public UserAccount() {
//        name = prefs.getString("username", "_error_");
        unlockedLevels = new PersistentInt("unlocked_levels", PREFS_NAME, 1);
        totalProgress = new PersistentInt("total_score", PREFS_NAME);

        currencies = new CurrenciesManager(PREFS_NAME);

        int scoreLeft = totalProgress.getValue();
        for (int i = 0; i < expTable.length; ++i) {
            if (scoreLeft < expTable[i]) {
                userLevel = i + 1;
                progressForLevel = scoreLeft;
                break;
            }
            scoreLeft -= expTable[i];
        }
        powerups = new PowerupManager(PREFS_NAME);
    }

    public int getLotteryCoins() {
        return currencies.get(LOTTERY_COINS);
    }

    public int getHearts() {
        return currencies.get(HEARTS);
    }

    public boolean consumeLotteryCoin() {
        if (currencies.get(LOTTERY_COINS) > 0) {
            currencies.consume(LOTTERY_COINS, 1);
            return true;
        }
        return false;
    }

    public void addLotteryCoins(int amount) {
        assert amount > 0;
        currencies.add(LOTTERY_COINS, amount);
    }

    public PowerupManager getSpecialBallsAvailable() {
        return powerups;
    }

    public int getUnlockedLevels() {
        return unlockedLevels.getValue();
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String name) {
        this.name = name;
        Gdx.app.getPreferences("account").putString("username", name);
    }

    public int getTotalProgress() {
        return totalProgress.getValue();
    }

    public void saveStats(StatsManager.GameStats stats) {
        int score = stats.getTotalScore();
        if (stats.isRoundWon()) { // WON

            if (stats.isLevelUnlocked()) {
                unlockedLevels.addAmount(1);
            }

            if (score > stats.getTargetScore()) {
                Gdx.app.getPreferences(PREFS_NAME).putInteger("level" + stats.getActiveLevel(), score).flush();
                if (stats.isLevelUnlocked()) {
                    saveScore(score);
                } else {
                    saveScore(score / 3);
                }
            } else {
                saveScore(stats.getTotalScore() / 5);
            }

        } else { // LOST
            saveScore(stats.getTotalScore() / 10);
        }
    }

    public int getLevel() {
        return userLevel;
    }

    public int getXPProgress() {
        return progressForLevel;
    }

    public int getExpForNextLevel() {
        return expTable[userLevel - 1];
    }

    public void addPowerup(TileType.PowerupType type, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Illegal amount: " + amount);

        powerups.addPowerup(type, amount);
    }

    public void consumePowerup(TileType.PowerupType type) {
        powerups.consumePowerup(type);
    }

    private void saveScore(int score) {
        totalProgress.addAmount(score);
        progressForLevel += score;
        if (progressForLevel >= expTable[userLevel - 1]) {
            progressForLevel -= expTable[userLevel - 1];
            ++userLevel;
        }
    }

    private static class CurrenciesManager {
        private Map<CurrencyType, PersistentInt> currencies;

        public CurrenciesManager(String prefsName) {
            currencies = new EnumMap<>(CurrencyType.class);
            currencies.put(HEARTS, new PersistentInt("currency_hearts", prefsName));
            currencies.put(LOTTERY_COINS, new PersistentInt("currency_lottery_coins", prefsName));
        }

        public void add(CurrencyType type, int amount) {
            assert amount > 0;
            currencies.get(type).addAmount(amount);
        }

        public int get(CurrencyType type) {
            return currencies.get(type).getValue();
        }

        public void consume(CurrencyType type, int amount) {
            currencies.get(type).subAmount(amount);
        }

    }


    // ============| POWERUP-MANAGER |============
    public static class PowerupManager {
        private Map<TileType.PowerupType, PersistentInt> powerups;

        private PowerupManager(String prefsName) {
            powerups = new HashMap<>();
            for (TileType.PowerupType type : TileType.PowerupType.values()) {
                powerups.put(type, new PersistentInt(type.name(), prefsName));
            }
        }

        public int getAmountOf(TileType.PowerupType type) {
            return powerups.get(type).getValue();

        }

        public void consumePowerup(TileType.PowerupType type) {
            PersistentInt power = powerups.get(type);
            if (power.getValue() == 0)
                throw new RuntimeException("Powerup not available! Amount: " + power.getValue());
            power.subAmount(1);
        }

        public void addPowerup(TileType.PowerupType type, int amount) {
            powerups.get(type).addAmount(amount);
        }

    }
}