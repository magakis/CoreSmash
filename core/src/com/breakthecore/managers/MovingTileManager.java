package com.breakthecore.managers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.BombTile;
import com.breakthecore.tiles.MovingTile;
import com.breakthecore.tiles.RegularTile;
import com.breakthecore.tiles.Tile;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Michail on 24/3/2018.
 */

public class MovingTileManager {
    private Queue<MovingTile> launcher;
    private LinkedList<MovingTile> activeList;
    private LinkedList<MovingTile> movtPool;
    private int m_colorCount;
    private Vector2 launcherPos;
    private int m_tileSize;
    private float launchDelay;
    private float launchDelayCounter;
    private boolean hasSpecial;

    private int m_defaultSpeed;
    private float m_defaultScale;

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
        m_defaultScale = 1/2f;
        launchDelay = .1f;
    }

    public void insertSpecialTile(Tile.TileType tileType) {
        // Remove pre-existing special tiles
        if (hasSpecial) {
            launcher.removeFirst();
            hasSpecial = false;
        }

        switch (tileType) {
            case BOMB:
                //TODO: WORK ON THIS
                launcher.addFirst(createMovingTile(launcherPos.x, launcherPos.y + m_tileSize, new BombTile()));
                launcher.first().setScale(1);
                hasSpecial = true;
                break;
        }
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

    public void setActiveState(boolean active) {
        isActive = active;
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
            if (launcher.size > 0) {
                activeList.add(launcher.removeFirst());
                if (!hasSpecial) {
                    launcher.get(0).setPositionInWorld(launcherPos.x, launcherPos.y);
                    launcher.get(1).setPositionInWorld(launcherPos.x, launcherPos.y - m_tileSize);
                    launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - 2 * m_tileSize, createRegularTile()));
                } else {
                    hasSpecial = false;
                }
                launchDelayCounter = launchDelay;
            } else {
                throw new NullPointerException("WTF?");
            }
        }
    }

    public void reset() {
        isActive = true;
        m_defaultSpeed = 15;
        launchDelay = 0.1f;

        Iterator<MovingTile> iter = activeList.iterator();
        MovingTile tile;

        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()) {
                tile.setFlag(false);
            }
            movtPool.add(tile);
            iter.remove();
        }

        iter = launcher.iterator();
        while (iter.hasNext()) {
            tile = iter.next();
            tile.setSpeed(m_defaultSpeed);
            tile.extractTile(); //TODO: Remove tile in a pool of tiles
            movtPool.add(tile);
            iter.remove();
        }

        while (launcher.size < 3) {
            launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - launcher.size * m_tileSize, createRegularTile()));
        }
    }

    public void setDefaultBallSpeed(int defaultSpeed) {
        m_defaultSpeed = defaultSpeed;
        for (MovingTile mt : launcher) {
            mt.setSpeed(defaultSpeed);
        }
    }

    public void disposeInactive() {
        ListIterator<MovingTile> iter = activeList.listIterator();
        MovingTile tile;
        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()) {
                tile.setFlag(false);
                // NOTE: It's questionable whether I want to remove the observes since MovingTiles
                // practically never get disposed. They are pooled.
//                tile.clearObserverList();
                movtPool.add(tile);
                iter.remove();
            }
        }
    }

    private MovingTile createMovingTile(float x, float y, Tile tile) {
        MovingTile res;
        if (movtPool.size() > 0) {
            res = movtPool.removeFirst();
            res.setPositionInWorld(x, y);
            res.setTile(tile);
            res.setSpeed(m_defaultSpeed);
            res.setScale(m_defaultScale);
        } else {
            res = new MovingTile(x, y, m_defaultSpeed, tile);
        }
        return res;
    }

    private RegularTile createRegularTile() {
        RegularTile t = new RegularTile();

        outer:
        while (true) {
            for (MovingTile mt : launcher) {
                if (mt.hasTile()) {
                    if (mt.getColor() == t.getColor()) {
                        t.setColor(getRandomColor());
                        continue outer;
                    }
                }
            }
            break;
        }

        return t;
    }
}