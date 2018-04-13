package com.breakthecore.managers;

import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
import com.breakthecore.Observer;
import com.breakthecore.tiles.Tile;

import java.util.Random;

public class StatsManager extends Observable implements Observer {
    private Random rand = new Random();
    private Integer m_scoreAdded;

    private int m_score;
    private int m_lives;
    private float m_time;
    private int m_moves;

    private int specialBallCount;

    private int m_streak;

    public void update(float delta) {
        m_time += delta;

        if (m_streak != 0) {
            float chanceToGainLife = 0;
            switch (m_streak) {
                case 3:
                    m_scoreAdded = m_streak * 10;
                    m_score += m_scoreAdded;
                    notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, m_scoreAdded);
                    chanceToGainLife = .01f;
                    break;
                case 4:
                    m_scoreAdded = m_streak * 12;
                    m_score += m_scoreAdded;
                    notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, m_scoreAdded);
                    chanceToGainLife = .05f;
                    break;
                default:
                    m_scoreAdded = m_streak * 15;
                    m_score += m_scoreAdded;
                    notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, m_scoreAdded);
                    chanceToGainLife = .1f;
                    break;
            }

            if (rand.nextFloat() < chanceToGainLife) {
                ++m_lives;
                notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
            }

            m_streak = 0;
        }
    }

    public void reset() {
        m_score = 0;
        m_time = 0;
        m_streak = 0;
        m_lives = 3;
        specialBallCount = 1;
    }

    public int getScore() {
        return m_score;
    }

    public float getTime() {
        return m_time;
    }

    public int getLives() {
        return m_lives;
    }

    public int getSpecialBallCount() {
        return specialBallCount;
    }

    public void consumeSpecialBall(MovingTileManager movingTileManager) {
        movingTileManager.insertSpecialTile(Tile.TileType.BOMB);
        --specialBallCount;
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_TILE_DESTROYED:
                ++m_streak;
                break;

            case NO_COLOR_MATCH:
                --m_lives;
                notifyObservers(NotificationType.NOTIFICATION_TYPE_LIVES_CHANGED, null);
                break;
        }
    }
}

