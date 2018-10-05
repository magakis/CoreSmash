package com.archapp.coresmash;

import com.archapp.coresmash.levelbuilder.LevelListParser;
import com.archapp.coresmash.levelbuilder.LevelParser;
import com.archapp.coresmash.levelbuilder.LevelSettings;
import com.archapp.coresmash.levelbuilder.MapSettings;
import com.archapp.coresmash.levelbuilder.ParsedLevel;
import com.archapp.coresmash.levelbuilder.ParsedTile;
import com.archapp.coresmash.managers.CollisionDetector;
import com.archapp.coresmash.managers.MovingBallManager;
import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.tilemap.TilemapBuilder;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.CollisionInitiator;
import com.archapp.coresmash.tiles.MovingBall;

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
    private RoundManager roundManager;
    private BehaviourPack behaviourPowerPack;
    private Launcher launcher;


    public GameController(TilemapManager tilemapManager, MovingBallManager movingBallManager, RoundManager roundManager, Launcher launcher) {
        collisionDetector = new CollisionDetector();
        this.movingBallManager = movingBallManager;
        this.tilemapManager = tilemapManager;
        this.roundManager = roundManager;
        this.launcher = launcher;
        behaviourPowerPack = new BehaviourPack(tilemapManager, movingBallManager, collisionDetector, roundManager);
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
                if (!((CollisionInitiator) tileHit.getTile()).handleCollisionWith(tileHit, mb, this)) {
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

    public void loadLevelMap(String fileName, LevelListParser.Source source) {
        ParsedLevel parsedLevel = LevelParser.loadFrom(fileName, source);

        LevelSettings levelSettings = Objects.requireNonNull(parsedLevel).getLevelSettings();
        roundManager.setLives(levelSettings.livesLimit);
        roundManager.setMoves(levelSettings.movesLimit);
        roundManager.setTime(levelSettings.timeLimit);

        roundManager.setTargetScoreOne(levelSettings.targetScores.one);
        roundManager.setTargetScoreTwo(levelSettings.targetScores.two);
        roundManager.setTargetScoreThree(levelSettings.targetScores.three);

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
        public final RoundManager roundManager;

        private BehaviourPack(TilemapManager manager, MovingBallManager ballManager, CollisionDetector detector, RoundManager roundManager) {
            tilemapManager = manager;
            movingBallManager = ballManager;
            collisionDetector = detector;
            this.roundManager = roundManager;
        }
    }
}
