package com.breakthecore.managers;

import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.Observer;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.tiles.Tile;

import java.util.Random;

public class StatsManager extends Observable implements Observer {
    private Random rand = new Random();
    private int score;

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
            scoreGained = ballsDestroyedThisFrame * multiplier;
            score += scoreGained;
            notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, scoreGained);

            if (isTimeEnabled) {
                time -= delta;
            }

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

    public void reset() {
        score = 0;
        gameMode = null;
        ballsDestroyedThisFrame = 0;
        specialBallCount = 0;
        isMovesEnabled = false;
        moves = 0;
        isTimeEnabled = false;
        time = 0;
        isLivesEnabled = false;
        lives = 0;
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

    public void setLives(boolean enabled, int lives) {
        isLivesEnabled = enabled;
        this.lives = lives;
    }

    public void setTime(boolean enabled, float time) {
        isTimeEnabled = enabled;
        this.time = time;
    }

    public void setMoves(boolean enabled, int moves) {
        isMovesEnabled = enabled;
        this.moves = moves;
    }

    public void setSpecialBallCount(int specialBallCount) {
        this.specialBallCount = specialBallCount;
    }

    public void setGameMode(GameScreen.GameMode mode) {
        gameMode = mode;
    }

    public void setRandomSeed(long seed) {
        rand.setSeed(seed);
    }

    public void consumeSpecialBall(MovingTileManager movingTileManager) {
        if (!movingTileManager.isLoadedWithSpecial()) {
            movingTileManager.insertSpecialTile(Tile.TileType.BOMB);
            --specialBallCount;
        }
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
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
                    notifyObservers(NotificationType.MOVES_AMOUNT_CHANGED, moves);
                }
                break;
        }
    }
}

