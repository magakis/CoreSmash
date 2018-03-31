package com.breakthecore;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Michail on 24/3/2018.
 */

public class MovingTileManager{
    private Queue<MovingTile> launcher;
    private LinkedList<MovingTile> activeList;
    private LinkedList<MovingTile> movtPool;
    private int m_colorCount;
    private Vector2 launcherPos;
    private int m_tileSize;
    private float launchDelay;
    private float launchDelayCounter;

    private int m_defaultSpeed;

    private boolean isActive;
    private boolean m_autoEject;

    public MovingTileManager(int tileSize, int colorCount) {
        launcher = new Queue<MovingTile>(3);
        launcherPos = new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() / 5);
        activeList = new LinkedList<MovingTile>();
        movtPool = new LinkedList<MovingTile>();
        m_tileSize = tileSize;
        m_colorCount = colorCount;
        isActive = true;
        m_defaultSpeed = 15;

        for (int i = 0; i < 3; ++i) {
            launcher.addLast(new MovingTile(launcherPos.x, launcherPos.y - i * tileSize, getRandomColor(), m_defaultSpeed));
        }

        launchDelay = tileSize / launcher.first().getSpeed();
    }

    public MovingTile getFirstActiveTile() {
        if (activeList.size() > 0) {
            return activeList.getFirst();
        }
        return null;
    }

    public LinkedList<MovingTile> getActiveList() {
        return activeList;
    }

    public Vector2 getLauncherPos() {
        return launcherPos;
    }

    public void update(float delta) {
        disposeInactive();
        if (isActive) {
            updateActiveList(delta);

            if (launchDelayCounter > 0) {
                if (launchDelayCounter - delta < 0)
                    launchDelayCounter = 0;
                else
                    launchDelayCounter -= delta;
            }

            if (m_autoEject && launchDelayCounter == 0) {
                eject();
            }
        }
    }

    public void setAutoEject(boolean autoEject) {
        m_autoEject = autoEject;
    }

    public void setLaunchDelay(float delay) {
        launchDelay = delay;
    }

    private void updateActiveList(float delta) {
        for (MovingTile mt : activeList) {
            mt.update(delta);
        }
    }

    public int getRandomColor() {
        return (WorldSettings.getRandomInt(m_colorCount));
    }

    public Queue<MovingTile> getLauncherQueue() {
        return launcher;
    }

    public void eject() {
        if (launchDelayCounter == 0) {
            activeList.add(launcher.removeFirst());
            launcher.get(0).setPositionInWorld(launcherPos.x, launcherPos.y);
            launcher.get(1).setPositionInWorld(launcherPos.x, launcherPos.y - m_tileSize);

            if (movtPool.size() > 0) {
                launcher.addLast(movtPool.removeFirst());
                launcher.last().setPositionInWorld(launcherPos.x, launcherPos.y - 2 * m_tileSize);
                launcher.last().setColor(getRandomColor());
                launcher.last().setSpeed(m_defaultSpeed);
            } else {
                launcher.addLast(new MovingTile(launcherPos.x, launcherPos.y - 2 * m_tileSize, getRandomColor(), m_defaultSpeed));
            }
            launchDelayCounter = launchDelay;
        }
    }

    public void setDefaultSpeed(int defaultSpeed) {
        m_defaultSpeed = defaultSpeed;
        for (MovingTile mt : launcher) {
            mt.setSpeed(defaultSpeed);
        }
    }

    private void disposeInactive() {
        ListIterator<MovingTile> iter = activeList.listIterator();
        MovingTile tile;
        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()){
                tile.setFlag(false);
                // NOTE: It's questionable whether I want to remove the observes since MovingTiles
                // practically never get disposed. They are pooled.
//                tile.emptyObserverList();
                movtPool.add(tile);
                iter.remove();
            }
        }
    }
}