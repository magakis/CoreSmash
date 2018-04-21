package com.breakthecore.managers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import com.breakthecore.NotificationType;
import com.breakthecore.Observable;
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

public class MovingTileManager extends Observable {
    private Queue<MovingTile> launcher;
    private Pool<MovingTile> movingTilePool;
    private LinkedList<MovingTile> activeList;

    // XXX(20/4/2018): Feels like the following variables shouldn't be here
    private int colorCount;
    private int tileSize;
    /////////////

    private Vector2 launcherPos;
    private float launcherCooldown;
    private float launcherCooldownTimer;
    private boolean isLoadedWithSpecial;

    private int defaultSpeed;
    private float defaultScale;

    private boolean isBallGenerationEnabled;
    private boolean isActive;
    private boolean autoEjectEnabled;

    public MovingTileManager(int tileSize, int colorCount) {
        launcher = new Queue<MovingTile>(3);
        launcherPos = new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() / 5);
        activeList = new LinkedList<MovingTile>();
        this.tileSize = tileSize;
        this.colorCount = colorCount;
        isActive = true;
        defaultSpeed = 15;
        defaultScale = 1 / 2f;
        launcherCooldown = .1f;

        movingTilePool = new Pool<MovingTile>() {
            @Override
            protected MovingTile newObject() {
                return new MovingTile(launcherPos, defaultSpeed);
            }
        };
    }

    public boolean isLoadedWithSpecial() {
        return isLoadedWithSpecial;
    }

    public void insertSpecialTile(Tile.TileType tileType) {
        if (!isLoadedWithSpecial) {
            switch (tileType) {
                case BOMB:
                    //TODO: WORK ON THIS
                    launcher.addFirst(createMovingTile(launcherPos.x, launcherPos.y + tileSize, new BombTile()));
                    launcher.first().setScale(1);
                    isLoadedWithSpecial = true;
                    break;
            }
        }
    }

    public void setBallGenerationEnabled(boolean state) {
        isBallGenerationEnabled = state;
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

            if (launcherCooldownTimer > 0) {
                if (launcherCooldownTimer - delta < 0)
                    launcherCooldownTimer = 0;
                else
                    launcherCooldownTimer -= delta;
            }

            if (autoEjectEnabled && launcherCooldownTimer == 0) {
                eject();
            }
        }
    }

    public void setActiveState(boolean active) {
        isActive = active;
    }

    public void setAutoEject(boolean autoEject) {
        autoEjectEnabled = autoEject;
    }

    public void setLauncherCooldown(float delay) {
        launcherCooldown = delay;
        launcherCooldownTimer = delay;
    }

    private void updateActiveList(float delta) {
        for (MovingTile mt : activeList) {
            mt.update(delta);
        }
    }

    public int getRandomColor() {
        return (WorldSettings.getRandomInt(colorCount));
    }

    public Queue<MovingTile> getLauncherQueue() {
        return launcher;
    }

    public void setLastTileColor(int colorId) {
        launcher.last().getTile().setColor(colorId);
    }

    public void eject() {
        if (launcherCooldownTimer == 0) {
            if (launcher.size > 0) {
                activeList.add(launcher.removeFirst());

                if (!isLoadedWithSpecial) {
                    if (launcher.size > 0) {
                        launcher.last().setPositionInWorld(launcherPos.x, launcherPos.y - tileSize);
                        launcher.first().setPositionInWorld(launcherPos.x, launcherPos.y);
                    }
                    if (isBallGenerationEnabled) {
                        launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - 2 * tileSize, createRegularTile()));
                    }
                    notifyObservers(NotificationType.BALL_LAUNCHED, null);
                } else {
                    isLoadedWithSpecial = false;
                }

                launcherCooldownTimer = launcherCooldown;
            }
        }
    }

    public void reset() {
        isActive = true;
        defaultSpeed = 0;
        launcherCooldown = 0;
        isBallGenerationEnabled = true;

        Iterator<MovingTile> iter = activeList.iterator();
        MovingTile tile;

        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()) {
                tile.setFlag(false);
            }
            movingTilePool.free(tile);
            iter.remove();
        }

        iter = launcher.iterator();
        while (iter.hasNext()) {
            tile = iter.next();
            tile.extractTile(); //TODO: Remove tile in a pool of tiles
            movingTilePool.free(tile);
            iter.remove();
        }

        while (launcher.size < 3) {
            launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - launcher.size * tileSize, createRegularTile()));
        }
    }

    public void setDefaultBallSpeed(int defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
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
                movingTilePool.free(tile);
                iter.remove();
            }
        }
    }

    private MovingTile createMovingTile(float x, float y, Tile tile) {
        MovingTile res;
        res = movingTilePool.obtain();
        res.setPositionInWorld(x, y);
        res.setTile(tile);
        res.setSpeed(defaultSpeed);
        res.setScale(defaultScale);
        return res;
    }

    private RegularTile createRegularTile() {
        // TODO(20/4/2018): Change the name and take the following logic elsewhere
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