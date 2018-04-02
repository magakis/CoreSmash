package com.breakthecore;

public class ScoreManager extends Observable implements Observer {
    private int m_score;
    private Integer m_scoreAdded;
    private int m_streak;

    public ScoreManager() {
    }

    public void update() {
        if (m_streak > 0) {
            m_scoreAdded = m_streak * 10;
            m_score += m_scoreAdded;
            notifyObservers(NotificationType.NOTIFICATION_TYPE_SCORE_INCREMENTED, m_scoreAdded);
            m_streak = 0;
        }
    }

    public void reset() {
        m_score = 0;
        m_streak = 0;
    }

    public int getScore() {
        return m_score;
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_TILE_DESTROYED:
                ++m_streak;
                break;
        }
    }
}

