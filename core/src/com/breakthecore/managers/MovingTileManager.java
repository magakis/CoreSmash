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
import java.util.Arrays;
import java.util.Comparator;
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
    private ChanceColorPicker chanceColorPicker;
    private TilemapManager tilemapManager;

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
    private boolean isAutoEjectEnabled;
    private boolean isControlledBallGenerationEnabled;
    private boolean isAutoReloadEnabled;
    private boolean isActive;

    public MovingTileManager(int tileSize) {
        launcher = new Queue<MovingTile>(3);
        launcherPos = new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() / 5);
        activeList = new LinkedList<MovingTile>();
        this.tileSize = tileSize;
        rand = new Random();
        chanceColorPicker = new ChanceColorPicker();

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

    /**
     * A list of predefined colors that will be used to load the Launcher insead of random ones.
     */
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

    public void setDefaultBallSpeed(int defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
        for (MovingTile mt : launcher) {
            mt.setSpeed(defaultSpeed);
        }
    }

    public void setAutoEject(boolean autoEject) {
        isAutoEjectEnabled = autoEject;
    }

    public void setAutoReloadEnabled(boolean autoReload) {
        isAutoReloadEnabled = autoReload;
    }

    public void setLauncherCooldown(float delay) {
        launcherCooldown = delay;
        launcherCooldownTimer = delay;
    }

    public void setLastTileColor(int colorId) {
        launcher.last().getTile().setID(colorId);
    }

    public boolean isLoadedWithSpecial() {
        return isLoadedWithSpecial;
    }

    public void insertSpecialTile(int id) {
        // TODO(21/4/2018): This function should know anything about the specialTile...
        if (!isLoadedWithSpecial) {
            switch (id) {
                case 10:
                    //TODO: WORK ON THIS
                    launcher.addFirst(createMovingTile(launcherPos.x, launcherPos.y + tileSize, new BombTile(10)));
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

                    if (isAutoReloadEnabled) {
                        loadLauncher();
                    }

                    notifyObservers(NotificationType.BALL_LAUNCHED, null);
                } else {
                    isLoadedWithSpecial = false;
                }

                launcherCooldownTimer = launcherCooldown;
            }
        }
    }

    private void loadLauncher() {
        if (colorSequenceList.hasNext()) {
            launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - launcher.size * tileSize, new RegularTile(colorSequenceList.getNext())));
        } else if (isControlledBallGenerationEnabled) {
            launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - launcher.size * tileSize, new RegularTile(getColorBasedOnTilemap())));
        } else {
            launcher.addLast(createMovingTile(launcherPos.x, launcherPos.y - launcher.size * tileSize, createRegularTile()));
        }
    }

    private int getColorBasedOnTilemap() {
        if (!isControlledBallGenerationEnabled)
            throw new RuntimeException("ControlledBallGeneration is disabled..?!");

        int[] amountOfColor = tilemapManager.getColorAmountsAvailable();
        int totalTiles = tilemapManager.getTotalAmountOfTiles();

        chanceColorPicker.load(amountOfColor, totalTiles);
        if (launcher.size > 0) {
            chanceColorPicker.colorGroups[launcher.last().getTileID()].skip = true;
        }
        return chanceColorPicker.get();
    }

    public void initLauncher(int launcherSize) {
        this.launcherSize = launcherSize;
        while (launcher.size < launcherSize) {
            loadLauncher();
        }
    }

    public void enableControlledBallGeneration(TilemapManager tilemapManager) {
        this.tilemapManager = tilemapManager;
        isControlledBallGenerationEnabled = true;
    }

    public void reset() {
        isActive = true;
        defaultSpeed = 0;
        launcherCooldown = 0;
        isControlledBallGenerationEnabled = false;
        isAutoReloadEnabled = true;
        tilemapManager = null;

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
                while (launcher.last().getTileID() == t.getID()) {
                    t.setID(getRandomColor());
                }
            }
        }

        return t;
    }

    public class ColorSequenceList {
        private int index;
        private ArrayList<Integer> listColors = new ArrayList<>();

        private ColorSequenceList() {
        }

        public int getNext() {
            return listColors.get(index++);
        }

        public void add(int color) {
            if (color < colorCount && color >= 0) {
                listColors.add(color);
            } else {
                throw new IllegalArgumentException();
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

    /**
     * Color reloading in launcher can be done in a separate thread!
     */
    private class ChanceColorPicker {
        private ColorGroup[] colorGroups;
        private int totalAmount;

        private Comparator sortColors = new Comparator<ColorGroup>() {
            @Override
            public int compare(ColorGroup o1, ColorGroup o2) {
                return o1.groupColor > o2.groupColor ? 1 : -1;
            }
        };

        private Comparator sortAmounts = new Comparator<ColorGroup>() {
            @Override
            public int compare(ColorGroup o1, ColorGroup o2) {
                if (o1.enabled) {
                    if (o2.enabled) {
                        if (o1.skip) {
                            if (o2.skip) {
                                return o1.amount < o2.amount ? -1 : 1;
                            } else {
                                return 1;
                            }
                        } else {
                            if (o2.skip) {
                                return -1;
                            } else {
                                return o1.amount < o2.amount ? -1 : 1;
                            }
                        }
                    } else {
                        return -1;
                    }
                } else {
                    if (o2.enabled) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        };

        public ChanceColorPicker() {
            colorGroups = new ColorGroup[10]; // XXX(22/4/2018): Magic Value 10
            for (int i = 0; i < colorGroups.length; ++i) {
                colorGroups[i] = new ColorGroup(i);
            }
        }

        public int get() {
            int maxIndex = totalAmount;

            for (MovingTile mt : activeList) {
                int color = mt.getTileID();
                int amount = colorGroups[color].amount;
                if (amount == 2) {
                    maxIndex -=2;
                    colorGroups[color].amount = 0;
                } else if (amount < 2) {
                    ++colorGroups[color].amount;
                    ++maxIndex;
                }
            }

            for (MovingTile mt : launcher) {
                int color = mt.getTileID();
                int amount = colorGroups[color].amount;
                if (amount == 2) {
                    maxIndex -=2;
                    colorGroups[color].amount = 0;
                } else if (amount < 2) {
                    ++colorGroups[color].amount;
                    ++maxIndex;
                }
            }

            Arrays.sort(colorGroups, sortAmounts);

            for (ColorGroup cg : colorGroups) {
                if (cg.skip || !cg.enabled) {
                    maxIndex -= cg.amount;
                }
            }

            if (maxIndex == 0) {
                int enabledCount = 0;

                for (ColorGroup cg : colorGroups) {
                    if (cg.enabled && !cg.skip) {
                        ++enabledCount;
                    } else {
                        break;
                    }
                }

                if (enabledCount == 0) {
                    for (ColorGroup cg : colorGroups) {
                        if (cg.enabled) {
                            ++enabledCount;
                        } else {
                            break;
                        }
                    }
                }

                if (enabledCount == 0) return 9; // XXX(27/4/2018): Needs better implementation cause this is just for indication
                return colorGroups[rand.nextInt(enabledCount)].groupColor;
            }

            int index = rand.nextInt(maxIndex);
            int sum = 0;
            int i = 0;
            while (colorGroups[i].amount == 0) ++i;

            do {
                sum += colorGroups[i++].amount;
            } while (sum < index);

            return colorGroups[i - 1].groupColor;
        }

        public void load(int[] arrOfColorAmounts, int total) {
            totalAmount = total;

            for (ColorGroup colorGroup : colorGroups) {
                colorGroup.reset();
            }

            Arrays.sort(colorGroups, sortColors);

            for (int i = 0; i < arrOfColorAmounts.length; ++i) {
                colorGroups[i].amount = arrOfColorAmounts[i];
            }

            for (ColorGroup colorGroup : colorGroups) {
                if (colorGroup.amount > 0) {
                    colorGroup.enabled = true;
                }
            }
        }

        private class ColorGroup {
            private final int groupColor;
            private int amount;
            private boolean enabled;
            private boolean skip;

            public ColorGroup(int color) {
                groupColor = color;
            }

            public void set(int amount) {
                this.amount = amount;
            }

            public void reset() {
                amount = 0;
                enabled = false;
                skip = false;
            }
        }
    }
}