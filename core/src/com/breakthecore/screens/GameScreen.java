package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.breakthecore.Coords2D;
import com.breakthecore.CoreSmash;
import com.breakthecore.GameController;
import com.breakthecore.Launcher;
import com.breakthecore.NotificationType;
import com.breakthecore.Observer;
import com.breakthecore.StreakUI;
import com.breakthecore.WorldSettings;
import com.breakthecore.levelbuilder.LevelParser;
import com.breakthecore.levelbuilder.LevelSettings;
import com.breakthecore.levelbuilder.MapSettings;
import com.breakthecore.levelbuilder.ParsedLevel;
import com.breakthecore.levelbuilder.ParsedTile;
import com.breakthecore.levels.Level;
import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tiles.MovingBall;
import com.breakthecore.ui.UIComponent;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        gameController = new GameController(tilemapManager, movingBallManager);
        gameScreenController = new GameScreenController(this);

        skin = gameInstance.getSkin();

        streakUI = new StreakUI(skin);
        gameUI = new GameUI();
        resultUI = new ResultUI();
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

        renderManager.renderCenterDot(tilemapManager.getDefTilemapPosition(), camera.combined);
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
        stage.act(); //Moved out of updateStage() cause it always has to convert called
    }

    private void updateStage() {
        if (statsManager.isTimeEnabled()) {
            float time = statsManager.getTime();
            gameUI.lblTime.setText(String.format(Locale.ENGLISH, "%d:%02d", (int) time / 60, (int) time % 60));
        }
        debugUI.dblb2.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    private void endGame() {
        activeLevel.end(statsManager);

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
        level.initialize(gameScreenController);
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
        Table root, tblPowerUps, tblTop;
        Table tblTime, tblScore;
        Table tblCenter;
        Label lblTime, lblScore, lblLives, lblMoves, lblTargetScore;
        TextButton tbPower1;
        Label lblStaticLives;
        Image imgHourGlass, imgMovesIcon, imgLivesIcon, imgRound;

        public GameUI() {
            lblTime = new Label("0", skin, "h4");
            lblTime.setAlignment(Align.left);

            lblScore = new Label("0", skin, "h4");
            lblScore.setAlignment(Align.right);

            lblLives = new Label("null", skin, "h4");
            lblLives.setAlignment(Align.center);

            lblMoves = new Label("null", skin, "h4");
            lblMoves.setAlignment(Align.center);

            lblTargetScore = new Label("", skin, "h5", Color.GRAY);
            lblTargetScore.setAlignment(Align.center);

            lblStaticLives = new Label("Lives: ", skin, "h4");

            imgHourGlass = new Image(skin.getDrawable("timeIcon"));
            imgHourGlass.setScaling(Scaling.fit);

            imgMovesIcon = new Image(skin.getDrawable("movesIcon"));
            imgMovesIcon.setScaling(Scaling.fit);

            imgLivesIcon = new Image(skin.getDrawable("heartIcon"));
            imgLivesIcon.setScaling(Scaling.fit);

            tblScore = new Table(skin);
            tblScore.background("softGray");
            tblScore.pad(Value.percentHeight(.25f, lblScore)).right();
            tblScore.add(lblScore).right().width(lblScore.getPrefHeight() * 2);
            tblScore.add("/", "h4");
            tblScore.add(lblTargetScore).padRight(Value.percentHeight(.4f, lblScore));


            tblTime = new Table(skin);
            tblTime.setBackground("softGray");
            tblTime.pad(Value.percentHeight(.2f, lblTime))
                    .padLeft(Value.percentHeight(.4f, lblScore));
            tblTime.add(imgHourGlass)
                    .size(Value.percentHeight(.9f, lblTime))
                    .padRight(Value.percentHeight(0.2f, lblTime));
            tblTime.add(lblTime);

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

            tblCenter = new Table(skin);
            tblCenter.background("gameScreenTopRound");
            tblCenter.bottom();
            tblCenter.columnDefaults(0).padRight(Value.percentHeight(.1f, lblLives)).padLeft(Value.percentHeight(.2f, lblLives));
            tblCenter.columnDefaults(1).width(lblLives.getPrefHeight() * 1.5f).right();
            tblCenter.add(imgMovesIcon).size(lblLives.getPrefHeight());
            tblCenter.add(lblMoves).left();
            tblCenter.row().padTop(Value.percentHeight(.2f, lblLives));
            tblCenter.add(imgLivesIcon).size(lblLives.getPrefHeight());
            tblCenter.add(lblLives).left();

            imgRound = new Image(skin, "gameScreenTopRound");
            imgRound.setScaling(Scaling.fit);

            GlyphLayout fontLayout = new GlyphLayout(skin.getFont("h4"), "000000/000000");

            tblTop = new Table(skin);
            tblTop.background("softGray");
            tblTop.pad(0);
            tblTop.columnDefaults(0).padLeft(Value.percentHeight(.5f, lblScore)).expandX().uniformX();
            tblTop.columnDefaults(1);
            tblTop.columnDefaults(2).padRight(Value.percentHeight(.5f, lblScore)).expandX().uniformX();
            tblTop.padTop(Value.percentHeight(.5f, lblScore));
            tblTop.row()
                    .padBottom(Value.percentHeight(.2f, lblScore));
            tblTop.add(tblTime)
                    .width(fontLayout.width);
            tblTop.add(tblCenter)
                    .size(lblLives.getPrefHeight() * 5);
            tblTop.add(tblScore)
                    .width(fontLayout.width);


            tblPowerUps.setTouchable(Touchable.enabled);
            tblPowerUps.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            /* Validate the tblTop in order to obtain the correct height for tblScore */
            tblTop.validate();
            root = new Table();
            root.setFillParent(true);
            root.top().add(tblTop).growX().height(Value.percentHeight(1.6f, tblScore)).padTop(Value.percentHeight(-.25f, tblScore));
        }

        public void setup() {
            lblScore.setText(0);
            lblTargetScore.setText(statsManager.getTargetScore());
            lblLives.setText(String.valueOf(statsManager.getLives()));
            tbPower1.setText(String.valueOf(statsManager.getSpecialBallCount()));
            lblMoves.setText(String.valueOf(statsManager.getMoves()));

            if (statsManager.isMovesEnabled() && statsManager.isLivesEnabled()) {
                tblTop.getCells().get(1).padTop(Value.percentHeight(-1.0f, lblLives));
                tblCenter.getCells().get(2).padBottom(lblLives.getPrefHeight() * .5f);
                tblCenter.getCells().get(3).padBottom(lblLives.getPrefHeight() * .5f);
            } else {
                tblTop.getCells().get(1).padTop(Value.percentHeight(-2.5f, lblLives));
                tblCenter.getCells().get(2).padBottom(-lblLives.getHeight() * .4f);
                tblCenter.getCells().get(3).padBottom(-lblLives.getHeight() * .4f);
            }

            imgMovesIcon.setVisible(statsManager.isMovesEnabled());
            lblMoves.setVisible(statsManager.isMovesEnabled());

            imgLivesIcon.setVisible(statsManager.isLivesEnabled());
            lblLives.setVisible(statsManager.isLivesEnabled());

            tblTime.setVisible(statsManager.isTimeEnabled());

            tblPowerUps.setVisible(statsManager.getSpecialBallCount() != 0);
            tbPower1.setDisabled(statsManager.getSpecialBallCount() == 0);

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
        Label resultTextLbl, lblScore;
        Container<Table> root;

        ResultUI() {
            Table main = new Table(skin);
            main.background("box_white_5");
            main.pad(40);
            root = new Container<>(main);
            root.setFillParent(true);

            resultTextLbl = new Label("null", skin, "h2");
            lblScore = new Label("null", skin, "h3");

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

            Label staticScore = new Label("Score:", skin, "h3");

            main.center();
            main.add(resultTextLbl).padBottom(40).row();
            main.add(staticScore).padBottom(10).row();
            main.add(lblScore).row();
            main.add(buttonGroup).padTop(100);

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

    public class GameScreenController {
        GameScreen gameScreen;

        private GameScreenController(GameScreen screen) {
            gameScreen = screen;
        }

        public void loadLevel(int lvl) {
            statsManager.setLevel(lvl);
            loadLevelMap("level" + lvl);
        }

        public GameScreenController loadLevelMap(String fileName) {
            ParsedLevel parsedLevel = LevelParser.loadFrom(fileName);

            LevelSettings levelSettings = Objects.requireNonNull(parsedLevel).getLevelSettings();
            statsManager.setGameMode(GameMode.CLASSIC);
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
                        .setMinMaxRotationSpeed(settings.getMinSpeed(), settings.getMaxSpeed(), settings.isRotateCCW())
                        .populateFrom(tileList)
                        .setChained(i == 0)
                        .build();
            }

            return this;
        }
    }
}