package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.StreakUI;
import com.breakthecore.managers.CollisionManager;
import com.breakthecore.NotificationType;
import com.breakthecore.Observer;
import com.breakthecore.RoundEndListener;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.MovingTile;
import com.breakthecore.ui.UIBase;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenBase implements Observer {
    private OrthographicCamera m_camera;

    private Tilemap m_tilemap;
    private TilemapManager tilemapManager;
    private MovingTileManager movingTileManager;
    private RenderManager renderManager;
    private CollisionManager collisionManager;
    private StatsManager statsManager;
    private StreakUI m_streakUI;

    private RoundEndListener m_roundEndListener;

    private InputProcessor m_classicGestureDetector, m_spinTheCoreGestureDetector, m_debugGestureDetector;

    private boolean isGameActive;
    private boolean roundWon;

    private GameMode m_gameMode;

    //===========
    private GameUI gameUI;
    private ResultUI m_resultUI;
    private Skin m_skin;
    private Stage m_stage;
    private Stack m_root;
    //===========

    private int colorCount = 7;
    private int sideLength = WorldSettings.getTileSize();


    public GameScreen(BreakTheCoreGame game) {
        super(game);
        m_camera = (OrthographicCamera) m_game.getWorldViewport().getCamera();
        m_skin = m_game.getSkin();

        renderManager = m_game.getRenderManager();
        movingTileManager = new MovingTileManager(sideLength, colorCount);
        collisionManager = new CollisionManager();

        m_tilemap = new Tilemap(new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4), 31, sideLength);
        tilemapManager = new TilemapManager(m_tilemap);

        m_classicGestureDetector = new CustomGestureDetector(new ClassicModeInputListener());
        m_spinTheCoreGestureDetector = new CustomGestureDetector(new SpinTheCoreModeInputListener(m_tilemap.getPositionInWorld()));
        m_debugGestureDetector = new CustomGestureDetector(new DebugModeInputListener());


        isGameActive = true;

        statsManager = new StatsManager();

        m_streakUI = new StreakUI(m_skin);
        statsManager.addObserver(this);
        statsManager.addObserver(m_streakUI);

        tilemapManager.addObserver(this);
        tilemapManager.addObserver(statsManager);

        m_stage = new Stage(game.getWorldViewport());

        gameUI = new GameUI();
        m_resultUI = new ResultUI();

        m_root = new Stack(gameUI.getRoot(), m_streakUI.getRoot());
        m_root.setFillParent(true);
    }

    private void setInputHanlderFor(GameMode mode) {
        screenInputMultiplexer.clear();
        screenInputMultiplexer.addProcessor(m_stage);
        switch (mode) {
            case SHOOT_EM_UP:
                screenInputMultiplexer.addProcessor(m_debugGestureDetector);
                break;

            case CLASSIC:
                screenInputMultiplexer.addProcessor(m_classicGestureDetector);
                break;

            case SPIN_THE_CORE:
                screenInputMultiplexer.addProcessor(m_spinTheCoreGestureDetector);
                break;
        }
    }

    public void initialize(GameSettings settings) {
        m_roundEndListener = null;
        isGameActive = true;
        tilemapManager.initTilemapCircle(m_tilemap, settings.initRadius);
        m_gameMode = settings.gameMode;
        movingTileManager.reset();
        statsManager.reset();

        m_stage.clear();
        m_stage.addActor(m_root);

        switch (m_gameMode) {
            case SHOOT_EM_UP:
                tilemapManager.setAutoRotation(false);
                tilemapManager.setMinMaxRotationSpeed(settings.minRotationSpeed, settings.maxRotationSpeed);
                movingTileManager.setDefaultBallSpeed(settings.movingTileSpeed);
                movingTileManager.setAutoEject(false);
                movingTileManager.setLaunchDelay(0);
                movingTileManager.setActiveState(false);
                gameUI.highscoreLbl.setText(String.valueOf(Gdx.app.getPreferences("highscores").getInteger("classic_highscore", 0)));
                break;
            case CLASSIC:
                tilemapManager.setAutoRotation(true);
                tilemapManager.setMinMaxRotationSpeed(settings.minRotationSpeed, settings.maxRotationSpeed);
                movingTileManager.setDefaultBallSpeed(settings.movingTileSpeed);
                movingTileManager.setAutoEject(false);
                gameUI.highscoreLbl.setText(String.valueOf(Gdx.app.getPreferences("highscores").getInteger("classic_highscore", 0)));
                break;

            case SPIN_THE_CORE:
                tilemapManager.setAutoRotation(false);
                movingTileManager.setLaunchDelay(settings.launcherCooldown);
                movingTileManager.setDefaultBallSpeed(settings.movingTileSpeed);
                movingTileManager.setAutoEject(true);
                gameUI.highscoreLbl.setText(String.valueOf(Gdx.app.getPreferences("highscores").getInteger("spinthecore_highscore", 0)));
                break;
        }

        gameUI.livesLbl.setText(String.valueOf(statsManager.getLives()));
        gameUI.tbPower1.setText(String.valueOf(statsManager.getSpecialBallCount()));

        setInputHanlderFor(settings.gameMode);
    }

    public void startRound(GameSettings settings, RoundEndListener listener) {
        initialize(settings);
        m_roundEndListener = listener;
        m_game.setScreen(this);
    }

    @Override
    public void render(float delta) {
        update(delta);

        if (isGameActive) {
            renderManager.start(m_camera.combined);
            renderManager.draw(m_tilemap);
            renderManager.drawLauncher(movingTileManager.getLauncherQueue(), movingTileManager.getLauncherPos());
            renderManager.draw(movingTileManager.getActiveList());
            renderManager.end();

            renderManager.renderCenterDot(m_camera.combined);
        }
        m_stage.draw();
    }

    private void update(float delta) {
        if (isGameActive) {
            movingTileManager.update(delta);
            tilemapManager.update(delta);
            collisionManager.checkForCollision(movingTileManager, tilemapManager);
            tilemapManager.cleanTilemap();
            statsManager.update(delta);
            updateStage();
        }
    }

    private void updateStage() {
        m_stage.act();
        float time = statsManager.getTime();
        gameUI.timeLbl.setText(String.format("%d:%02d", (int) time / 60, (int) time % 60));
        gameUI.scoreLbl.setText(String.valueOf(statsManager.getScore()));
    }

    public void handleGameEnd() {
        m_resultUI.update();
        m_stage.clear();
        m_stage.addActor(m_resultUI.getRoot());
        int score = statsManager.getScore();

        // NOTE: It's questionable whether I want to store such scores because mainly of this campaign.
        if (roundWon) {
            Preferences prefs = Gdx.app.getPreferences("highscores");
            switch (m_gameMode) {
                case CLASSIC:
                    if (prefs.getInteger("classic_highscore", 0) < score) {
                        prefs.putInteger("classic_highscore", score);
                        prefs.flush();
                    }
                    break;
                case SPIN_THE_CORE:
                    if (prefs.getInteger("spinthecore_highscore", 0) < score) {
                        prefs.putInteger("spinthecore_highscore", score);
                        prefs.flush();
                    }
                    break;
            }
        }

        if (m_roundEndListener != null) {
            m_roundEndListener.onRoundEnded(roundWon);
        }
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED:
                isGameActive = false;
                roundWon = true;
                handleGameEnd();
                break;

            case NOTIFICATION_TYPE_LIVES_CHANGED:
                int lives = statsManager.getLives();
                if (lives == 0) {
                    isGameActive = false;
                    roundWon = false;
                    handleGameEnd();
                } else {
                    gameUI.livesLbl.setText(String.valueOf(lives));
                }
                break;
        }
    }

    private class ClassicModeInputListener implements GestureDetector.GestureListener {

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            movingTileManager.eject();
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
//        MovingTile mt = movingTileManager.getFirstActiveTile();
//        if (mt == null) {
//            movingTileManager.eject();
//            mt = movingTileManager.getFirstActiveTile();
//        }
//        mt.moveBy(deltaX, -deltaY);
            return true;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
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

    private class DebugModeInputListener implements GestureDetector.GestureListener {

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
//            movingTileManager.eject();
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
            MovingTile mt = movingTileManager.getFirstActiveTile();
            if (mt == null) {
                movingTileManager.eject();
                mt = movingTileManager.getFirstActiveTile();
            }
            mt.moveBy(deltaX, -deltaY);
            return true;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
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

    private class SpinTheCoreModeInputListener implements GestureDetector.GestureListener {

        private boolean isPanning;
        private Vector2 tmPos;
        private Vector3 scrPos;
        private float initAngle;
        private Vector2 currPoint;

        public SpinTheCoreModeInputListener(Vector2 tilemapPos) {
            tmPos = tilemapPos;
            scrPos = new Vector3();
            currPoint = new Vector2();
        }

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            return false;
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
            if (isPanning) {
                float currAngle;
                scrPos.set(x, y, 0);
                scrPos = m_camera.unproject(scrPos);
                currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                currAngle = currPoint.angle();
                m_tilemap.rotate((initAngle - currAngle) * 2.5f);
                initAngle = currAngle;
            } else {
                isPanning = true;
                scrPos.set(x, y, 0);
                scrPos = m_camera.unproject(scrPos);
                currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                initAngle = currPoint.angle();
            }
            return true;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            isPanning = false;
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
            if (keycode == Input.Keys.BACK) {
                m_game.setPrevScreen();
                return false;
            }
            return false;
        }

    }

    public enum GameMode {
        CLASSIC,
        SPIN_THE_CORE,
        SHOOT_EM_UP;

    }

    public static class GameSettings {

        public GameMode gameMode;
        public int initRadius;
        public float minRotationSpeed;
        public float maxRotationSpeed;
        public int movingTileSpeed;
        public float launcherCooldown;

        public void reset() {
            gameMode = null;
            initRadius = 0;
            minRotationSpeed = 0;
            maxRotationSpeed = 0;
            movingTileSpeed = 0;
            launcherCooldown = 0;
        }

    }

    private class GameUI extends UIBase {
        Label timeLbl, scoreLbl, livesLbl, highscoreLbl;
        TextButton tbPower1;

        public GameUI() {
            final Label staticTimeLbl, staticScoreLbl, staticLivesLbl;
            HorizontalGroup hgrp;

            Table mainTable = new Table();

            timeLbl = new Label("0", m_skin, "comic_48");
            timeLbl.setAlignment(Align.center);

            scoreLbl = new Label("0", m_skin, "comic_48");
            scoreLbl.setAlignment(Align.center);

            livesLbl = new Label("null", m_skin, "comic_48");
            livesLbl.setAlignment(Align.center);

            highscoreLbl = new Label("", m_skin, "comic_32b");
            highscoreLbl.setAlignment(Align.center);

            staticTimeLbl = new Label("Time:", m_skin, "comic_48b");
            staticScoreLbl = new Label("Score:", m_skin, "comic_48b");
            staticLivesLbl = new Label("Lives: ", m_skin, "comic_48b");

            Stack grpTime = new Stack();
            Stack grpScore = new Stack();

            Image img = new Image(m_skin.getDrawable("box_white_5"));
            grpTime.addActor(img);
            grpTime.addActor(timeLbl);

            img = new Image(m_skin.getDrawable("box_white_5"));
            grpScore.addActor(img);
            grpScore.addActor(scoreLbl);

            hgrp = new HorizontalGroup();
            hgrp.addActor(staticLivesLbl);
            hgrp.addActor(livesLbl);

            tbPower1 = new TextButton("null", m_skin, "tmpPowerup");
            tbPower1.getLabel().setAlignment(Align.bottomLeft);
            tbPower1.getLabelCell().padBottom(5).padLeft(10);
            tbPower1.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    statsManager.consumeSpecialBall(movingTileManager);
                    if (statsManager.getSpecialBallCount() == 0) {
                        tbPower1.setDisabled(true);
                        tbPower1.setText(String.valueOf(statsManager.getSpecialBallCount()));
                    }
                }
            });


            Drawable tmp = m_skin.newDrawable("box_white_5");
            Image powerupsBackround = new Image(tmp);

            Table powerUpTable = new Table();
            powerUpTable.center();
            powerUpTable.add(tbPower1).size(100,100);

            Stack powerUpStack = new Stack();
            powerUpStack.add(powerupsBackround);
            powerUpStack.add(powerUpTable);

            powerupsBackround.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            mainTable.setFillParent(true);
            mainTable.top().left();
            mainTable.add(staticTimeLbl, hgrp, staticScoreLbl);
            mainTable.row();
            mainTable.add(grpTime).width(200).height(100).padLeft(-10);
            mainTable.add().expandX();
            mainTable.add(grpScore).width(200).height(100).padRight(-10).row();
            mainTable.add().colspan(2);
            mainTable.add(new Label("Highscore:", m_skin, "comic_32b")).row();
            mainTable.add().colspan(2);
            mainTable.add(highscoreLbl).row();
            mainTable.add().colspan(2);
            mainTable.add(powerUpStack).height(300).width(140).expandY().fill().bottom().padBottom(150);


            root = mainTable;
        }
    }

    private class ResultUI extends UIBase {
        Label resultTextLbl, timeLbl, scoreLbl;

        public ResultUI() {
            Table tbl = new Table();
            tbl.setFillParent(true);

            Label staticTime = new Label("Time:", m_skin, "comic_48b");
            Label staticScore = new Label("Score:", m_skin, "comic_48b");
            resultTextLbl = new Label("null", m_skin, "comic_96b");
            timeLbl = new Label("null", m_skin, "comic_48");
            scoreLbl = new Label("null", m_skin, "comic_48");

            TextButton tb = new TextButton("Menu", m_skin);
            tb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    m_game.setPrevScreen();
                }
            });

            tb.getLabelCell().width(200).height(150);

            HorizontalGroup hg = new HorizontalGroup();
            hg.align(Align.center);
            hg.addActor(tb);


            tbl.center();
            tbl.add(resultTextLbl).colspan(2).padBottom(50).row();
            tbl.add(staticTime);
            tbl.add(staticScore).row();
            tbl.add(timeLbl);
            tbl.add(scoreLbl).row();
            tbl.add(hg).colspan(2).padTop(100);

            root = tbl;
        }

        public void update() {
            String resultText = roundWon ? "Congratulations!" : "You Failed!";
            resultTextLbl.setText(resultText);
            timeLbl.setText(gameUI.timeLbl.getText());
            scoreLbl.setText(String.valueOf(statsManager.getScore()));
        }
    }

    private class DebugUI extends UIBase {
        private Label dblb1, dblb2, dblb3, dblb4, dblb5;

        public DebugUI() {
            Table dbtb = new Table();
            dblb1 = new Label("db1:", m_skin, "comic_24b");
            dblb1.setAlignment(Align.left);
            dblb2 = new Label("db1:", m_skin, "comic_24b");
            dblb2.setAlignment(Align.left);
            dblb3 = new Label("db1:", m_skin, "comic_24b");
            dblb3.setAlignment(Align.left);
            dblb4 = new Label("db1:", m_skin, "comic_24b");
            dblb4.setAlignment(Align.left);
            dblb5 = new Label("db1:", m_skin, "comic_24b");
            dblb5.setAlignment(Align.left);

            dbtb.bottom().left();
            dbtb.add(dblb1).fillX().row();
            dbtb.add(dblb2).fillX().row();
            dbtb.add(dblb3).fillX().row();
            dbtb.add(dblb4).fillX().row();
            dbtb.add(dblb5).fillX();

            root = dbtb;
        }
    }
}











