package com.archapp.coresmash.managers;

import com.archapp.coresmash.Launcher;
import com.archapp.coresmash.NotificationType;
import com.archapp.coresmash.Observable;
import com.archapp.coresmash.Observer;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.TileType;
import com.archapp.coresmash.tiles.TileType.PowerupType;
import com.badlogic.gdx.Gdx;

import java.util.Random;

public class RoundManager extends Observable implements Observer {
    private Random rand = new Random();
    private PowerupCase powerupCase;
    private GameStats gameStats;

    private boolean gamePaused;
    private boolean gameTerminated;
    private boolean debugEnabled;
    private int match3Size;
    private int ballsDestroyedThisFrame;
    private int scoreThisFrame;


    public RoundManager() {
        powerupCase = new PowerupCase();
        gameStats = new GameStats();
    }

    public void setLevel(int level, int unlockedLevel) {
        gameStats.activeLevel = level;
        gameStats.unlockedLevel = unlockedLevel;
        gameStats.userHighScore = Gdx.app.getPreferences("account").getInteger("level" + level);

        // Allow only paid livesLimit after level 10
        if (level < 11)
            gameStats.freeSecondLife = true;
    }

    public void start() {
        if ((gameStats.activeLevel == -1 || gameStats.unlockedLevel == -1) && !debugEnabled)
            throw new RuntimeException("Game not initialized properly (Level:" + gameStats.activeLevel + ", Unlocked:" + gameStats.unlockedLevel + ")");

        gameStats.movesEnabled = gameStats.movesLeft > 0;
        gameStats.livesEnabled = gameStats.livesLeft > 0;
        gameStats.timeEnabled = gameStats.timeLeft > 0;

        gamePaused = false;
    }

    public void update(float delta) {
        if (gameStats.timeEnabled) {
            gameStats.timeLeft -= delta;
        }

        if (ballsDestroyedThisFrame != 0) {
            int multiplier = 10;

            switch (match3Size) {
                case 0:
                case 1: // 1 and 2 should never appear
                case 2:
                case 3:
                    break;
                case 4:
                    multiplier = 15;
                    break;
                case 5:
                    multiplier = 30;
                    break;
                default:
                    multiplier = 40;
                    break;
            }

            multiplier += ballsDestroyedThisFrame - match3Size;


            int scoreGained = (ballsDestroyedThisFrame * multiplier) + scoreThisFrame;
            gameStats.totalScore += scoreGained;
            notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, scoreGained);

            checkIfTargetScoreReached();

            if (gameStats.livesEnabled) {
                float chanceToGainLife = ((ballsDestroyedThisFrame * ballsDestroyedThisFrame) / 9.f) / 100.f; // random algorithm I came up with
                if (rand.nextFloat() < chanceToGainLife) {
                    ++gameStats.livesLeft;
                    notifyObservers(NotificationType.LIVES_AMOUNT_CHANGED, null);
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
        match3Size = 0;
        scoreThisFrame = 0;
        debugEnabled = false;
        gameTerminated = false;
        gamePaused = true;
    }

    /**
     * @return true should end the game and false should not
     */
    public boolean checkEndingConditions(MovingBallManager ballManager) {
        if (gameTerminated || gameStats.isRoundWon) {
            if (!gameStats.isRoundWon) {
                if (gameStats.astronautsLeft != 0) {
                    gameStats.reasonOfLoss = ReasonOfLoss.ASTRONAUTS_LEFT;
                } else if (gameStats.totalScore < gameStats.targetScoreOne) {
                    gameStats.reasonOfLoss = ReasonOfLoss.MISSED_TARGET_SCORE;
                } else {
                    gameStats.isRoundWon = true;
                }
            }
            return true;
        }

        if (gameStats.timeEnabled && gameStats.timeLeft <= 0) {
            gameStats.reasonOfLoss = ReasonOfLoss.OUT_OF_TIME;
            return true;
        }

        if (gameStats.livesEnabled && gameStats.livesLeft == 0) {
            gameStats.reasonOfLoss = ReasonOfLoss.OUT_OF_LIVES;
            return true;
        }

        if (gameStats.movesEnabled && gameStats.movesLeft == 0 && !ballManager.hasActiveBalls()) {
            gameStats.reasonOfLoss = ReasonOfLoss.OUT_OF_MOVES;
            return true;
        }

        return false;
    }

    public int getLevel() {
        return gameStats.activeLevel;
    }

    public void debug() {
        debugEnabled = true;
    }

    public boolean isRoundWon() {
        return gameStats.isRoundWon;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public boolean isGameTerminated() {
        return gameTerminated;
    }

    public void endGame() {
        gameTerminated = true;
        gamePaused = true;
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
        return gameStats.movesEnabled;
    }

    public boolean isLivesEnabled() {
        return gameStats.livesEnabled;
    }

    public boolean isTimeEnabled() {
        return gameStats.timeEnabled;
    }

    public void loseLife() {
        if (gameStats.livesEnabled) {
            --gameStats.livesLeft;
            notifyObservers(NotificationType.LIVES_AMOUNT_CHANGED, null);
        }
    }

    public void setTargetScoreOne(int scoreOne) {
        gameStats.targetScoreOne = scoreOne;
    }

    public void setTargetScoreTwo(int scoreTwo) {
        gameStats.targetScoreTwo = scoreTwo;
    }

    public void setTargetScoreThree(int scoreThree) {
        gameStats.targetScoreThree = scoreThree;
    }

    public void setLives(int lives) {
        gameStats.livesLeft = lives < 0 ? 0 : lives;
    }

    public void setTime(int time) {
        gameStats.timeLeft = time < 0 ? 0 : time;
    }

    public void setMoves(int moves) {
        gameStats.movesLeft = moves < 0 ? 0 : moves;
    }

    public void giveExtraLife(int moves, int lives, int time) {
        rewardLives(lives);
        rewardMoves(moves);
        rewardTime(time);
        ++gameStats.extraLivesUsed;
    }

    public void rewardLives(int amount) {
        if (gameStats.livesEnabled) {
            gameStats.livesLeft += amount;
            notifyObservers(NotificationType.REWARDED_LIVES, amount);
            notifyObservers(NotificationType.LIVES_AMOUNT_CHANGED, amount);
        }
    }

    public void rewardMoves(int amount) {
        if (gameStats.movesEnabled) {
            gameStats.movesLeft += amount;
            notifyObservers(NotificationType.REWARDED_MOVES, amount);
            notifyObservers(NotificationType.MOVES_AMOUNT_CHANGED, gameStats.movesLeft);
        }
    }

    public void addScore(int amount) {
        scoreThisFrame += amount;
    }

    public void checkIfTargetScoreReached() {
        int starsUnlocked = gameStats.starsUnlocked;
        if (starsUnlocked == 3) return;

        while (true) {
            switch (gameStats.starsUnlocked) {
                case 0:
                    if (gameStats.totalScore < gameStats.targetScoreOne) return;
                    notifyObservers(NotificationType.TARGET_SCORE_REACHED, 1);
                    break;
                case 1:
                    if (gameStats.totalScore < gameStats.targetScoreTwo) return;
                    notifyObservers(NotificationType.TARGET_SCORE_REACHED, 2);
                    break;
                case 2:
                    if (gameStats.totalScore < gameStats.targetScoreThree) return;
                    notifyObservers(NotificationType.TARGET_SCORE_REACHED, 3);
                    break;
                default:
                    return;
            }
            ++gameStats.starsUnlocked;
        }
    }

    // Time constantly changes so a notification is not required
    public void rewardTime(int amount) {
        if (gameStats.timeEnabled) {
            gameStats.timeLeft += amount;
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void pauseGame() {
        gamePaused = true;
    }

    public void resumeGame() {
        gamePaused = false;
    }

    public void consumeSecondLife() {
        gameStats.freeSecondLife = false;
        ++gameStats.extraLivesUsed;
    }

    public boolean isSecondLifeAvailable() {
        return gameStats.freeSecondLife;
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
            case ASTRONAUTS_FOUND:
                gameStats.astronautsLeft += (short) ob;
                break;

            case SAME_COLOR_MATCH:
                match3Size = (int) ob;
                break;

            case NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED:
                gameTerminated = true;
                break;

            case TILE_DESTROYED:
                if (((TilemapTile) ob).getTile().getTileType().getMajorType() == TileType.MajorType.ASTRONAUT)
                    --gameStats.astronautsLeft;

                ++ballsDestroyedThisFrame;
                break;

            case NO_COLOR_MATCH:
                if (gameStats.livesEnabled) {
                    --gameStats.livesLeft;
                    notifyObservers(NotificationType.LIVES_AMOUNT_CHANGED, null);
                }
                break;

            case BALL_LAUNCHED:
                if (gameStats.movesEnabled) {
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
        private ReasonOfLoss reasonOfLoss;

        private int activeLevel;
        private int unlockedLevel;
        private boolean isRoundWon;

        private int starsUnlocked;
        private int totalScore;
        private int userHighScore;
        private int targetScoreOne;
        private int targetScoreTwo;
        private int targetScoreThree;

        private boolean movesEnabled;
        private boolean livesEnabled;
        private boolean timeEnabled;

        private int movesLeft;
        private float timeLeft;
        private int livesLeft;
        private int astronautsLeft;

        private boolean freeSecondLife;
        private int extraLivesUsed;

        private void reset() {
            activeLevel = -1;
            unlockedLevel = -1;
            isRoundWon = false;

            starsUnlocked = 0;
            totalScore = 0;
            userHighScore = 0;
            targetScoreOne = 0;
            targetScoreTwo = 0;
            targetScoreThree = 0;

            extraLivesUsed = 0;

            livesEnabled = false;
            movesEnabled = false;
            timeEnabled = false;
            freeSecondLife = false;
            reasonOfLoss = ReasonOfLoss.NONE;

            movesLeft = 0;
            timeLeft = 0;
            livesLeft = 0;
            astronautsLeft = 0;
        }

        public boolean isFreeLifeAvailable() {
            return freeSecondLife;
        }

        public ReasonOfLoss getReasonOfLoss() {
            return reasonOfLoss;
        }

        public int getStarsUnlocked() {
            return starsUnlocked;
        }

        public int getExtraLivesUsed() {
            return extraLivesUsed;
        }

        public boolean isRoundWon() {
            return isRoundWon;
        }

        public int getActiveLevel() {
            return activeLevel;
        }

        public int getUnlockedLevel() {
            return unlockedLevel;
        }

        public boolean isLevelUnlocked() {
            return isRoundWon && activeLevel == unlockedLevel;
        }

        public int getUserHighScore() {
            return userHighScore;
        }

        public int getTargetScoreOne() {
            return targetScoreOne;
        }

        public int getTargetScoreTwo() {
            return targetScoreTwo;
        }

        public int getTargetScoreThree() {
            return targetScoreThree;
        }

        public int getTotalScore() {
            return totalScore;
        }
    }

    public enum ReasonOfLoss {
        NONE,
        MISSED_TARGET_SCORE,
        ASTRONAUTS_LEFT,
        OUT_OF_MOVES,
        OUT_OF_LIVES,
        OUT_OF_TIME
    }
}

