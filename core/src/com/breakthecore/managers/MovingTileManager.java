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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

/**
 * Created by Michail on 24/3/2018.
 */

public class MovingTileManager extends Observable {
    private Queue<MovingTile> launcher;
    private Pool<MovingTile> movingTilePool;
    private LinkedList<MovingTile> activeList;
    private Random rand;
    private ColorSequenceList colorSequenceList;

    // XXX(20/4/2018): Feels like the following variables shouldn't be here
    private int colorCount;
    private int tileSize;
    /////////////

    private Vector2 launcherPos;
    private int launcherSize;
    private float launcherCooldown;
    private float launcherCooldownTimer;

    private int defaultSpeed;
    private float defaultScale;

    private boolean isLoadedWithSpecial;
    private boolean isLauncherLoadingEnabled;
    private boolean isAutoEjectEnabled;
    private boolean isActive;

    public MovingTileManager(int tileSize) {
        launcher = new Queue<MovingTile>(3);
        launcherPos = new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() / 5);
        activeList = new LinkedList<MovingTile>();
        this.tileSize = tileSize;
        rand = new Random();

        isActive = true;
        defaultSpeed = 15;
        defaultScale = 1 / 2f;
        launcherCooldown = .1f;

        colorSequenceList = new ColorSequenceList();
        movingTilePool = new Pool<MovingTile>() {
            @Override
            protected MovingTile newObject() {
                return new MovingTile(launcherPos, defaultSpeed);
            }
        };
    }

    public int getRandomColor() {
        return rand.nextInt(colorCount);
    }

    public int getLauncherSize() {
        return launcherSize;
    }

    public Vector2 getLauncherPos() {
        return launcherPos;
    }

    public MovingTile getFirstActiveTile() {
        if (activeList.size() > 0) {
            return activeList.getFirst();
        }
        return null;
    }

    /** A list of predefined colors that will be used to load the Launcher insead of random ones. */
    public ColorSequenceList getColorSequenceList() {
        return colorSequenceList;
    }

    public LinkedList<MovingTile> getActiveList() {
        return activeList;
    }

    public Queue<MovingTile> getLauncherQueue() {
        return launcher;
    }

    public void setActiveState(boolean active) {
        isActive = active;
    }

    public void setRandomSeed(long seed) {
        rand.setSeed(seed);
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }

    public void setLauncherLoadingEnabled(boolean state) {
        isLauncherLoadingEnabled = state;
    }

    public void setDefaultBallSpeed(int defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
        for (MovingTile mt : launcher) {
            mt.setSpeed(defaultSpeed);
        }
    }

    public void setAutoEject(boolean autoEject) {
        isAutoEjectEnabled = autoEject;
    }

    public void setLauncherCooldown(float delay) {
        launcherCooldown = delay;
        launcherCooldownTimer = delay;
    }

    public void setLastTileColor(int colorId) {
        launcher.last().getTile().setColor(colorId);
    }

    public boolean isLoadedWithSpecial() {
        return isLoadedWithSpecial;
    }

    public void insertSpecialTile(Tile.TileType tileType) {
        // TODO(21/4/2018): This function should know anything about the specialTile...
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

    public void update(float delta) {
        if (isActive) {
            for (MovingTile mt : activeList) {
                mt.update(delta);
            }

            if (launcherCooldownTimer > 0) {
                if (launcherCooldownTimer - delta < 0)
                    launcherCooldownTimer = 0;
                else
                    launcherCooldownTimer -= delta;
            }

            if (isAutoEjectEnabled && launcherCooldownTimer == 0) {
                eject();
            }
        }
    }

    public void eject() {
        if (launcherCooldownTimer == 0) {
            if (launcher.size > 0) {
                activeList.add(launcher.removeFirst());

                if (!isLoadedWithSpecial) {
                    if (launcher.size > 0) {
                        for (int i = 0; i < launcher.size; ++i) {
                            launcher.get(i).setPositionInWorld(launcherPos.x, launcherPos.y - i * tileSize);
                        }
                    }

                    loadLauncher();

                    notifyObservers(NotificationType.BALL_LAUNCHED, null);
                } else {
                    isLoadedWithSpecial = false;
                }

                launcherCooldownTimer = launcherCooldown;
            }
        }
    }

    private void loadLauncher() {
        if (isLauncherLoadingEnabled) {
            if (colorSequenceList.hasNext()) {
                launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - launcher.size * tileSize, new RegularTile(colorSequenceList.getNext())));
            } else {
                launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - launcher.size * tileSize, createRegularTile()));
            }
        }
    }

    public void initLauncher(int launcherSize) {
        this.launcherSize = launcherSize;
        while (launcher.size < launcherSize && (isLauncherLoadingEnabled || colorSequenceList.hasNext())) {
            loadLauncher();
        }
    }

    public void reset() {
        isActive = true;
        defaultSpeed = 0;
        launcherCooldown = 0;
        isLauncherLoadingEnabled = true;

        Iterator<MovingTile> iter = activeList.iterator();
        MovingTile tile;

        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()) {
                tile.setFlagForDisposal(false);
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

        colorSequenceList.reset();
    }

    public void disposeInactive() {
        ListIterator<MovingTile> iter = activeList.listIterator();
        MovingTile tile;
        while (iter.hasNext()) {
            tile = iter.next();
            if (tile.getFlag()) {
                tile.setFlagForDisposal(false);
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
        RegularTile t = new RegularTile(getRandomColor());

        if (colorCount > 1) {
            if (launcher.size > 0) {
                while (launcher.last().getColor() == t.getColor()) {
                    t.setColor(getRandomColor());
                }
            }
        }

        return t;
    }

    public class ColorSequenceList {
        private int index;
        private ArrayList<Integer> listColors = new ArrayList<Integer>();
        private ColorSequenceList() {}

        public int getNext(){
            return listColors.get(index++);
        }

        public void add(int color) {
            if (color < colorCount && color >= 0) {
                listColors.add(color);
            } else {
                listColors.add(7); // 7 is WHITE color. Signals problem.
            }
        }

        public boolean hasNext() {
            return index < listColors.size();
        }

        public void reset() {
            listColors.clear();
            index = 0;
        }
    }
}