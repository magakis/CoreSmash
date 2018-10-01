package com.archapp.coresmash;

import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.tiles.TileType;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.archapp.coresmash.CurrencyType.GOLD_BAR;
import static com.archapp.coresmash.CurrencyType.LOTTERY_TICKET;
import static com.archapp.coresmash.CurrencyType.SPACE_COINS;

public class UserAccount {
    private static final String PREFS_NAME = "account";

    private final List<PropertyChangeListener> propertyChangeListeners;

    private String name;
    private HeartManager heartManager;
    private CurrencyManager currencies;
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
        propertyChangeListeners = new ArrayList<>();

        unlockedLevels = new PersistentInt("unlocked_levels", PREFS_NAME, 1);
        totalProgress = new PersistentInt("total_score", PREFS_NAME);

        currencies = new CurrencyManager(PREFS_NAME);

        /*
         * I could have also passed the user and generically manipulate the hearts.
         * Passing the Currency makes it more specific
         */
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

    public CurrencyManager getCurrencyManager() {
        return currencies;
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

    public void saveStats(RoundManager.GameStats stats) {
        int score = stats.getTotalScore();
        if (stats.isRoundWon()) { // WON
            heartManager.restoreHeart();

            if (stats.isLevelUnlocked()) {
                unlockedLevels.addAmount(1);
            }

            if (score > stats.getUserHighScore()) {
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

    public int getHighscoreForLevel(int level) {
        return Gdx.app.getPreferences(PREFS_NAME).getInteger("level" + level);
    }

    public int getExpForNextLevel() {
        return expTable[userLevel - 1];
    }

    public void givePowerup(TileType.PowerupType type, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Illegal amount: " + amount);

        powerups.addPowerup(type, amount);
    }

    public void consumePowerup(TileType.PowerupType type) {
        powerups.consumePowerup(type);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!propertyChangeListeners.contains(listener))
            propertyChangeListeners.add(listener);
    }

    private void notifyListeners(String name, Object value) {
        for (PropertyChangeListener listener : propertyChangeListeners) {
            listener.onChange(name, value);
        }
    }

    private void saveScore(int score) {
        totalProgress.addAmount(score);
        progressForLevel += score;
        if (progressForLevel >= expTable[userLevel - 1]) {
            progressForLevel -= expTable[userLevel - 1];
            ++userLevel;
        }
    }

    public class CurrencyManager {
        private Map<CurrencyType, Currency> currencies;

        private CurrencyManager(String prefsName) {
            currencies = new EnumMap<>(CurrencyType.class);
            currencies.put(LOTTERY_TICKET, new Currency("currency_lottery_coins", prefsName, 999_999));
            currencies.put(GOLD_BAR, new Currency("currency_space_gems", prefsName, 999_999));
            currencies.put(SPACE_COINS, new Currency("currency_space_coins", prefsName, 999_999));
        }

        public int getAmountOf(CurrencyType type) {
            return currencies.get(type).value();
        }

        public void giveCurrency(CurrencyType type) {
            giveCurrency(type, 1);
        }

        public void giveCurrency(CurrencyType type, int amount) {
            assert amount > 0;
            currencies.get(type).addAmount(amount);
            notifyListeners(type.name(), getAmountOf(type));
        }

        public void consumeCurrency(CurrencyType type) {
            consumeCurrency(type, 1);
        }

        public void consumeCurrency(CurrencyType type, int amount) {
            assert amount > 0;
            if (isCurrencyAvailable(type, amount)) {
                currencies.get(type).subAmount(amount);
                notifyListeners(type.name(), getAmountOf(type));
            } else {
                throw new RuntimeException("Not enough of : " + type.name() + " (" + amount + ")");
            }
        }

        public boolean isCurrencyAvailable(CurrencyType type) {
            return isCurrencyAvailable(type, 1);
        }

        public boolean isCurrencyAvailable(CurrencyType type, int amount) {
            return getAmountOf(type) >= amount;
        }

        public boolean isFullOf(CurrencyType type) {
            return currencies.get(type).isFull();
        }

        private class Currency {
            private final PersistentInt val;
            private final int maxValue;

            public Currency(String prefKey, String prefsName, int maxValue) {
                val = new PersistentInt(prefKey, prefsName);
                this.maxValue = maxValue;
            }

            public int value() {
                return val.getValue();
            }

            public int getMaxValue() {
                return maxValue;
            }

            public boolean isFull() {
                return value() == maxValue;
            }

            /**
             * WARNING: Doesn't check for integer overflows
             */
            public void addAmount(int amount) {
                if (value() + amount > maxValue)
                    throw new RuntimeException("Operation exceeds max amount: " + (value() + amount) + " > " + maxValue);
                else
                    val.addAmount(amount);
            }

            /**
             * WARNING: Doesn't check for integer overflows
             */
            public void subAmount(int amount) {
                if (value() - amount < 0)
                    throw new RuntimeException("Operation results in negative amount: " + (value() - amount));
                else
                    val.subAmount(amount);
            }
        }
    }

    public static class HeartManager {
        private static int NEW_HEART_INTERVAL = 12 * 60 * 1000; // Millis
        private static int MAX_HEARTS = 5;

        private PersistentInt hearts;
        private PersistentLong timeForNextHeart;

        /* One change listener... if more are required put them in a list */
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
                notifyListeners();
            }
        }

        public void restoreToFull() {
            if (!isFull()) {
                hearts.setValue(MAX_HEARTS);
                notifyListeners();
            }
        }

        public int getHearts() {
            return hearts.getValue();
        }

        public boolean isHeartAvailable() {
            return hearts.getValue() > 0;
        }

        public void consumeHeart() {
            int availableHearts = hearts.getValue();
            if (availableHearts == 0)
                throw new RuntimeException("No available heartManager!: " + availableHearts);

            if (availableHearts == MAX_HEARTS) {
                timeForNextHeart.setValue(System.currentTimeMillis() + NEW_HEART_INTERVAL);
            }

            hearts.subAmount(1);
            notifyListeners();
        }

        public void checkTimeForHeart() {
            if (isFull()) return;

            long currentTime = System.currentTimeMillis();
            // If dif < 0 : It's the timeLimit left for life
            // if dif > 0 : It's the extra timeLimit
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

                notifyListeners();
            }
        }

        private void notifyListeners() {
            if (changeListener != null) {
                changeListener.onChange("HEART", getHearts());
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