package com.archapp.coresmash;

import com.archapp.coresmash.managers.MovingBallManager;
import com.archapp.coresmash.managers.RenderManager;
import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tiles.Launchable;
import com.archapp.coresmash.tiles.MovingBall;
import com.archapp.coresmash.tiles.RegularTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

/* We need a new Launcher BOYYSSS. This one is disgusting */
public class Launcher extends Observable {
    private Queue<MovingBall> launcher;
    private ChanceColorPicker chanceColorPicker;
    private MovingBallManager movingBallManager;

    private int ballSize;
    private Vector2 launcherPos;
    private int launcherSize;
    private float launcherCooldown;
    private float launcherCooldownTimer;

    private boolean isLoadedWithSpecial;
    private boolean isAutoEjectEnabled;


    public Launcher(MovingBallManager movingBallManager) {
        this.movingBallManager = movingBallManager;
        launcher = new Queue<>(3);
        launcherPos = new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() / 5);
        launcherCooldown = .1f;
        chanceColorPicker = new ChanceColorPicker();
        launcherCooldown = 0;
        ballSize = WorldSettings.getTileSize();
    }

    public void eject() {
        if (launcherCooldownTimer == 0) {
            if (launcher.size > 0) {
                MovingBall ball = launcher.removeFirst();
                movingBallManager.activate(ball);
                ((Launchable) ball.getTile()).onLaunch();

                if (!isLoadedWithSpecial) {
                    if (launcher.size > 0) {
                        for (int i = 0; i < launcher.size; ++i) {
                            launcher.get(i).setPositionInWorld(launcherPos.x, launcherPos.y - i * WorldSettings.getTileSize());
                        }
                    }
                    notifyObservers(NotificationType.BALL_LAUNCHED, null);
                } else {
                    isLoadedWithSpecial = false;
                }

                launcherCooldownTimer = launcherCooldown;
            }
        }
    }

    public void draw(RenderManager renderManager) {
        renderManager.drawLauncher(launcher, launcherPos);
    }

    public void update(float delta) {
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

    public void setLauncherSize(int launcherSize) {
        this.launcherSize = launcherSize;
    }

    private int getColorBasedOnTilemap(TilemapManager tilemapManager) {
        int[] amountOfColor = tilemapManager.getColorAmountsAvailable();
        int totalTiles = 0;
        for (int amount : amountOfColor) {
            totalTiles += amount;
        }

        chanceColorPicker.load(amountOfColor, totalTiles);
        if (launcher.size > 0) {
            chanceColorPicker.colorGroups[launcher.last().getTileID()].skip = true;
        }
        return chanceColorPicker.get();
    }

    public void fillLauncher(TilemapManager tilemapManager, RoundManager roundManager) {
        int maxSize;
        if (roundManager.isMovesEnabled())
            maxSize = launcherSize <= roundManager.getMoves() ? launcherSize : roundManager.getMoves();
        else
            maxSize = launcherSize;


        while (launcher.size < maxSize) {
            loadLauncher(tilemapManager);
        }
    }

    public void loadLauncher(TilemapManager tilemapManager) {
        if (launcher.size == launcherSize) throw new RuntimeException("ff");
        loadLauncher(getColorBasedOnTilemap(tilemapManager));
    }

    public void loadLauncher(int ballID) {
        launcher.addLast(movingBallManager.create(launcherPos.x, launcherPos.y - launcher.size * ballSize, ballID));
    }

    public void reset() {
        Iterator<MovingBall> iter = launcher.iterator();
        while (iter.hasNext()) {
            MovingBall tile = iter.next();
            tile.extractTile(); //TODO: Remove tile in a pool of tiles
            movingBallManager.dispose(tile);
            iter.remove();
        }

        isLoadedWithSpecial = false;
        launcherCooldownTimer = 0;
        launcherCooldown = 0;
        launcherSize = 3;
    }

    public int getLauncherSize() {
        return launcherSize;
    }

    public void setAutoEject(boolean autoEject) {
        isAutoEjectEnabled = autoEject;
    }

    public boolean isLoadedWithSpecial() {
        return isLoadedWithSpecial;
    }

    public void setLauncherCooldown(float delay) {
        launcherCooldown = delay;
        launcherCooldownTimer = delay;
    }

    /* Function insertSpecialTile should be removed and a better way of handling special tiles
     * should be implemented
     */
    public void insertSpecialTile(int id) {
        // TODO(21/4/2018): This function should know anything about the specialTile...
        if (!isLoadedWithSpecial) {
            launcher.addFirst(movingBallManager.create(launcherPos.x, launcherPos.y + ballSize, id));
            isLoadedWithSpecial = true;
        }
    }

    /**
     * Color reloading in launcher can be done in a separate thread!
     */
    private class ChanceColorPicker {
        private ColorGroup[] colorGroups;
        private Random rand;
        private int totalAmount;

        private Comparator sortColors = new Comparator<ColorGroup>() {
            @Override
            public int compare(ColorGroup o1, ColorGroup o2) {
                return Integer.compare(o1.groupColor, o2.groupColor);
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
            rand = new Random();
            colorGroups = new ColorGroup[10]; // XXX(22/4/2018): Magic Value 10
            for (int i = 0; i < colorGroups.length; ++i) {
                colorGroups[i] = new ColorGroup(i);
            }
        }

        public int get() {
            int maxIndex = totalAmount;

            for (MovingBall mt : movingBallManager.getActiveList()) {
                if (!(mt.getTile() instanceof RegularTile)) continue;

                int color = mt.getTileID();
                int amount = colorGroups[color].amount;
                if (amount == 2) {
                    maxIndex -= 2;
                    colorGroups[color].amount = 0;
                } else if (amount < 2) {
                    ++colorGroups[color].amount;
                    ++maxIndex;
                }
            }

            for (MovingBall mt : launcher) {
                int color = mt.getTileID();
                int amount = colorGroups[color].amount;
                if (amount == 2) {
                    maxIndex -= 2;
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

                if (enabledCount == 0)
                    throw new IllegalStateException("No enabled IDs"); // XXX(27/4/2018): Needs better implementation cause this is just for indication
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
