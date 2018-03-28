package com.breakthecore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Michail on 24/3/2018.
 */

public class MovingTileManager{
    private Queue<MovingTile> launcher;
    private LinkedList<MovingTile> activeList;
    private LinkedList<MovingTile> tilePool;
    private int m_colorCount;
    private Vector2 launcherPos;
    private int m_tileSize;
    private float launchDelay;
    private float launchDelayCounter;

    public MovingTileManager(int tileSize, int colorCount) {
        launcher = new Queue<MovingTile>(3);
        launcherPos = new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() / 5);
        activeList = new LinkedList<MovingTile>();
        tilePool = new LinkedList<MovingTile>();
        m_tileSize = tileSize;
        m_colorCount = colorCount;

        for (int i = 0; i < 3; ++i) {
            launcher.addLast(new MovingTile(launcherPos.x,launcherPos.y-i*tileSize,getRandomColor()));
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
        updateActiveList(delta);

        if (launchDelayCounter > 0) {
            if (launchDelayCounter-delta < 0)
                launchDelayCounter = 0;
            else
                launchDelayCounter -= delta;
        }
    }

    private void updateActiveList(float delta) {
        for (MovingTile mt : activeList) {
            mt.update(delta);
        }
    }

    public int getRandomColor() {
        return(int)(Math.random()*100)%m_colorCount;
    }

    public Queue<MovingTile> getLauncherQueue() {
        return launcher;
    }

    public void eject() {
        if (launchDelayCounter == 0) {
            activeList.add(launcher.removeFirst());
            launcher.get(0).setPositionInWorld(launcherPos.x, launcherPos.y);
            launcher.get(1).setPositionInWorld(launcherPos.x, launcherPos.y - m_tileSize);

            if (tilePool.size() > 0) {
                launcher.addLast(tilePool.removeFirst());
                launcher.last().setPositionInWorld(launcherPos.x, launcherPos.y - 2 * m_tileSize);
                launcher.last().setColor(getRandomColor());
            } else {
                launcher.addLast(new MovingTile(launcherPos.x, launcherPos.y - 2 * m_tileSize, getRandomColor()));
            }
            launchDelayCounter = launchDelay;
        }
    }

    private void disposeInactive() {
        ListIterator<MovingTile> iter = activeList.listIterator();
        MovingTile tile;
        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()){
                tile.setFlag(false);
                tilePool.add(tile);
                iter.remove();
            }
        }
    }
}

/*
        moveTile = new MovingTile(
                new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 4),
                texture);

*/