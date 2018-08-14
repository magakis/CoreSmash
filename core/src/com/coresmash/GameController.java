package com.coresmash;

import com.coresmash.levelbuilder.LevelListParser;
import com.coresmash.levelbuilder.LevelParser;
import com.coresmash.levelbuilder.LevelSettings;
import com.coresmash.levelbuilder.MapSettings;
import com.coresmash.levelbuilder.ParsedLevel;
import com.coresmash.levelbuilder.ParsedTile;
import com.coresmash.managers.CollisionDetector;
import com.coresmash.managers.MovingBallManager;
import com.coresmash.managers.StatsManager;
import com.coresmash.tilemap.TilemapBuilder;
import com.coresmash.tilemap.TilemapManager;
import com.coresmash.tilemap.TilemapTile;
import com.coresmash.tiles.CollisionInitiator;
import com.coresmash.tiles.MovingBall;

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
    private com.coresmash.Launcher launcher;


    public GameController(TilemapManager tilemapManager, MovingBallManager movingBallManager, StatsManager statsManager, com.coresmash.Launcher launcher) {
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

//    public void loadLevel(int lvl) {
//        statsManager.setLevel(lvl);
//        loadLevelMap("level" + lvl);
//    }

    public void loadLevelMap(String fileName, LevelListParser.Source source) {
        ParsedLevel parsedLevel = LevelParser.loadFrom(fileName, source);

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
