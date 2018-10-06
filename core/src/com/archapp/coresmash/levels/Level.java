package com.archapp.coresmash.levels;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.screens.GameScreen;

/**
 * Level is the interface used to manipulate the GameScreen for the needs of the different game levels.
 */

/* If StatManager forces a UserAccount in the future, the Level interface should probably force that too? */
public abstract class Level {

    public abstract void initialize(GameController gameController);

    public abstract void update(float delta, GameController.BehaviourPack behaviourPack, GameScreen.GameUI gameUI);

    public abstract void end(RoundManager.GameStats stats);

    public abstract int getLevelNumber();
}
