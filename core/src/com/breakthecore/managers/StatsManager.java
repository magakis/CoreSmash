package com.breakthecore.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.breakthecore.Launcher;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.Observer;
import com.breakthecore.tiles.TileType.PowerupType;

import java.util.Random;

public class StatsManager extends Observable implements Observer {
    private Random rand = new Random();
    private PowerupCase powerupCase;
    private GameStats gameStats;

    private boolean isGameActive;
    private boolean isDebugEnabled;
    private int ballsDestroyedThisFrame;


    public StatsManager() {
        powerupCase = new PowerupCase();
        gameStats = new GameStats();
    }

    public void start() {
        isGameActive = true;
    }

    public void update(float delta) {
        if (gameStats.isTimeEnabled) {
            gameStats.timeLeft -= delta;
        }

        if (ballsDestroyedThisFrame != 0) {
            int scoreGained;
            int multiplier = 5;

            switch (ballsDestroyedThisFrame) {
                case 1:
                case 2:
                    break;
                case 3:
                    multiplier = 10;
                    break;
                case 4:
                    multiplier = 12;
                    break;
                default:
                    multiplier = 15;
                    break;
            }
            scoreGained = (ballsDestroyedThisFrame * multiplier);
            gameStats.totalScore += scoreGained;
            notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, scoreGained);

            if (gameStats.isLivesEnabled) {
                float chanceToGainLife = ((ballsDestroyedThisFrame * ballsDestroyedThisFrame) / 9.f) / 100.f; // random algorithm I came up with
                if (rand.nextFloat() < chanceToGainLife) {
                    ++gameStats.livesLeft;
                    notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
                }
            }

            ballsDestroyedThisFrame = 0;
        }
    }

    /* Q: Should reset be private and provide only a function that resets but requires a user? */
    public void reset() {
        powerupCase.reset();
        gameStats.reset();

        ballsDestroyedThisFrame = 0;
        isDebugEnabled = false;
    }

    public boolean checkEndingConditions(MovingBallManager ballManager) {
        if (!isGameActive) {
            return true;
        }

        if (gameStats.isTimeEnabled && gameStats.timeLeft <= 0) {
            isGameActive = false;
            return true;
        }
        if (gameStats.isLivesEnabled && gameStats.livesLeft == 0) {
            isGameActive = false;
            return true;
        }
        if (gameStats.isMovesEnabled && gameStats.movesLeft == 0 && !ballManager.hasActiveBalls()) {
            isGameActive = false;
            return true;
        }
        return false;
    }

    public int getLevel() {
        return gameStats.level;
    }

    public void debug() {
        isDebugEnabled = true;
    }

    public void setLevel(int lvl) {
        gameStats.level = lvl;
        Preferences prefs = Gdx.app.getPreferences("account");
        gameStats.targetScore = prefs.getInteger("level" + lvl, 0);
    }

    public boolean getRoundOutcome() {
        if (isGameActive) throw new IllegalStateException("The game is still running?!");
        return gameStats.isRoundWon;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public void stopGame() {
        isGameActive = false;
    }

    public int getTargetScore() {
        return gameStats.targetScore;
    }

    public int getScore() {
        return gameStats.totalScore;
    }

    public int getLives() {
        return gameStats.livesLeft;
    }

    public int getMoves() {
        return gameStats.movesLeft;
    }

    public float getTime() {
        return gameStats.timeLeft;
    }

    public PowerupType[] getEnabledPowerups() {
        return powerupCase.getEnabledPowerups();
    }

    public int getPowerupUsages(PowerupType type) {
        return powerupCase.findSlot(type).amount;
    }

    public int getEnabledPowerupsCount() {
        return powerupCase.enabledSlots;
    }

    public void enablePowerup(PowerupType type, int amount) {
        powerupCase.putPowerup(type, amount);
    }

    public boolean isMovesEnabled() {
        return gameStats.isMovesEnabled;
    }

    public boolean isLivesEnabled() {
        return gameStats.isLivesEnabled;
    }

    public boolean isTimeEnabled() {
        return gameStats.isTimeEnabled;
    }

    public void loseLife() {
        if (gameStats.isLivesEnabled) {
            --gameStats.livesLeft;
            notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
        }
    }

    public void setLives(int lives) {
        gameStats.livesLeft = lives < 0 ? 0 : lives;
        gameStats.isLivesEnabled = lives != 0;
    }

    public void setTime(int time) {
        gameStats.timeLeft = time < 0 ? 0 : time;
        gameStats.isTimeEnabled = time != 0;
    }

    public void setMoves(int moves) {
        gameStats.movesLeft = moves < 0 ? 0 : moves;
        gameStats.isMovesEnabled = moves != 0;
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public boolean consumePowerup(PowerupType type, Launcher launcher) {
        if (!launcher.isLoadedWithSpecial()) {
            int id = powerupCase.consumePowerup(type);
            launcher.insertSpecialTile(id);
            return true;
        }
        return false;
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED:
                gameStats.isRoundWon = true;
                isGameActive = false;
                break;

            case NOTIFICATION_TYPE_TILE_DESTROYED:
                ++ballsDestroyedThisFrame;
                break;

            case NO_COLOR_MATCH:
                if (gameStats.isLivesEnabled) {
                    --gameStats.livesLeft;
                    notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
                }
                break;

            case BALL_LAUNCHED:
                if (gameStats.isMovesEnabled) {
                    --gameStats.movesLeft;
                    notifyObservers(NotificationType.MOVES_AMOUNT_CHANGED, null);
                }
                notifyObservers(NotificationType.BALL_LAUNCHED, null);
                break;
        }
    }

    private static class PowerupCase {
        private static int SLOT_COUNT = 3;
        private PowerupSlot[] slots;
        private PowerupType[] enabledPowerups;
        private int enabledSlots;

        PowerupCase() {
            slots = new PowerupSlot[SLOT_COUNT];
            for (int i = 0; i < slots.length; ++i) {
                slots[i] = new PowerupSlot();
            }
            enabledPowerups = new PowerupType[SLOT_COUNT];
        }

        PowerupType[] getEnabledPowerups() {
            for (int i = 0; i < enabledPowerups.length; ++i) {
                enabledPowerups[i] = slots[i].type;
            }
            return enabledPowerups;
        }

        void putPowerup(PowerupType type, int amount) {
            if (enabledSlots == slots.length) throw new RuntimeException("Not enough slots!");
            slots[enabledSlots].put(type, amount);
            ++enabledSlots;
        }

        int consumePowerup(PowerupType type) {
            PowerupSlot slot = findSlot(type);
            if (slot.amount < 1)
                throw new RuntimeException("No amount left to consume for " + type + "(" + slot.amount + ")");

            --slot.amount;
            return slot.powerupId;
        }

        void reset() {
            enabledSlots = 0;
            for (PowerupSlot slot : slots) {
                slot.reset();
            }
        }

        private PowerupSlot findSlot(PowerupType type) {
            for (PowerupSlot slot : slots) {
                if (slot.type == type) {
                    return slot;
                }
            }
            throw new RuntimeException("No slot found for the specified PowerupType: " + type);
        }

        private static class PowerupSlot {
            PowerupType type;
            int powerupId;
            int amount;

            void put(PowerupType type, int amount) {
                this.type = type;
                this.amount = amount;
                switch (type) {
                    case FIREBALL:
                        powerupId = 101;
                        break;
                    case COLORBOMB:
                        powerupId = 102;
                        break;
                    default:
                        throw new RuntimeException("FUCK YOU FOR DESIGNING IT THIS WAY!!!");
                }
            }

            void reset() {
                type = null;
                powerupId = 0;
                amount = 0;
            }
        }

    }

    public static class GameStats {
        private int level;
        private boolean isRoundWon;

        private int minScore;
        private int totalScore;
        private int targetScore;

        private boolean isMovesEnabled;
        private boolean isLivesEnabled;
        private boolean isTimeEnabled;

        private int movesLeft;
        private float timeLeft;
        private int livesLeft;

        private void reset() {
            level = -1;
            isRoundWon = false;

            minScore = 0;
            totalScore = 0;
            targetScore = 0;

            isLivesEnabled = false;
            isMovesEnabled = false;
            isTimeEnabled = false;

            movesLeft = 0;
            timeLeft = 0;
            livesLeft = 0;
        }

        public boolean isRoundWon() {
            return isRoundWon;
        }

        public int getLevel() {
            return level;
        }

        public int getTargetScore() {
            return targetScore;
        }

        public int getTotalScore() {
            return totalScore;
        }
    }
}

