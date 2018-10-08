package com.archapp.coresmash.screens;

import com.archapp.coresmash.Coords2D;
import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.GameController;
import com.archapp.coresmash.Launcher;
import com.archapp.coresmash.NotificationType;
import com.archapp.coresmash.Observer;
import com.archapp.coresmash.StreakUI;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.animation.AnimationManager;
import com.archapp.coresmash.levels.Level;
import com.archapp.coresmash.managers.MovingBallManager;
import com.archapp.coresmash.managers.RenderManager;
import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.platform.AdManager;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tiles.Launchable;
import com.archapp.coresmash.tiles.TileType.PowerupType;
import com.archapp.coresmash.ui.UIComponent;
import com.archapp.coresmash.ui.UIFactory;
import com.archapp.coresmash.ui.UIUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.Locale;
import java.util.Objects;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenBase implements Observer {
    private ExtendViewport viewport;
    private OrthographicCamera camera;
    private RenderManager renderManager;

    private GameController gameController;
    private TilemapManager tilemapManager;
    private MovingBallManager movingBallManager;
    private RoundManager roundManager;
    private AnimationManager animationManager;
    private Launcher launcher;

    private StreakUI streakUI;
    private SecondLifeDialog secondLifeDialog;
    private ResultDialog resultDialog;

    private Level activeLevel;
    private boolean speedUp;

    //===========
    private DebugUI debugUI;
    private GameUI gameUI;
    private Skin skin;
    private Stage stage;
    private Stack rootUIStack;
    //===========

    public GameScreen(CoreSmash game) {
        super(game);
        viewport = new ExtendViewport(WorldSettings.getWorldWidth(), WorldSettings.getWorldHeight());
        camera = (OrthographicCamera) viewport.getCamera();
        camera.setToOrtho(false, viewport.getMinWorldWidth(), viewport.getMinWorldHeight());

        animationManager = new AnimationManager();
        renderManager = gameInstance.getRenderManager();
        movingBallManager = new MovingBallManager();
        launcher = new Launcher(movingBallManager);
        tilemapManager = new TilemapManager(animationManager);
        roundManager = new RoundManager();
        gameController = new GameController(tilemapManager, movingBallManager, roundManager, launcher);

        launcher.addObserver(roundManager);
        tilemapManager.addObserver(roundManager);
        movingBallManager.addObserver(roundManager);

        stage = new Stage(game.getUIViewport());
        skin = gameInstance.getSkin();
        streakUI = new StreakUI(skin);
        gameUI = new GameUI();
        resultDialog = new ResultDialog(skin);
        debugUI = new DebugUI();

        secondLifeDialog = new SecondLifeDialog(skin, gameInstance.getAdManager()) {
            @Override
            protected void result(Object object) {
                roundManager.resumeGame();
                if (roundManager.checkEndingConditions(movingBallManager))
                    endGame();
            }
        };

        roundManager.addObserver(this);
        roundManager.addObserver(streakUI);
        roundManager.addObserver(gameUI);

        rootUIStack = new Stack();
        rootUIStack.setFillParent(true);
        stage.addActor(rootUIStack);

        screenInputMultiplexer.addProcessor(stage);
        InputProcessor gameGestureDetector = new CustomGestureDetector(new GameInputListener());
        screenInputMultiplexer.addProcessor(gameGestureDetector);
    }

    @Override
    public void show() {
        super.show();
        SoundManager.get().playGameMusic();
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void hide() {
        super.hide();
        SoundManager.get().playMenuMusic();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void draw() {
        renderManager.spriteBatchBegin(camera.combined);

        launcher.draw(renderManager);
        tilemapManager.draw(renderManager);
        movingBallManager.draw(renderManager);
        renderManager.drawCenterTileIndicator(tilemapManager);
        animationManager.draw(renderManager);

        renderManager.spriteBatchEnd();

        stage.draw();
    }

    private void update(float delta) {
        if (speedUp)
            delta *= 2f;

        if (!roundManager.isGamePaused()) {
            activeLevel.update(delta, gameController.getBehaviourPack(), gameUI);
            launcher.update(delta);
            tilemapManager.update(delta);
            movingBallManager.update(delta);
            gameController.update(delta);

            roundManager.update(delta);
            updateStage();

            if (roundManager.checkEndingConditions(movingBallManager)) {
                roundManager.pauseGame();
                if (!roundManager.isRoundWon()) {
                    /* If the center tile is gone, we can't offer an extra life */
                    if (!roundManager.getGameStats().getReasonOfLoss().equals(RoundManager.ReasonOfLoss.ASTRONAUTS_LEFT)) {
                        if (roundManager.getGameStats().getExtraLivesUsed() < 3)
                            secondLifeDialog.show(stage);
                        else
                            endGame();
                    } else {
                        endGame();
                    }
                } else {
                    endGame();
                }
            }
        }
        animationManager.update(delta);
        stage.act(); // Moved out of updateStage() cause it always has to get called
    }

    private void updateStage() {
        if (roundManager.isTimeEnabled()) {
            float time = roundManager.getTime();
            gameUI.lblTime.setText(String.format(Locale.ENGLISH, "%d:%02d", (int) time / 60, (int) time % 60));
        }
        if (CoreSmash.DEV_MODE)
            debugUI.dblb5.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    private void endGame() {
        roundManager.endGame();
        activeLevel.end(roundManager.getGameStats());
        resultDialog.update();
        resultDialog.show(stage, null);
    }

    private void reset() {
        tilemapManager.reset();
        movingBallManager.reset();
        launcher.reset();
        roundManager.reset();
        streakUI.reset();
        animationManager.reset();

        activeLevel = null;
        rootUIStack.clear();
    }

    public void deployLevel(Level level) {
        reset();
        activeLevel = Objects.requireNonNull(level);
        level.initialize(gameController);

        if (tilemapManager.getTilemapTile(0, 0, 0) == null) {
            roundManager.endGame();
            endGame();
            gameInstance.setScreen(this);
            return;
        }
        launcher.fillLauncher(tilemapManager, roundManager);


        rootUIStack.addActor(gameUI.getRoot());
        rootUIStack.addActor(streakUI.getRoot());
        rootUIStack.addActor(debugUI.getRoot());

        roundManager.start();
        gameUI.setup();

        gameInstance.setScreen(this);
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case BALL_LAUNCHED:
                if (roundManager.isMovesEnabled()) {
                    int moves = roundManager.getMoves();
                    if (moves > launcher.getLauncherSize()) {
                        launcher.loadLauncher(tilemapManager);
                    } else if (moves == launcher.getLauncherSize()) {
                        if (tilemapManager.getCenterTile() instanceof Launchable)
                            launcher.loadLauncher(tilemapManager.getCenterTileID());
                        else
                            launcher.loadLauncher(tilemapManager);
                    }
                } else {
                    launcher.loadLauncher(tilemapManager);
                }
                break;

            case REWARDED_MOVES:
                launcher.fillLauncher(tilemapManager, roundManager);
                break;
        }
    }

    private class GameInputListener implements GestureDetector.GestureListener {
        private boolean isPanning;
        private Coords2D tmPos;
        private Vector3 scrPos;
        private float initAngle;
        private Vector2 currPoint;

        public GameInputListener() {
            tmPos = tilemapManager.getDefTilemapPosition();
            scrPos = new Vector3();
            currPoint = new Vector2();
        }

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            launcher.eject();
            return true;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
//            switch (statsManager.getGameMode()) {
//                case SPIN_THE_CORE:
//                    // FIXME (21/4/2018) : There is a very evident bug here if you try the gamemode and spin it
//                    if (statsManager.isGameActive()) {
//                        if (isPanning) {
//                            float currAngle;
//                            scrPos.set(x, y, 0);
//                            scrPos = camera.unproject(scrPos);
//                            currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
//                            currAngle = currPoint.angle();
//                            initAngle = currAngle;
//                        } else {
//                            isPanning = true;
//                            scrPos.set(x, y, 0);
//                            scrPos = camera.unproject(scrPos);
//                            currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
//                            initAngle = currPoint.angle();
//                        }
//                    }
//                    break;
//
//                case SHOOT_EM_UP:
//                    MovingBall mt = movingBallManager.getFirstActiveTile();
//                    if (mt == null) {
//                        launcher.eject();
//                        mt = movingBallManager.getFirstActiveTile();
//                    }
//                    mt.moveBy(deltaX, -deltaY);
//                    break;
//            }
            return true;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
//            switch (statsManager.getGameMode()) {
//                case SPIN_THE_CORE:
//                    isPanning = false;
//                    break;
//            }
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }

        @Override
        public void pinchStop() {

        }
    }

    private class CustomGestureDetector extends GestureDetector {

        public CustomGestureDetector(GestureListener listener) {
            super(listener);
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                gameInstance.setPrevScreen();
            }
            return false;
        }

    }

    public class GameUI implements UIComponent, Observer {
        Stack root;
        HorizontalGroup movesGroup, livesGroup, highscoreGroup;
        Table tblPowerUps, board;
        public Label lblTime, lblScore, lblLives, lblMoves, lblTargetScore, lblLevel, lblHighscore;
        Container<Label> targetScoreWrapper;
        PowerupButton[] powerupButtons;
        public ImageButton btnSpeedUp;
        Image imgMovesIcon, imgLivesIcon;
        Container<Image> star1, star2, star3;
        HorizontalGroup starsGroup;
        Color colorTargetScore;

        public GameUI() {
            lblTime = new Label("0", skin, "h3s");
            lblTime.setAlignment(Align.left);

            lblScore = new Label("0", skin, "h3s");
            lblLives = new Label("null", skin, "h3s");
            lblMoves = new Label("null", skin, "h3s");

            colorTargetScore = new Color(100 / 255f, 100 / 255f, 130 / 255f, 1);
            lblTargetScore = new Label("", skin, "h5");

            star1 = new Container<>(new Image(skin, "GrayStar"));
            star2 = new Container<>(new Image(skin, "GrayStar"));
            star3 = new Container<>(new Image(skin, "GrayStar"));
            star1.prefSize(lblTargetScore.getPrefHeight());
            star2.prefSize(lblTargetScore.getPrefHeight());
            star3.prefSize(lblTargetScore.getPrefHeight());
            star1.setTransform(true);
            star2.setTransform(true);
            star3.setTransform(true);

            lblLevel = new Label("", skin, "h2");
            lblHighscore = new Label("", skin, "h5", new Color(150 / 255f, 150 / 255f, 90 / 255f, 1));

            imgMovesIcon = new Image(skin.getDrawable("movesIcon"));
            imgLivesIcon = new Image(skin.getDrawable("heartIcon"));

            float sidePadding = 10 * Gdx.graphics.getDensity();
            board = new Table(skin);
            board.defaults().expandX().uniformX();
            board.columnDefaults(0).left().padLeft(sidePadding);
            board.columnDefaults(2).right().padRight(sidePadding);

            Container<Image> imgMovesWrapper = new Container<>(imgMovesIcon);
            imgMovesWrapper.size(lblMoves.getPrefHeight() * .6f);

            movesGroup = new HorizontalGroup();
            movesGroup.addActor(imgMovesWrapper);
            movesGroup.addActor(lblMoves);

            Container<Image> imgLivesWrapper = new Container<>(imgLivesIcon);
            imgLivesWrapper.size(UIUtils.getWidthFor(imgLivesIcon.getDrawable(), lblLives.getPrefHeight() * .6f), lblLives.getPrefHeight() * .6f);

            targetScoreWrapper = new Container<>(lblTargetScore);
            targetScoreWrapper.setTransform(true);

            starsGroup = new HorizontalGroup();
            starsGroup.addActor(star3);
            starsGroup.addActor(star2);
            starsGroup.addActor(star1);

            livesGroup = new HorizontalGroup();
            livesGroup.addActor(imgLivesWrapper);
            livesGroup.addActor(lblLives);

            VerticalGroup targetScoreAndStarsGroup = new VerticalGroup();
            targetScoreAndStarsGroup.columnAlign(Align.topRight);
            targetScoreAndStarsGroup.addActor(targetScoreWrapper);
            targetScoreAndStarsGroup.addActor(starsGroup);

            board.add(movesGroup);
            board.add(lblTime);
            board.add(lblScore);
            board.row();
            board.add(livesGroup);
            board.add();
            board.add(targetScoreAndStarsGroup).top().right()
                    .padTop(-lblTargetScore.getPrefHeight() * .2f)
                    .row();

            // POWERUP TABLE
            powerupButtons = new PowerupButton[3];
            for (int i = 0; i < powerupButtons.length; ++i) {
                final PowerupButton btn = new PowerupButton();
                btn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (roundManager.consumePowerup(btn.type, launcher)) {
                            if (!roundManager.isDebugEnabled()) {
                                gameInstance.getUserAccount().consumePowerup(btn.type);
                            }
                            int usagesLeft = roundManager.getPowerupUsages(btn.type);
                            if (usagesLeft == 0) {
                                btn.setDisabled(true);
                                btn.image.setDrawable(skin.newDrawable(btn.type.name(), Color.DARK_GRAY));
                                btn.text.setColor(Color.DARK_GRAY);
                            }
                            btn.setText(usagesLeft);

                        }
                    }
                });
                powerupButtons[i] = btn;
            }

            tblPowerUps = new Table();
            tblPowerUps.defaults().size(40 * Gdx.graphics.getDensity(), 40 * Gdx.graphics.getDensity()).pad(5 * Gdx.graphics.getDensity());
            tblPowerUps.center();
            tblPowerUps.setTouchable(Touchable.enabled);
            tblPowerUps.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            Container<Table> boardWrapper = new Container<>(board);
            boardWrapper.fillX().top();

            Container<Table> powerupsWrapper = new Container<>(tblPowerUps);
            powerupsWrapper.bottom().right().padBottom(gameInstance.getUIViewport().getScreenHeight() / 4);

            // SPEED-UP BUTTON
            btnSpeedUp = new ImageButton(skin, "ButtonSpeedUp");
            btnSpeedUp.getImageCell().size(75 * Gdx.graphics.getDensity());
            btnSpeedUp.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    speedUp = !speedUp;
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    speedUp = !speedUp;
                }
            });
            Container<ImageButton> speedupWrapper = new Container<>(btnSpeedUp);
            speedupWrapper
                    .bottom()
                    .right()
                    .padBottom(gameInstance.getUIViewport().getScreenHeight() * .08f)
                    .padRight(10 * Gdx.graphics.getDensity());


            Container<Image> imgTrophyWrapper = new Container<>(new Image(skin.getDrawable("Trophy")));
            imgTrophyWrapper.size(lblHighscore.getPrefHeight() * .8f);
            highscoreGroup = new HorizontalGroup();
            highscoreGroup.addActor(imgTrophyWrapper);
            highscoreGroup.addActor(lblHighscore);

            Table levelInfo = new Table(skin);
            levelInfo.bottom().left()
                    .padLeft(10 * Gdx.graphics.getDensity())
                    .padBottom(gameInstance.getUIViewport().getScreenHeight() * .1f);
            levelInfo.add("Level", "h4")
                    .padBottom(lblLevel.getPrefHeight() * -.2f)
                    .row();
            levelInfo.add(lblLevel)
                    .padBottom(lblLevel.getPrefHeight() * -.1f)
                    .row();
            levelInfo.add(highscoreGroup);

            // ASSEMBLE ROOT
            root = new Stack();
            root.addActor(boardWrapper);
            root.addActor(powerupsWrapper);
            root.addActor(speedupWrapper);
            root.addActor(levelInfo);
        }

        public void setup() {
            RoundManager.GameStats stats = roundManager.getGameStats();

//            star1.setVisible(false);
//            star2.setVisible(false);
//            star3.setVisible(false);

            resetStar(star1);
            resetStar(star2);
            resetStar(star3);
            resetActor(targetScoreWrapper);
            resetActor(lblTargetScore);

            int highScore = stats.getUserHighScore();
            lblScore.setText(String.valueOf(0));
            lblTargetScore.setColor(colorTargetScore);
            lblTargetScore.setText(String.valueOf(roundManager.getGameStats().getTargetScoreOne()));
            lblLevel.setText(roundManager.getLevel());
            if (highScore != 0) {
                lblHighscore.setText(String.valueOf(highScore));
                highscoreGroup.setVisible(true);
            } else {
                highscoreGroup.setVisible(false);
            }
            movesGroup.setVisible(roundManager.isMovesEnabled());
            livesGroup.setVisible(roundManager.isLivesEnabled());
            lblTime.setVisible(roundManager.isTimeEnabled());

            lblMoves.setText(String.valueOf(roundManager.getMoves()));
            lblLives.setText(String.valueOf(roundManager.getLives()));
            if (roundManager.isTimeEnabled()) {
                float time = roundManager.getTime();
                lblTime.setText(String.format(Locale.ENGLISH, "%d:%02d", (int) time / 60, (int) time % 60));
            }

            setupPowerups();
        }

        private <T extends Actor & Layout> void resetActor(T actor) {
            actor.clearActions();
            actor.setScale(1);
            actor.invalidateHierarchy();
        }

        private void resetStar(Container<Image> star) {
            star.getActor().setDrawable(skin, "GrayStar");
            resetActor(star);
        }

        private void showStar(Container<Image> star) {
            star.setOrigin(Align.center);
            star.setScale(0);
            star.getActor().setDrawable(skin, "Star");

            float moveAmount = 10 * Gdx.graphics.getDensity();
            star.addAction(Actions.parallel(
                    Actions.scaleTo(1, 1, .4f),
                    Actions.sequence(
                            Actions.moveBy(0, -moveAmount, .2f),
                            Actions.moveBy(0, moveAmount, .2f)
                    )
            ));
        }

        @Override
        public Group getRoot() {
            return root;
        }

        @Override
        public void onNotify(NotificationType type, Object ob) {
            switch (type) {
                case NOTIFICATION_TYPE_SCORE_INCREMENTED:
                    lblScore.setText(String.valueOf(roundManager.getScore()));
                    break;
                case LIVES_AMOUNT_CHANGED:
                    lblLives.setText(String.valueOf(roundManager.getLives()));
                    break;
                case MOVES_AMOUNT_CHANGED:
                    lblMoves.setText(String.valueOf(roundManager.getMoves()));
                    break;
                case TARGET_SCORE_REACHED:
                    updateStarsUnlocked((int) ob);
                    break;
            }
        }

        private void updateStarsUnlocked(final int starsUnlocked) {
            targetScoreWrapper.setOrigin(Align.center);

            if (starsUnlocked != 3) {
                targetScoreWrapper.addAction(Actions.after(Actions.sequence(
                        Actions.parallel(
                                Actions.scaleBy(.7f, .7f, .3f),
                                Actions.run(new Runnable() {
                                    @Override
                                    public void run() {
                                        lblTargetScore.setColor(Color.YELLOW);
                                    }
                                })

                        ),
                        Actions.scaleTo(0, 0, .2f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                if (starsUnlocked == 1) {
                                    lblTargetScore.setText(String.valueOf(roundManager.getGameStats().getTargetScoreTwo()));
                                    showStar(star1);
                                } else {
                                    lblTargetScore.setText(String.valueOf(roundManager.getGameStats().getTargetScoreThree()));
                                    showStar(star2);
                                }
                            }
                        }),
                        Actions.parallel(
                                Actions.scaleTo(1, 1, .3f),
                                Actions.sequence(
                                        Actions.moveBy(0, -10 * Gdx.graphics.getDensity(), .15f),
                                        Actions.moveBy(0, 10 * Gdx.graphics.getDensity(), .15f)
                                )
                        ),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                lblTargetScore.addAction(
                                        Actions.color(colorTargetScore, .5f));
                            }
                        })
                )));
            } else {
                targetScoreWrapper.addAction(Actions.after(
                        Actions.sequence(
                                Actions.parallel(
                                        Actions.scaleBy(.7f, .7f, .3f),
                                        Actions.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                showStar(star3);
                                                lblTargetScore.setColor(Color.YELLOW);
                                            }
                                        })

                                ),
                                Actions.scaleTo(0, 0, .2f),
                                Actions.run(new Runnable() {
                                    @Override
                                    public void run() {
                                        lblTargetScore.setText("");
                                        float height = lblTargetScore.getPrefHeight();
                                        starsGroup.addAction(Actions.moveBy(
                                                0, height, .5f, Interpolation.circleOut
                                        ));
                                    }
                                })
                        )
                ));
            }
        }

        private void setupPowerups() {
            int enabledCount = roundManager.getEnabledPowerupsCount();
            if (enabledCount == 0) {
                tblPowerUps.setVisible(false);
                return;
            }

            tblPowerUps.clearChildren();

            PowerupType[] enabledPowerups = roundManager.getEnabledPowerups();
            for (int i = 0; i < enabledCount; ++i) {
                powerupButtons[i].setPower(enabledPowerups[i], roundManager.getPowerupUsages(enabledPowerups[i]));
                tblPowerUps.add(powerupButtons[i]).row();
                powerupButtons[i].setDisabled(false);
            }

            tblPowerUps.setVisible(true);
        }

        private class PowerupButton extends Button {
            private PowerupType type;
            private Container<Image> imageContainer;
            private Image image;
            private Label text;

            public PowerupButton() {
                super(skin, "ButtonPowerup");

                text = new Label("null", skin, "h5");
                text.setAlignment(Align.bottomLeft);

                image = new Image();
                imageContainer = new Container<>(image);
                imageContainer.size(new Value() {
                    @Override
                    public float get(Actor context) {
                        return UIUtils.getWidthFor(image.getDrawable(), getHeight() * .9f);
                    }
                }, Value.percentHeight(.9f, this));

                stack(new Container<Container>(imageContainer), text).align(Align.center).grow();
            }

            public void setPower(PowerupType type, int count) {
                this.type = type;
                text.setText(String.valueOf(count));
                if (count > 0) {
                    text.setColor(Color.WHITE);
                    image.setDrawable(skin.getDrawable(type.name()));

                } else {

                    text.setColor(Color.DARK_GRAY);
                    image.setDrawable(skin.newDrawable(type.name(), Color.DARK_GRAY));
                    setDisabled(true);
                }
            }

            public void setText(int value) {
                text.setText(String.valueOf(value));
            }
        }
    }

    private class ResultDialog extends Dialog {
        private final String
                outOfMoves = "No more shots left!",
                outOfLives = "No more lives left!",
                outOfTime = "Run out of time!",
                missedTargetScore = "Missed target Score!",
                astronautsLeft = "Astronauts left Unsaved!";
        private Label lblResult, lblScore, lblMessage, staticScore, lblLavel;
        private float contentWidth;
        private Table main;
        private Container<Image> star1, star2, star3;
        private Image completeSign, failSign;
        private Stack signStack;
        private Stack starGroup;
        private Runnable showSignStack;

        ResultDialog(Skin skin) {
            super("", skin, "BlackBackOnly");
            contentWidth = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();

            showSignStack = new Runnable() {
                @Override
                public void run() {
                    signStack.setOrigin(Align.center);
                    signStack.setScale(20);
                    signStack.addAction(Actions.parallel(
                            Actions.fadeIn(.3f, Interpolation.circle),
                            Actions.scaleTo(1, 1, .3f, Interpolation.sine)
                    ));
                }
            };

            lblLavel = UIFactory.createLabel("", skin, "h3", Align.center);
            lblLavel.setColor(new Color(153f / 255f, 46f / 255f, 103f / 255f, 1));
            staticScore = new Label("Score:", skin, "h3");
            lblResult = new Label("null", skin, "h3");
            lblResult.setColor(255f / 255f, 100f / 255f, 100f / 255f, 1);
            lblScore = new Label("null", skin, "h5");
            lblMessage = new Label("", skin, "h4");
            lblMessage.setAlignment(Align.center);

            ImageButton btnMenu = UIFactory.createImageButton(skin, "ButtonHomeEndScreen");
            btnMenu.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    gameInstance.setPrevScreen();
                    hide(null);
                }
            });

            completeSign = new Image(skin, "MissionCompleteSign");
            failSign = new Image(skin, "MissionFailedSign");

            signStack = new Stack(completeSign, failSign);
            signStack.setTransform(true);
            Container<Stack> signWrapper = new Container<>(signStack);
            signWrapper.setTransform(true);
            signWrapper
                    .padTop(Value.percentHeight(.18f, this))
                    .padRight(Value.percentWidth(.14f, this))
                    .size(contentWidth * .13f);

            Container<Container<Stack>> signStackWrapper = new Container<>(signWrapper);
            signStackWrapper.top().right();

            star1 = new Container<>(new Image(skin, "GrayStar"));
            star2 = new Container<>(new Image(skin, "GrayStar"));
            star3 = new Container<>(new Image(skin, "GrayStar"));
            float starScale = contentWidth * .13f;
            star1.prefSize(1f * starScale).bottom();
            star2.prefSize(1f * starScale).bottom();
            star3.prefSize(1.6f * starScale).bottom();
            star1.padRight(star3.getPrefWidth() * 1.4f);
            star2.padLeft(star3.getPrefWidth() * 1.4f);
            starGroup = new Stack();
            starGroup.addActor(star1);
            starGroup.addActor(star2);
            starGroup.addActor(star3);

            Image background = new Image(skin, "DialogSelectPowerups");
            main = new Table(skin);
            main.defaults().expandX();

            Stack rootStack = new Stack(background, main, signStackWrapper);

            getContentTable()
                    .add(rootStack)
                    .height(UIUtils.getHeightFor(background.getDrawable(), contentWidth))
                    .width(contentWidth);

            float buttonSize = WorldSettings.getDefaultButtonHeight() * 1.2f;
            Table buttons = getButtonTable();
            buttons.add(btnMenu)
                    .width(UIUtils.getWidthFor(btnMenu.getImage().getDrawable(), buttonSize))
                    .height(buttonSize);
            getCell(buttons)
                    .padTop(-buttonSize)
                    .maxWidth(contentWidth);

            setResizable(false);
            setMovable(false);
            setClip(false);
        }

        @Override
        public Dialog show(Stage stage, Action action) {
            super.show(stage, action);
            setPosition(stage.getWidth() / 2, stage.getHeight() / 2, Align.center);
            return this;
        }

        public void update() {
            String resultText = roundManager.isRoundWon() ? "Complete" : "Failed";
            switch (roundManager.getGameStats().getReasonOfLoss()) {
                case NONE:
                    lblMessage.setText("");
                    break;
                case ASTRONAUTS_LEFT:
                    lblMessage.setText(astronautsLeft);
                    break;
                case MISSED_TARGET_SCORE:
                    lblMessage.setText(missedTargetScore);
                    break;
                case OUT_OF_MOVES:
                    lblMessage.setText(outOfMoves);
                    break;
                case OUT_OF_LIVES:
                    lblMessage.setText(outOfLives);
                    break;
                case OUT_OF_TIME:
                    lblMessage.setText(outOfTime);
                    break;
            }
            lblResult.setText(resultText);
            lblScore.setText(String.valueOf(roundManager.getScore()));
            lblLavel.setText("Level " + roundManager.getLevel());

            Table main = this.main;
            main.clear();
            main.top()
                    .add(lblLavel)
                    .expandX()
                    .center()
                    .padTop(contentWidth * .04f)
                    .row();

            main.row()
                    .height(starGroup.getPrefHeight())
                    .padTop(contentWidth * .22f);
            if (roundManager.isRoundWon()) {
                main.add(starGroup)
                        .padTop(contentWidth * .22f)
                        .expandX()
                        .row();
                int starsGained = roundManager.getGameStats().getStarsUnlocked();
                switch (starsGained) {
                    case 1:
                        star1.getActor().setDrawable(skin, "Star");
                        star2.getActor().setDrawable(skin, "GrayStar");
                        star3.getActor().setDrawable(skin, "GrayStar");
                        break;
                    case 2:
                        star1.getActor().setDrawable(skin, "Star");
                        star2.getActor().setDrawable(skin, "Star");
                        star3.getActor().setDrawable(skin, "GrayStar");
                        break;
                    case 3:
                        star1.getActor().setDrawable(skin, "Star");
                        star2.getActor().setDrawable(skin, "Star");
                        star3.getActor().setDrawable(skin, "Star");
                        break;
                }

                lblResult.setColor(100 / 255f, 255f / 255f, 100f / 255f, 1);
                showSign(completeSign);
            } else {
                main.add(lblMessage)
                        .row();

                lblResult.setColor(255f / 255f, 100f / 255f, 100f / 255f, 1);
                showSign(failSign);
            }

            main.add(lblResult)
                    .row();
            main.add(staticScore)
                    .padTop(Value.percentHeight(-.2f))
                    .row();
            main.add(lblScore).row();

            main.invalidateHierarchy();
        }


        private void showSign(final Actor sign) {
            completeSign.clearActions();
            failSign.clearActions();
            completeSign.setVisible(sign == completeSign);
            failSign.setVisible(sign == failSign);

            signStack.setColor(1, 1, 1, 0);
            signStack.addAction(Actions.run(showSignStack));
        }
    }

    private class DebugUI implements UIComponent {
        private Label dblb1, dblb2, dblb3, dblb4, dblb5;
        Table root;

        public DebugUI() {
            root = new Table();
            dblb1 = new Label("", skin, "h6");
            dblb1.setAlignment(Align.left);
            dblb2 = new Label("", skin, "h6");
            dblb2.setAlignment(Align.left);
            dblb3 = new Label("", skin, "h6");
            dblb3.setAlignment(Align.left);
            dblb4 = new Label("", skin, "h6");
            dblb4.setAlignment(Align.left);
            dblb5 = new Label("", skin, "h6");
            dblb5.setAlignment(Align.left);

            root.bottom().left();
            root.add(dblb1).fillX().row();
            root.add(dblb2).fillX().row();
            root.add(dblb3).fillX().row();
            root.add(dblb4).fillX().row();
            root.add(dblb5).fillX();

        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class SecondLifeDialog extends Dialog {
        private float contentSize;
        private float buttonSize;

        private ImageButton btnPlayOnFree, btnPlayOnAd, btnGiveUp;

        public SecondLifeDialog(Skin skin, final AdManager adManager) {
            super("", skin, "PickPowerUpDialog");

            Label title, description;
            title = UIFactory.createLabel("Oh no!", skin, "h3", Align.center);
            description = UIFactory.createLabel("You are about to lose! Here, I will give you a helping hand if you choose so.", skin, "h4", Align.center);
            description.setWrap(true);

            btnPlayOnFree = UIFactory.createImageButton(skin, "ButtonPlayOnFree");
            btnPlayOnFree.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    roundManager.giveExtraLife(5, 1, 30);
                    hide(null);
                    result(null);
                }
            });

            btnPlayOnAd = UIFactory.createImageButton(skin, "ButtonPlayOnAd");
            btnPlayOnAd.addListener(new ChangeListener() {
                AdManager.AdRewardListener listener = new AdManager.AdRewardListener() {
                    @Override
                    public void reward(String type, int amount) {
                        roundManager.giveExtraLife(5, 1, 30);
                        hide(null);
                        result(null);
                    }

                    @Override
                    public void canceled() {

                    }
                };

                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    adManager.showAdForReward(listener, AdManager.VideoAdRewardType.EXTRA_LIFE);
                }
            });

            btnGiveUp = UIFactory.createImageButton(skin, "ButtonGiveUp");
            btnGiveUp.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide(null);
                    result(null);
                }
            });

            contentSize = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();


            Table content = getContentTable();
            getCell(content).width(contentSize);
            content.add(title).padBottom(contentSize * .02f).row();
            content.add(description).width(contentSize).row();
            content.padBottom(contentSize * .1f);

            buttonSize = WorldSettings.getDefaultButtonHeight();
            Table buttons = getButtonTable();
            getCell(buttons).width(contentSize);
        }

        @Override
        public Dialog show(Stage stage, Action action) {
            Table buttons = getButtonTable();
            buttons.clearChildren();
            buttons.row()
                    .padBottom(contentSize * .02f);
            if (roundManager.isSecondLifeAvailable()) {
                addButton(btnPlayOnFree, 1.1f);
                roundManager.consumeSecondLife();
            } else {
                addButton(btnPlayOnAd);
            }
            buttons.row();
            addButton(btnGiveUp, .8f).row();

            return super.show(stage, action);
        }

        private Cell addButton(ImageButton btn) {
            return addButton(btn, 1);
        }

        private Cell addButton(ImageButton btn, float percent) {
            return getButtonTable().add(btn).height(buttonSize * percent);
        }
    }
}