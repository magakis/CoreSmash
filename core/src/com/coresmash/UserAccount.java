package com.coresmash;

import com.badlogic.gdx.Gdx;
import com.coresmash.managers.StatsManager;
import com.coresmash.tiles.TileType;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.coresmash.CurrencyType.LOTTERY_COIN;

public class UserAccount {
    private final String PREFS_NAME = "account";

    private String name;
    private HeartManager heartManager;
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
        heartManager = new HeartManager(PREFS_NAME);

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

    public HeartManager getHeartManager() {
        return heartManager;
    }

    public int getAmountOf(CurrencyType type) {
        return currencies.get(type);
    }

    public void giveCurrency(CurrencyType type) {
        giveCurrency(type, 1);
    }

    public void giveCurrency(CurrencyType type, int amount) {
        assert amount > 0;
        currencies.give(type, amount);
    }

    public void consumeCurrency(CurrencyType type) {
        consumeCurrency(type, 1);
    }

    public void consumeCurrency(CurrencyType type, int amount) {
        assert amount > 0;
        if (isCurrencyAvailable(type, amount)) {
            currencies.consume(type, amount);
        } else {
            throw new RuntimeException("Not enough of : " + type.name() + " (" + amount + ")");
        }
    }

    public boolean isCurrencyAvailable(CurrencyType type) {
        return isCurrencyAvailable(type, 1);
    }

    public boolean isCurrencyAvailable(CurrencyType type, int amount) {
        return currencies.get(type) >= amount;
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
            heartManager.restoreHeart();

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
            currencies.put(LOTTERY_COIN, new PersistentInt("currency_lottery_coins", prefsName));
        }

        public void give(CurrencyType type, int amount) {
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

    public static class HeartManager {
        private static int NEW_HEART_INTERVAL = 8 * 60 * 1000; // Millis
        private static int MAX_HEARTS = 5;

        private PersistentInt hearts;
        private PersistentLong timeForNextHeart;
        private PropertyChangeListener changeListener;

        public HeartManager(String prefsName) {
            hearts = new PersistentInt("hearts", prefsName, MAX_HEARTS);
            timeForNextHeart = new PersistentLong("nextLife", prefsName);
            checkTimeForHeart();
        }

        public long getTimeForNextHeart() {
            long dif = timeForNextHeart.getValue() - System.currentTimeMillis();
            return dif < 0 ? 0 : dif;
        }

        public void setChangeListener(PropertyChangeListener changeListener) {
            this.changeListener = changeListener;
        }

        public boolean isFull() {
            return hearts.getValue() == MAX_HEARTS;
        }

        public void restoreHeart() {
            int availableHearts = hearts.getValue();
            if (availableHearts < MAX_HEARTS) {
                hearts.addAmount(1);
                if (changeListener != null) {
                    changeListener.propertyChange(new PropertyChangeEvent(this, "hearts", availableHearts, hearts.getValue()));
                }
            }
        }

        public void restoreToFull() {
            int availableHearts = hearts.getValue();
            if (availableHearts != MAX_HEARTS) {
                hearts.setValue(MAX_HEARTS);
                if (changeListener != null) {
                    changeListener.propertyChange(new PropertyChangeEvent(this, "hearts", availableHearts, MAX_HEARTS));
                }
            }
        }

        public int getHearts() {
            return hearts.getValue();
        }

        public void consumeHeart() {
            int availableHearts = hearts.getValue();
            if (availableHearts == 0)
                throw new RuntimeException("No available heartManager!: " + availableHearts);

            if (availableHearts == MAX_HEARTS) {
                timeForNextHeart.setValue(System.currentTimeMillis() + NEW_HEART_INTERVAL);
            }
            hearts.subAmount(1);
            if (changeListener != null) {
                changeListener.propertyChange(new PropertyChangeEvent(this, "hearts", availableHearts, hearts.getValue()));
            }
        }

        public void checkTimeForHeart() {
            if (hearts.getValue() == MAX_HEARTS) return;

            long currentTime = System.currentTimeMillis();
            // If dif < 0 : It's the time left for life
            // if dif > 0 : It's the extra time
            long dif = currentTime - timeForNextHeart.getValue();

            if (dif >= 0) {
                int generatedHearts = (int) (1 + dif / NEW_HEART_INTERVAL);
                int availableHearts = MAX_HEARTS - hearts.getValue();

                if (generatedHearts >= availableHearts) {
                    hearts.setValue(MAX_HEARTS);
                } else {
                    hearts.addAmount(generatedHearts);
                    timeForNextHeart.addAmount(generatedHearts * NEW_HEART_INTERVAL);
                }

                if (changeListener != null) {
                    changeListener.propertyChange(new PropertyChangeEvent(this, "hearts", availableHearts, hearts.getValue()));
                }
            }
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