package com.archapp.coresmash.screens;

import com.archapp.coresmash.Coords2D;
import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.GameController;
import com.archapp.coresmash.Launcher;
import com.archapp.coresmash.NotificationType;
import com.archapp.coresmash.Observer;
import com.archapp.coresmash.StreakUI;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.levels.Level;
import com.archapp.coresmash.managers.MovingBallManager;
import com.archapp.coresmash.managers.RenderManager;
import com.archapp.coresmash.managers.StatsManager;
import com.archapp.coresmash.tilemap.TilemapManager;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
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
    private StatsManager statsManager;
    private Launcher launcher;

    private StreakUI streakUI;
    private Level activeLevel;

    //===========
    private DebugUI debugUI;
    private GameUI gameUI;
    private ResultUI resultUI;
    private Skin skin;
    private Stage stage;
    private Stack rootUIStack;
    //===========

    public GameScreen(CoreSmash game) {
        super(game);
        viewport = new ExtendViewport(WorldSettings.getWorldWidth(), WorldSettings.getWorldHeight());
        camera = (OrthographicCamera) viewport.getCamera();
        camera.setToOrtho(false, viewport.getMinWorldWidth(), viewport.getMinWorldHeight());

        renderManager = gameInstance.getRenderManager();
        movingBallManager = new MovingBallManager();
        launcher = new Launcher(movingBallManager);
        tilemapManager = new TilemapManager();
        statsManager = new StatsManager();
        gameController = new GameController(tilemapManager, movingBallManager, statsManager, launcher);

        launcher.addObserver(statsManager);
        tilemapManager.addObserver(statsManager);
        movingBallManager.addObserver(statsManager);

        stage = new Stage(game.getUIViewport());
        skin = gameInstance.getSkin();
        streakUI = new StreakUI(skin);
        gameUI = new GameUI();
        resultUI = new ResultUI();
        debugUI = new DebugUI();

        statsManager.addObserver(this);
        statsManager.addObserver(streakUI);
        statsManager.addObserver(gameUI);

        rootUIStack = new Stack();
        rootUIStack.setFillParent(true);
        stage.addActor(rootUIStack);

        screenInputMultiplexer.addProcessor(stage);
        InputProcessor gameGestureDetector = new CustomGestureDetector(new GameInputListener());
        screenInputMultiplexer.addProcessor(gameGestureDetector);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw(delta);
    }

    private void draw(float delta) {
        renderManager.spriteBatchBegin(camera.combined);

        launcher.draw(renderManager);
        tilemapManager.draw(renderManager);
        movingBallManager.draw(renderManager);
        renderManager.drawCenterTileIndicator(tilemapManager);

        renderManager.spriteBatchEnd();

        stage.draw();
    }

    private void update(float delta) {
        if (statsManager.isGameActive()) {
            activeLevel.update(delta, tilemapManager);
            launcher.update(delta);
            tilemapManager.update(delta);
            movingBallManager.update(delta);
            gameController.update(delta);

            statsManager.update(delta);
            updateStage();

            if (statsManager.checkEndingConditions(movingBallManager)) {
                endGame();
            }
        }
        stage.act(); // Moved out of updateStage() cause it always has to convert called
    }

    private void updateStage() {
        if (statsManager.isTimeEnabled()) {
            float time = statsManager.getTime();
            gameUI.lblTime.setText(String.format(Locale.ENGLISH, "%d:%02d", (int) time / 60, (int) time % 60));
        }
        debugUI.dblb2.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    private void endGame() {
        activeLevel.end(statsManager.getGameStats());

        resultUI.update();
        rootUIStack.clear();
        rootUIStack.addActor(resultUI.getRoot());
    }

    private void reset() {
        tilemapManager.reset();
        movingBallManager.reset();
        launcher.reset();
        statsManager.reset();
        streakUI.reset();

        activeLevel = null;
        rootUIStack.clear();
    }

    public void deployLevel(Level level) {
        reset();
        activeLevel = Objects.requireNonNull(level);
        level.initialize(gameController);

        if (tilemapManager.getTilemapTile(0, 0, 0) == null) {
            statsManager.stopGame();
            endGame();
            gameInstance.setScreen(this);
            return;
        }
        launcher.fillLauncher(tilemapManager);

        gameUI.setup();

        rootUIStack.addActor(gameUI.getRoot());
        rootUIStack.addActor(streakUI.getRoot());
        rootUIStack.addActor(debugUI.getRoot());

        gameInstance.setScreen(this);
        statsManager.start();
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case BALL_LAUNCHED:
                if (statsManager.isMovesEnabled()) {
                    int moves = statsManager.getMoves();
                    if (moves > launcher.getLauncherSize()) {
                        launcher.loadLauncher(tilemapManager);
                    } else if (moves == launcher.getLauncherSize()) {
                        launcher.loadLauncher(tilemapManager.getCenterTileID());
                    }
                } else {
                    launcher.loadLauncher(tilemapManager);
                }
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
            if (statsManager.isGameActive())
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

    private class GameUI implements UIComponent, Observer {
        Stack root;

        Table tblPowerUps, board;
        Table tblTime, tblScore;
        Container<Table> boardRoot, centerTable;
        Table tblCenter;
        Label lblTime, lblScore, lblLives, lblMoves, lblTargetScore;
        PowerupButton[] powerupButtons;
        Label lblStaticLives;
        Image imgHourGlass, imgMovesIcon, imgLivesIcon;

        public GameUI() {
            lblTime = new Label("0", skin, "h4");
            lblTime.setAlignment(Align.left);

            lblScore = new Label("0", skin, "h4");
            lblScore.setAlignment(Align.right);

            lblLives = new Label("null", skin, "h4");
            lblLives.setAlignment(Align.center);

            lblMoves = new Label("null", skin, "h4");
            lblMoves.setAlignment(Align.center);

            lblTargetScore = new Label("", skin, "h5", new Color(61 / 255f, 61 / 255f, 92 / 255f, 1));
            lblTargetScore.setAlignment(Align.center);

            lblStaticLives = new Label("Lives: ", skin, "h4");

            imgHourGlass = new Image(skin.getDrawable("timeIcon"));
            imgHourGlass.setScaling(Scaling.fit);

            imgMovesIcon = new Image(skin.getDrawable("movesIcon"));
            imgMovesIcon.setScaling(Scaling.fit);

            imgLivesIcon = new Image(skin.getDrawable("heartIcon"));
            imgLivesIcon.setScaling(Scaling.fit);

            tblScore = new Table(skin);
            tblScore.background("BoardScore");
            tblScore.row().padTop(Value.percentHeight(.4f, lblScore)).right();
            tblScore.add(lblScore);
            tblScore.add("/", "h4");
            tblScore.add(lblTargetScore).bottom();


            tblTime = new Table(skin);
            tblTime.setBackground("BoardTime");
            tblTime.row().padTop(Value.percentHeight(.4f, lblTime)).right();
            tblTime.add(imgHourGlass)
                    .size(Value.percentHeight(.9f, lblTime))
                    .padRight(Value.percentHeight(0.2f, lblTime));
            tblTime.add(lblTime);

            tblCenter = new Table();
            tblCenter.padTop(Value.percentHeight(.5f, lblScore))
                    .columnDefaults(1).width(lblLives.getPrefHeight() * 1.4f).right();

            centerTable = new Container<>(tblCenter);
            centerTable.setBackground(skin.getDrawable("BoardCenter"));

            board = new Table(skin);
            board.columnDefaults(0).expandX().padBottom(lblTime.getPrefHeight() / 6);
            board.columnDefaults(1);
            board.columnDefaults(2).expandX().padBottom(lblScore.getPrefHeight() / 6);

            Container<Table> timeWrapper = new Container<>(tblTime);
            timeWrapper.height(Value.percentHeight(2, lblTime)).width(new Value() {
                @Override
                public float get(Actor context) {
                    return UIUtils.getWidthFor(tblTime.getBackground(), lblTime.getPrefHeight() * 2f);
                }
            });
            board.add(timeWrapper).right();

            board.add(centerTable).height(Value.percentHeight(1.6f, board)).width(new Value() {
                @Override
                public float get(Actor context) {
                    return UIUtils.getWidthFor(centerTable.getBackground(), board.getHeight() * 1.6f);
                }
            }).padTop(Value.percentHeight(-.5f, lblScore));

            Container<Table> scoreWrapper = new Container<>(tblScore);
            scoreWrapper.height(Value.percentHeight(2, lblScore)).width(new Value() {
                @Override
                public float get(Actor context) {
                    return UIUtils.getWidthFor(tblScore.getBackground(), lblScore.getPrefHeight() * 2);
                }
            });
            board.add(scoreWrapper).left();

            board.validate(); // Important call! Required for tblScore to have correct values on size

            powerupButtons = new PowerupButton[3];
            for (int i = 0; i < powerupButtons.length; ++i) {
                final PowerupButton btn = new PowerupButton();
                btn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (statsManager.consumePowerup(btn.type, launcher)) {
                            if (!statsManager.isDebugEnabled()) {
                                gameInstance.getUserAccount().consumePowerup(btn.type);
                            }
                            int usagesLeft = statsManager.getPowerupUsages(btn.type);
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
            tblPowerUps.defaults().size(60 * Gdx.graphics.getDensity(), 60 * Gdx.graphics.getDensity()).pad(3 * Gdx.graphics.getDensity());
            tblPowerUps.center();
            tblPowerUps.setTouchable(Touchable.enabled);
            tblPowerUps.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            float boardHeight = tblScore.getHeight() * 1.28f; // Prior call to board.validate() is required!
            boardRoot = new Container<>(board);
            boardRoot.fill().height(boardHeight);

            Container<Container> boardWrapper = new Container<Container>(boardRoot);
            boardWrapper.top();

            Container<Table> powerupsWrapper = new Container<>(tblPowerUps);
            powerupsWrapper.center().right();

            root = new Stack();
            root.addActor(boardWrapper);
            root.addActor(powerupsWrapper);
        }

        public void setup() {
            lblScore.setText(String.valueOf(0));
            lblTargetScore.setText(String.valueOf(statsManager.getTargetScore()));
            lblLives.setText(String.valueOf(statsManager.getLives()));
            lblMoves.setText(String.valueOf(statsManager.getMoves()));

            tblCenter.clear();

            if (statsManager.isMovesEnabled() && statsManager.isLivesEnabled()) {
                tblCenter.add(imgMovesIcon).size(lblLives.getPrefHeight());
                tblCenter.add(lblMoves).left();
                tblCenter.row().padTop(Value.percentHeight(.2f, lblLives));
                tblCenter.add(imgLivesIcon).size(lblLives.getPrefHeight());
                tblCenter.add(lblLives).left();
            } else {
                if (statsManager.isLivesEnabled()) {
                    tblCenter.add(imgLivesIcon).size(lblLives.getPrefHeight());
                    tblCenter.add(lblLives).left();
                }
                if (statsManager.isMovesEnabled()) {
                    tblCenter.add(imgMovesIcon).size(lblMoves.getPrefHeight());
                    tblCenter.add(lblMoves).left();
                }
            }

            imgMovesIcon.setVisible(statsManager.isMovesEnabled());
            lblMoves.setVisible(statsManager.isMovesEnabled());

            imgLivesIcon.setVisible(statsManager.isLivesEnabled());
            lblLives.setVisible(statsManager.isLivesEnabled());

            tblTime.setVisible(statsManager.isTimeEnabled());
            float time = statsManager.getTime();
            lblTime.setText(String.format(Locale.ENGLISH, "%d:%02d", (int) time / 60, (int) time % 60));

            setupPowerups();

            lblMoves.invalidateHierarchy();
        }

        @Override
        public Group getRoot() {
            return root;
        }

        @Override
        public void onNotify(NotificationType type, Object ob) {
            switch (type) {
                case NOTIFICATION_TYPE_SCORE_INCREMENTED:
                    lblScore.setText(String.valueOf(statsManager.getScore()));
                    break;
                case NOTIFICATION_TYPE_LIVES_CHANGED:
                    lblLives.setText(String.valueOf(statsManager.getLives()));
                    break;
                case MOVES_AMOUNT_CHANGED:
                    lblMoves.setText(String.valueOf(statsManager.getMoves()));
                    break;
            }
        }

        private void setupPowerups() {
            int enabledCount = statsManager.getEnabledPowerupsCount();
            if (enabledCount == 0) {
                tblPowerUps.setVisible(false);
                return;
            }

            tblPowerUps.clearChildren();

            PowerupType[] enabledPowerups = statsManager.getEnabledPowerups();
            for (int i = 0; i < enabledCount; ++i) {
                powerupButtons[i].setPower(enabledPowerups[i], statsManager.getPowerupUsages(enabledPowerups[i]));
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

    private class ResultUI implements UIComponent {
        Label resultTextLbl, lblScore;
        Container<Table> root;

        ResultUI() {
            resultTextLbl = new Label("null", skin, "h2");
            lblScore = new Label("null", skin, "h3");

            ImageButton btnMenu = UIFactory.createImageButton(skin, "ButtonMenu");
            btnMenu.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    gameInstance.setPrevScreen();
                }
            });


            float buttonSize = lblScore.getPrefHeight() * 2;
            Container<ImageButton> menuButtonWrapper = new Container<>(btnMenu);
            menuButtonWrapper.size(UIUtils.getWidthFor(btnMenu.getImage().getDrawable(), buttonSize), buttonSize);

            Label staticScore = new Label("Score:", skin, "h3");

            Table main = new Table(skin);
            main.background("simpleFrameTrans")
                    .pad(40)
                    .center();
            main.add(resultTextLbl).padBottom(40).row();
            main.add(staticScore).padBottom(10).row();
            main.add(lblScore).row();
            main.add(menuButtonWrapper).padTop(100);

            root = new Container<>(main);
            root.setFillParent(true);
        }

        public void update() {
            String resultText = statsManager.getRoundOutcome() ? "Level Completed!" : "You Failed!";
            resultTextLbl.setText(resultText);
            lblScore.setText(String.valueOf(statsManager.getScore()));
        }

        @Override
        public Group getRoot() {
            return root;
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

}