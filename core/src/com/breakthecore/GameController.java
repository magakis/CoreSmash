package com.breakthecore;

import com.breakthecore.levelbuilder.LevelParser;
import com.breakthecore.levelbuilder.LevelSettings;
import com.breakthecore.levelbuilder.MapSettings;
import com.breakthecore.levelbuilder.ParsedLevel;
import com.breakthecore.levelbuilder.ParsedTile;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;
import com.breakthecore.tiles.CollisionInitiator;
import com.breakthecore.tiles.MovingBall;
import com.breakthecore.tiles.PowerupType;

import java.util.List;
import java.util.Objects;

/**
 * The GameController is a wrapper of the game logic besides the notification system.
 * <p>
 * <b>THOUGHTS:</b>
 * <p>
 * I feel like this class should handle TilemapManager and MovingBallManager updates so
 * that it can produce better collision results and overall coordination of each component.
 * </p>
 */
public class GameController {
    private TilemapManager tilemapManager;
    private MovingBallManager movingBallManager;
    private CollisionDetector collisionDetector;
    private StatsManager statsManager;
    private BehaviourPack behaviourPowerPack;
    private Launcher launcher;


    public GameController(TilemapManager tilemapManager, MovingBallManager movingBallManager, StatsManager statsManager, Launcher launcher) {
        collisionDetector = new CollisionDetector();
        this.movingBallManager = movingBallManager;
        this.tilemapManager = tilemapManager;
        this.statsManager = statsManager;
        this.launcher = launcher;
        behaviourPowerPack = new BehaviourPack(tilemapManager, movingBallManager, collisionDetector, statsManager);
    }

    // XXX(18/6/2018): Take a look at this method
    public void update(float delta) {
        updateCollisions();
    }

    public void updateCollisions() {
        List<MovingBall> activeBalls = movingBallManager.getActiveList();
        List<TilemapTile> ballList = tilemapManager.getTileList();

        for (MovingBall mb : activeBalls) {
            TilemapTile tileHit = collisionDetector.findCollision(ballList, mb);
            if (tileHit == null) continue;

            if (tileHit.getTile() instanceof CollisionInitiator) {
                if (!((CollisionInitiator) tileHit.getTile()).handleCollisionWith(mb, this)) {
                    mb.getLaunchable().onCollide(mb, tileHit, this);
                }
            } else {
                mb.getLaunchable().onCollide(mb, tileHit, this);
            }
            mb.dispose();
        }
        movingBallManager.disposeInactive();
    }

    public BehaviourPack getBehaviourPack() {
        return behaviourPowerPack;
    }

    public void loadLevel(int lvl) {
        statsManager.setLevel(lvl);
        loadLevelMap("level" + lvl);
    }

    public void loadLevelMap(String fileName) {
        ParsedLevel parsedLevel = LevelParser.loadFrom(fileName);

        LevelSettings levelSettings = Objects.requireNonNull(parsedLevel).getLevelSettings();
        statsManager.setLives(levelSettings.lives);
        statsManager.setMoves(levelSettings.moves);
        statsManager.setTime(levelSettings.time);

        launcher.setLauncherSize(levelSettings.launcherSize);
        launcher.setLauncherCooldown(levelSettings.launcherCooldown);

        movingBallManager.setDefaultBallSpeed(levelSettings.ballSpeed);

        for (int i = 0; i < parsedLevel.getMapCount(); ++i) {
            List<ParsedTile> tileList = parsedLevel.getTiles(i);
            MapSettings settings = parsedLevel.getMapSettings(i);

            if (tileList.size() == 0) continue;

            TilemapBuilder builder = tilemapManager.newLayer();
            builder.setColorCount(settings.getColorCount())
                    .setOffset(settings.getOffset())
                    .setOrigin(settings.getOrigin())
                    .setChained(settings.isChained())
                    .setMinMaxRotationSpeed(settings.getMinSpeed(), settings.getMaxSpeed(), settings.isRotateCCW())
                    .setMapMinMaxRotationSpeed(settings.getMinMapSpeed(), settings.getMaxMapSpeed(), false)
                    .populateFrom(tileList)
                    .build();
        }

    }

    public static class BehaviourPack {
        public final TilemapManager tilemapManager;
        public final MovingBallManager movingBallManager;
        public final CollisionDetector collisionDetector;
        public final StatsManager statsManager;

        private BehaviourPack(TilemapManager manager, MovingBallManager ballManager, CollisionDetector detector, StatsManager statsManager) {
            tilemapManager = manager;
            movingBallManager = ballManager;
            collisionDetector = detector;
            this.statsManager = statsManager;
        }
    }
}
