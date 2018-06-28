package com.breakthecore.managers;

import com.badlogic.gdx.utils.Pool;
import com.breakthecore.Observable;
import com.breakthecore.tiles.Launchable;
import com.breakthecore.tiles.MovingBall;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TileFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Michail on 24/3/2018.
 */

public class MovingBallManager extends Observable {
    private Pool<MovingBall> movingTilePool;
    private LinkedList<MovingBall> activeList;

    private int defaultSpeed;
    private float defaultScale;

    private boolean isActive;

    public MovingBallManager() {
        activeList = new LinkedList<MovingBall>();

        isActive = true;
        defaultSpeed = 15;
        defaultScale = 1 / 2f;

        movingTilePool = new Pool<MovingBall>() {
            @Override
            protected MovingBall newObject() {
                return new MovingBall();
            }
        };
    }

    public MovingBall getFirstActiveTile() {
        if (activeList.size() > 0) {
            return activeList.getFirst();
        }
        return null;
    }

    public void setDefaultBallSpeed(int defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
    }

    public boolean hasActiveBalls() {
        return activeList.size() != 0;
    }

    public void activate(MovingBall ball) {
        activeList.add(ball);
    }

    public MovingBall create(float x, float y, int ID) {
        Tile tile = TileFactory.getTileFromID(ID);
        if (!(tile instanceof Launchable))
            throw new RuntimeException("Tile with ID:" + ID + ", is not Launchable!");

        MovingBall movingBall = movingTilePool.obtain();
        movingBall.setPositionInWorld(x, y);
        movingBall.setSpeed(defaultSpeed);
        movingBall.setScale(defaultScale);
        movingBall.setTile(tile);
        return movingBall;
    }

    public void dispose(MovingBall ball) {
        activeList.remove(ball);
        movingTilePool.free(ball);
    }

    public LinkedList<MovingBall> getActiveList() {
        return activeList;
    }

    public void update(float delta) {
        if (isActive) {
            for (MovingBall mt : activeList) {
                mt.update(delta);
            }
        }
    }

    public void draw(RenderManager renderManager) {
        renderManager.draw(activeList);
    }

    public void reset() {
        isActive = true;
        defaultSpeed = 0;

        Iterator<MovingBall> iter = activeList.iterator();
        MovingBall tile;

        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()) {
                tile.setFlagForDisposal(false);
            }
            movingTilePool.free(tile);
            iter.remove();
        }

    }

    public void disposeInactive() {
        ListIterator<MovingBall> iter = activeList.listIterator();
        MovingBall tile;
        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()) {
                tile.setFlagForDisposal(false);
                movingTilePool.free(tile);
                iter.remove();
            }
        }
    }

    private MovingBall createMovingTile(float x, float y, Tile tile) {
        MovingBall res;
        res = movingTilePool.obtain();
        res.setPositionInWorld(x, y);
        res.setTile(tile);
        res.setSpeed(defaultSpeed);
        res.setScale(defaultScale);
        return res;
    }

}