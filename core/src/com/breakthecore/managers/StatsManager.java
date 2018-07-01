package com.breakthecore.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.breakthecore.Launcher;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.Observer;
import com.breakthecore.screens.GameScreen;

import java.util.Random;

public class StatsManager extends Observable implements Observer {
    private Random rand = new Random();

    private int level;
    private boolean isGameActive;
    private boolean isRoundWon;

    private int score;
    private int targetScore;

    private int lives;
    private float time;
    private int moves;

    private boolean isTimeEnabled;
    private boolean isMovesEnabled;
    private boolean isLivesEnabled;

    private GameScreen.GameMode gameMode;
    private int specialBallCount;
    private int ballsDestroyedThisFrame;


    public void update(float delta) {
        if (isTimeEnabled) {
            time -= delta;
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
            score += scoreGained;
            notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, scoreGained);

            if (isLivesEnabled) {
                float chanceToGainLife = ((ballsDestroyedThisFrame * ballsDestroyedThisFrame) / 9.f) / 100.f; // random algorithm I came up with
                if (rand.nextFloat() < chanceToGainLife) {
                    ++lives;
                    notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
                }
            }

            ballsDestroyedThisFrame = 0;
        }
    }

    /* Q: Should reset be private and provide only a function that resets but requires a user? */
    public void reset() {
        score = 0;
        targetScore = 0;
        gameMode = null;
        // If UserAccount listens to StatManager in the future,
        // make sure I remove the current user from the StatManager observer list.
        isGameActive = true;
        isRoundWon = false;

        ballsDestroyedThisFrame = 0;
        specialBallCount = 0;
        isMovesEnabled = false;
        moves = 0;
        isTimeEnabled = false;
        time = 0;
        isLivesEnabled = false;
        lives = 0;
    }

    public boolean checkEndingConditions(MovingBallManager ballManager) {
        if (!isGameActive) {
            return true;
        }

        if (isTimeEnabled && time < 0) {
            isGameActive = false;
            return true;
        }
        if (isLivesEnabled && lives == 0) {
            isGameActive = false;
            return true;
        }
        if (isMovesEnabled && moves == 0 && !ballManager.hasActiveBalls()) {
            isGameActive = false;
            return true;
        }
        return false;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int lvl) {
        level = lvl;
        Preferences prefs = Gdx.app.getPreferences("account");
        targetScore = prefs.getInteger("level" + lvl, 0);
    }

    public boolean getRoundOutcome() {
        if (isGameActive) throw new IllegalStateException("The game is still running?!");
        return isRoundWon;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public void stopGame() {
        isGameActive = false;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getMoves() {
        return moves;
    }

    public float getTime() {
        return time;
    }

    public boolean isMovesEnabled() {
        return isMovesEnabled;
    }

    public boolean isLivesEnabled() {
        return isLivesEnabled;
    }

    public boolean isTimeEnabled() {
        return isTimeEnabled;
    }

    public GameScreen.GameMode getGameMode() {
        return gameMode;
    }

    public int getSpecialBallCount() {
        return specialBallCount;
    }

    public void loseLife() {
        if (isLivesEnabled) {
            --lives;
            notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
        }
    }

    public void setLives(int lives) {
        this.lives = lives < 0 ? 0: lives;
        isLivesEnabled = lives != 0;
    }

    public void setTime(float time) {
        this.time = time < 0 ? 0: time;
        isTimeEnabled = time != 0;
    }

    public void setMoves(int moves) {
        this.moves = moves < 0 ? 0: moves;
        isMovesEnabled = moves != 0;
    }

    public void setSpecialBallCount(int specialBallCount) {
        this.specialBallCount = specialBallCount;
    }

    public void setGameMode(GameScreen.GameMode mode) {
        gameMode = mode;
    }

    public void consumeSpecialBall(Launcher launcher) {
        if (!launcher.isLoadedWithSpecial()) {
            launcher.insertSpecialTile(18);
            --specialBallCount;
        }
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED:
                isRoundWon = true;
                isGameActive = false;
                break;

            case NOTIFICATION_TYPE_TILE_DESTROYED:
                ++ballsDestroyedThisFrame;
                break;

            case NO_COLOR_MATCH:
                if (isLivesEnabled) {
                    --lives;
                    notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
                }
                break;

            case BALL_LAUNCHED:
                if (isMovesEnabled) {
                    --moves;
                    notifyObservers(NotificationType.MOVES_AMOUNT_CHANGED, null);
                }
                notifyObservers(NotificationType.BALL_LAUNCHED, null);
                break;
        }
    }

    public static class ScoreMultiplier {
        private float multiplier = 1;

        public ScoreMultiplier() {
        }

        public void setup(int colorCount,
                          boolean livesEnabled, int lives,
                          boolean movesEnabled, int moves,
                          boolean timeEnabled, int timeAmount,
                          int amountOfTiles) {
            if (!livesEnabled && !movesEnabled) {
                multiplier = 0;
                return;
            }

            switch (colorCount) {
                case 1:
                    multiplier = 0.05f;
                    break;
                case 2:
                    multiplier = 0.1f;
                    break;
                case 3:
                    multiplier = 0.2f;
                    break;
                case 4:
                    multiplier = 0.6f;
                    break;
                case 5:
                    multiplier = 0.8f;
                    break;
                case 6:
                    multiplier = 1f;
                    break;
                case 7:
                    multiplier = 1.2f;
                    break;
                case 8:
                    multiplier = 1.4f;
                    break;
            }

            if (livesEnabled) {
                float multiplierFromLives;
                switch (lives) {
                    case 1:
                        multiplierFromLives = 2;
                        break;
                    case 2:
                        multiplierFromLives = 1.5f;
                        break;
                    case 3:
                        multiplierFromLives = 1;
                        break;
                    case 4:
                        multiplierFromLives = .8f;
                        break;
                    case 5:
                        multiplierFromLives = .6f;
                        break;
                    default:
                        multiplierFromLives = .4f;
                        break;
                }
                multiplier *= multiplierFromLives;
            }

            if (movesEnabled) {
                float multiplierFromMoves;
                float percentOfTotalTiles = (float) moves / amountOfTiles;
                if (percentOfTotalTiles <= .2f) {
                    multiplierFromMoves = 2;
                } else if (percentOfTotalTiles <= .4f) {
                    multiplierFromMoves = 1.5f;
                } else if (percentOfTotalTiles <= .6f) {
                    multiplierFromMoves = 1f;
                } else if (percentOfTotalTiles <= .8f) {
                    multiplierFromMoves = .8f;
                } else {
                    multiplierFromMoves = .6f;
                }
                multiplier *= multiplierFromMoves;
            }
        }

        public float get() {
            return multiplier;
        }


        public int getTotalTilesFromRadius(int radius) {
            /* Maybe this shouldn't be here..*/
            int total = 1;

            for (int i = 1; i <= radius; ++i) {
                total += i * 6;
            }
            return total;
        }

        public void reset() {
            multiplier = 1;
        }
    }
}

