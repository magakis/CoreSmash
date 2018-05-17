package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.breakthecore.CoreSmash;
import com.breakthecore.Coords2D;
import com.breakthecore.GameController;
import com.breakthecore.Launcher;
import com.breakthecore.levelbuilder.LevelFormatParser;
import com.breakthecore.levelbuilder.LevelSettings;
import com.breakthecore.levelbuilder.MapSettings;
import com.breakthecore.levelbuilder.ParsedLevel;
import com.breakthecore.levelbuilder.ParsedTile;
import com.breakthecore.levels.Level;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.StreakUI;
import com.breakthecore.managers.CollisionDetector;
import com.breakthecore.NotificationType;
import com.breakthecore.Observer;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.MovingBall;
import com.breakthecore.ui.UIComponent;

import java.util.List;
import java.util.Locale;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenBase implements Observer {
    private ExtendViewport viewport;
    private OrthographicCamera camera;
    private RenderManager renderManager;

    private GameScreenController gameScreenController;

    private GameController gameController;
    private TilemapManager tilemapManager;
    private MovingBallManager movingBallManager;
    private StatsManager statsManager;
    private Launcher launcher;

    private StreakUI streakUI;
    private LevelTools levelTools;

    private Level activeLevel;


    //===========
    private DebugUI debugUI;
    private GameUI gameUI;
    private ResultUI m_resultUI;
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
        gameController = new GameController(tilemapManager, movingBallManager);
        levelTools = new LevelTools(tilemapManager, movingBallManager, statsManager, launcher);
        gameScreenController = new GameScreenController(this);

        skin = gameInstance.getSkin();

        streakUI = new StreakUI(skin);
        gameUI = new GameUI();
        m_resultUI = new ResultUI();
        debugUI = new DebugUI();

        statsManager.addObserver(this);
        statsManager.addObserver(streakUI);
        statsManager.addObserver(gameUI);

        launcher.addObserver(statsManager);

        tilemapManager.addObserver(statsManager);

        movingBallManager.addObserver(statsManager);

        stage = new Stage(game.getUIViewport());
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
        draw();
    }

    private void draw() {
        renderManager.start(camera.combined);
        launcher.draw(renderManager);
        tilemapManager.draw(renderManager);
        movingBallManager.draw(renderManager);
        renderManager.end();

        renderManager.renderCenterDot(tilemapManager.getTilemapPosition(), camera.combined);
        stage.draw();
    }

    private void update(float delta) {
        if (statsManager.isGameActive()) {
            if (activeLevel != null) {
                activeLevel.update(delta, tilemapManager);
            }
            launcher.update(delta);
            tilemapManager.update(delta);
            movingBallManager.update(delta);
            gameController.update(delta);

            statsManager.update(delta);
            updateStage();
            checkEndingConditions();
        }
        stage.act(); //Moved out of updateStage() cause it always has to convert called
    }

    private void updateStage() {
        if (statsManager.isTimeEnabled()) {
            float time = statsManager.getTime();
            gameUI.lblTime.setText(String.format("%d:%02d", (int) time / 60, (int) time % 60));
        }
        debugUI.dblb2.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    private void checkEndingConditions() {
        if (!statsManager.isGameActive()) {
            endGame();
        }
        if (statsManager.isTimeEnabled() && statsManager.getTime() < 0) {
            endGame();
        }
        if (statsManager.isLivesEnabled() && statsManager.getLives() == 0) {
            endGame();
        }
        if (statsManager.isMovesEnabled() && statsManager.getMoves() == 0 && movingBallManager.getActiveList().size() == 0) {
            endGame();
        }
    }

    private void endGame() {
        statsManager.stopGame();
        m_resultUI.update();
        rootUIStack.clear();
        rootUIStack.addActor(m_resultUI.getRoot());

        if (activeLevel != null) {
            activeLevel.end(statsManager);
        }
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
        statsManager.setUserAccount(gameInstance.getUserAccount());
        activeLevel = level;
        level.initialize(gameScreenController);
        if (tilemapManager.getTilemapTile(0,0,0) == null) {
            endGame();
            gameInstance.setScreen(this);
            return;
        }
        launcher.fillLauncher(tilemapManager);

        gameUI.setup();

        rootUIStack.addActor(gameUI.getRoot());
        rootUIStack.addActor(streakUI.getRoot());
        rootUIStack.addActor(debugUI.getRoot());

        debugUI.dblb3.setText(String.format(Locale.ENGLISH, "Diff: %.2f", statsManager.getDifficultyMultiplier()));
        gameInstance.setScreen(this);
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

    public GameScreenController getGameScreenController() {
        return gameScreenController;
    }

    private class GameInputListener implements GestureDetector.GestureListener {
        private boolean isPanning;
        private Coords2D tmPos;
        private Vector3 scrPos;
        private float initAngle;
        private Vector2 currPoint;

        public GameInputListener() {
            tmPos = tilemapManager.getTilemapPosition();
            scrPos = new Vector3();
            currPoint = new Vector2();
        }

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            switch (statsManager.getGameMode()) {
                case CLASSIC:
                    launcher.eject();
                    break;
            }
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
            switch (statsManager.getGameMode()) {
                case SPIN_THE_CORE:
                    // FIXME (21/4/2018) : There is a very evident bug here if you try the gamemode and spin it
                    if (statsManager.isGameActive()) {
                        if (isPanning) {
                            float currAngle;
                            scrPos.set(x, y, 0);
                            scrPos = camera.unproject(scrPos);
                            currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                            currAngle = currPoint.angle();
                            tilemapManager.forceRotateLayer(0, (initAngle - currAngle) * 2.5f);
                            initAngle = currAngle;
                        } else {
                            isPanning = true;
                            scrPos.set(x, y, 0);
                            scrPos = camera.unproject(scrPos);
                            currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                            initAngle = currPoint.angle();
                        }
                    }
                    break;

                case SHOOT_EM_UP:
                    MovingBall mt = movingBallManager.getFirstActiveTile();
                    if (mt == null) {
                        launcher.eject();
                        mt = movingBallManager.getFirstActiveTile();
                    }
                    mt.moveBy(deltaX, -deltaY);
                    break;
            }
            return true;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            switch (statsManager.getGameMode()) {
                case SPIN_THE_CORE:
                    isPanning = false;
                    break;
            }
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

    public enum GameMode {
        CLASSIC,
        SPIN_THE_CORE,
        SHOOT_EM_UP,
        DEBUG
    }

    private class GameUI implements UIComponent, Observer {
        Table root, tblPowerUps;
        Table tblTime, tblScore;
        HorizontalGroup grpTop, grpLives, grpMoves;
        Label lblTime, lblScore, lblLives, lblMoves, lblHighscore;
        TextButton tbPower1;
        final Label lblStaticTime, lblStaticScore, lblStaticLives, lblStaticMoves;

        public GameUI() {
            root = new Table();

            lblTime = new Label("0", skin, "comic_48");
            lblTime.setAlignment(Align.center);

            lblScore = new Label("0", skin, "comic_48");
            lblScore.setAlignment(Align.center);

            lblLives = new Label("null", skin, "comic_48");
            lblLives.setAlignment(Align.center);

            lblMoves = new Label("null", skin, "comic_48");
            lblMoves.setAlignment(Align.center);

            lblHighscore = new Label("", skin, "comic_32b");
            lblHighscore.setAlignment(Align.center);

            lblStaticTime = new Label("Time:", skin, "comic_48b");
            lblStaticScore = new Label("Score:", skin, "comic_48b");
            lblStaticLives = new Label("Lives: ", skin, "comic_48b");
            lblStaticMoves = new Label("Moves Left: ", skin, "comic_48b");

            tblTime = new Table();
            tblScore = new Table();

            tblTime.setBackground(skin.getDrawable("box_white_5"));
            tblTime.add(lblTime).center();

            tblScore.setBackground(skin.getDrawable("box_white_5"));
            tblScore.add(lblScore).center();

            grpLives = new HorizontalGroup();
            grpLives.addActor(lblStaticLives);
            grpLives.addActor(lblLives);

            grpMoves = new HorizontalGroup();
            grpMoves.addActor(lblStaticMoves);
            grpMoves.addActor(lblMoves);

            grpTop = new HorizontalGroup();
            grpTop.space(30);

            tbPower1 = new TextButton("null", skin, "tmpPowerup");
            tbPower1.getLabel().setAlignment(Align.bottomLeft);
            tbPower1.getLabelCell().padBottom(5).padLeft(10);
            tbPower1.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    statsManager.consumeSpecialBall(launcher);
                    if (statsManager.getSpecialBallCount() == 0) {
                        tbPower1.setDisabled(true);
                    }
                    tbPower1.setText(String.valueOf(statsManager.getSpecialBallCount()));
                }
            });


            tblPowerUps = new Table();
            tblPowerUps.setBackground(skin.getDrawable("box_white_5"));
            tblPowerUps.center();
            tblPowerUps.add(tbPower1).size(100, 100);

            tblPowerUps.setTouchable(Touchable.enabled);
            tblPowerUps.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            root.setFillParent(true);
        }

        public void setup() {
            root.clear();
            grpTop.clear();

            lblScore.setText(0);
            if (activeLevel != null) {
                lblHighscore.setText(String.valueOf(Gdx.app.getPreferences("account").getInteger("level" + activeLevel.getLevelNumber(), 0)));
            }
            lblLives.setText(String.valueOf(statsManager.getLives()));
            tbPower1.setText(String.valueOf(statsManager.getSpecialBallCount()));
            lblMoves.setText(String.valueOf(statsManager.getMoves()));

            lblStaticTime.setVisible(statsManager.isTimeEnabled());
            tblTime.setVisible(statsManager.isTimeEnabled());

            tblPowerUps.setVisible(statsManager.getSpecialBallCount() != 0);
            tbPower1.setDisabled(statsManager.getSpecialBallCount() == 0);

            if (statsManager.isLivesEnabled()) {
                grpTop.addActor(grpLives);
            }
            if (statsManager.isMovesEnabled()) {
                grpTop.addActor(grpMoves);
            }

            root.top().left();
            root.add(lblStaticTime, grpTop, lblStaticScore);
            root.row();
            root.add(tblTime).width(200).height(100).padLeft(-10);
            root.add().expandX();
            root.add(tblScore).width(200).height(100).padRight(-10).row();
            root.add().colspan(2);
            root.add(new Label("Highscore:", skin, "comic_32b")).row();
            root.add().colspan(2);
            root.add(lblHighscore).row();
            root.add().colspan(2);
            root.add(tblPowerUps).height(300).width(140).expandY().fill().bottom().padBottom(150);
        }

        @Override
        public Group getRoot() {
            return root;
        }

        @Override
        public void onNotify(NotificationType type, Object ob) {
            switch (type) {
                case NOTIFICATION_TYPE_SCORE_INCREMENTED:
                    lblScore.setText(statsManager.getScore());
                    break;
                case NOTIFICATION_TYPE_LIVES_CHANGED:
                    lblLives.setText(statsManager.getLives());
                    break;
                case MOVES_AMOUNT_CHANGED:
                    lblMoves.setText(statsManager.getMoves());
                    break;
            }
        }
    }

    private class ResultUI implements UIComponent {
        Label resultTextLbl, timeLbl, scoreLbl;
        Container<Table> root;

        public ResultUI() {
            Table main = new Table(skin);
            main.background("box_white_5");
            main.pad(40);
            root = new Container<>(main);
            root.setFillParent(true);

            resultTextLbl = new Label("null", skin, "comic_96b");
            timeLbl = new Label("null", skin, "comic_48");
            scoreLbl = new Label("null", skin, "comic_48");

            TextButton tbMenu = new TextButton("Menu", skin);
            tbMenu.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    gameInstance.setPrevScreen();
                }
            });
            tbMenu.getLabelCell().width(200).height(150);

            HorizontalGroup buttonGroup = new HorizontalGroup();
            buttonGroup.align(Align.center);
            buttonGroup.addActor(tbMenu);

            Label staticTime = new Label("Time:", skin, "comic_48b");
            Label staticScore = new Label("Score:", skin, "comic_48b");

            main.center();
            main.add(resultTextLbl).colspan(2).padBottom(50).row();
            main.add(staticTime);
            main.add(staticScore).row();
            main.add(timeLbl);
            main.add(scoreLbl).row();
            main.add(buttonGroup).colspan(2).padTop(100);

        }

        public void update() {
            String resultText = statsManager.getRoundOutcome() ? "Congratulations!" : "You Failed!";
            resultTextLbl.setText(resultText);
            timeLbl.setText(gameUI.lblTime.getText());
            scoreLbl.setText(String.valueOf(statsManager.getScore()));
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
            dblb1 = new Label("", skin, "comic_24b");
            dblb1.setAlignment(Align.left);
            dblb2 = new Label("", skin, "comic_24b");
            dblb2.setAlignment(Align.left);
            dblb3 = new Label("", skin, "comic_24b");
            dblb3.setAlignment(Align.left);
            dblb4 = new Label("", skin, "comic_24b");
            dblb4.setAlignment(Align.left);
            dblb5 = new Label("", skin, "comic_24b");
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

    public static class LevelTools {
        public final TilemapManager tilemapManager;
        public final MovingBallManager movingBallManager;
        public final StatsManager statsManager;
        public final Launcher launcher;

        public LevelTools(TilemapManager manager, MovingBallManager ballManager, StatsManager statsManager, Launcher launcher) {
            tilemapManager = manager;
            movingBallManager = ballManager;
            this.statsManager = statsManager;
            this.launcher = launcher;
        }
    }

    public class GameScreenController {
        GameScreen gameScreen;

        private GameScreenController(GameScreen screen) {
            gameScreen = screen;
        }

        public GameScreenController loadLevel(String filename) {
            ParsedLevel parsedLevel = LevelFormatParser.loadFrom(filename);

            LevelSettings levelSettings = parsedLevel.getLevelSettings();
            statsManager.setGameMode(GameMode.CLASSIC);
            statsManager.setLives(levelSettings.lives);
            statsManager.setMoves(levelSettings.moves);
            statsManager.setTime(levelSettings.time);

            launcher.setLauncherSize(levelSettings.launcherSize);
            launcher.setLauncherCooldown(levelSettings.launcherCooldown);

            movingBallManager.setDefaultBallSpeed(levelSettings.ballSpeed);

            for (int i = 0; i < TilemapManager.MAX_TILEMAP_COUNT; ++i) {
                List<ParsedTile> tileList = parsedLevel.getTiles(i);
                MapSettings settings = parsedLevel.getMapSettings(i);

                if (tileList.size() == 0) break;

                TilemapBuilder builder = tilemapManager.newLayer();
                builder.setColorCount(settings.getColorCount())
                        .setMinMaxRotationSpeed(settings.getMinSpeed(), settings.getMaxSpeed(), settings.isRotateCCW())
                        .populateFrom(tileList)
                        .reduceColorMatches(2)
                        .balanceColorAmounts()
                        .forceEachColorOnEveryRadius()
                        .build();
            }

            return this;
        }
    }

}