package com.archapp.coresmash.levels;

import com.archapp.coresmash.GameController;
import com.archapp.coresmash.RoundEndListener;
import com.archapp.coresmash.UserAccount;
import com.archapp.coresmash.levelbuilder.LevelListParser;
import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.screens.GameScreen;
import com.archapp.coresmash.ui.Annotator;
import com.badlogic.gdx.utils.Align;

public class TutorialLevel extends CampaignLevel {
    private boolean fuck;
    private Annotator annotator;
    private float time;

    public TutorialLevel(int level, UserAccount user, RoundEndListener roundEndListener, Annotator annotator) {
        super(level, user, roundEndListener);
        this.annotator = annotator;
    }

    @Override
    public void initialize(GameController gameController) {
        super.initialize(gameController);
        gameController.loadLevelMap("tutorial1", LevelListParser.Source.INTERNAL);
        gameController.getBehaviourPack().launcher.setDisabled(true);
    }

    @Override
    public void update(float delta, final GameController.BehaviourPack behaviourPack, final GameScreen.GameUI gameUI) {
        if (!fuck) {
            time += delta;
            if (time > .9f) {
                final RoundManager roundManager = behaviourPack.roundManager;
                roundManager.pauseGame();
                annotator.newAnnotation(gameUI.lblMoves)
                        .setText("Amount of shots available")
                        .pad(gameUI.lblMoves.getPrefHeight() * .2f)
                        .align(Align.topLeft)
                        .setCallback(
                                new Annotator.Callback() {
                                    @Override
                                    public void call() {
                                        annotator.newAnnotation(gameUI.lblTargetScore)
                                                .setText("Target score")
                                                .pad(gameUI.lblTargetScore.getPrefHeight() * .6f)
                                                .align(Align.topRight)
                                                .setCallback(new Annotator.Callback() {
                                                    @Override
                                                    public void call() {
                                                        annotator.newAnnotation(gameUI.btnSpeedUp)
                                                                .setText("Hold to increase game speed")
                                                                .align(Align.bottomRight)
                                                                .setCallback(new Annotator.Callback() {
                                                                    @Override
                                                                    public void call() {
                                                                        roundManager.resumeGame();
                                                                        behaviourPack.launcher.setDisabled(false);
                                                                        fuck = true;
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                })
                                                .show();
                                    }
                                }
                        )
                        .show();
            }
        }
    }
}
