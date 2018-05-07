package com.breakthecore;

import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tilemap.TilemapTile;
import com.breakthecore.tiles.MovingBall;

import java.util.List;

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
    private BehaviourPowerPack behaviourPowerPack;


    public GameController(TilemapManager tilemapManager, MovingBallManager movingBallManager) {
        collisionDetector = new CollisionDetector();
        this.movingBallManager = movingBallManager;
        this.tilemapManager = tilemapManager;
        behaviourPowerPack = new BehaviourPowerPack(tilemapManager,movingBallManager, collisionDetector);
    }

    public void update(float delta) {
        List<MovingBall> activeBalls = movingBallManager.getActiveList();

        for (MovingBall mb : activeBalls) {
            TilemapTile tileHit = collisionDetector.checkIfBallCollides(mb, tilemapManager);
            if (tileHit == null) continue;

            mb.getTile().onCollide(mb, tileHit, behaviourPowerPack);
            movingBallManager.dispose(mb);
        }
    }

    private void resolveCollisionOf(MovingBall movingBall, TilemapTile tileHit) {
    }

    public static class BehaviourPowerPack {
        public final TilemapManager tilemapManager;
        public final MovingBallManager movingBallManager;
        public final CollisionDetector collisionDetector;

        private BehaviourPowerPack(TilemapManager manager, MovingBallManager ballManager, CollisionDetector detector) {
            tilemapManager = manager;
            movingBallManager = ballManager;
            collisionDetector = detector;
        }
    }
}
