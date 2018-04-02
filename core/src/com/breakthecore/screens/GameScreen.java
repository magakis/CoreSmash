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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.ScoreManager;
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
import com.breakthecore.ui.UIBase;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenBase implements Observer {
    private OrthographicCamera m_camera;

    private Tilemap m_tilemap;
    private TilemapManager m_tilemapManager;
    private MovingTileManager m_movingTileManager;
    private RenderManager renderManager;
    private CollisionManager m_collisionManager;
    private ScoreManager m_scoreManager;
    private StreakUI m_streakUI;

    private RoundEndListener m_roundEndListener;

    private InputProcessor m_classicGestureDetector, m_spinTheCoreGestureDetector;

    private boolean isGameActive;
    private boolean roundWon;
    private float m_time;
    private int m_lives;

    private GameMode m_gameMode;

    //===========
    private GameUI m_gameUI;
    private ResultUI m_resultUI;
    private Skin m_skin;
    private Stage m_stage;
    private Stack m_root;
    //===========

    private int colorCount = 7;
    private int sideLength = 64;


    public GameScreen(BreakTheCoreGame game) {
        super(game);
        m_camera = (OrthographicCamera) m_game.getWorldViewport().getCamera();
        m_skin = m_game.getSkin();

        renderManager = m_game.getRenderManager();
        m_movingTileManager = new MovingTileManager(sideLength, colorCount);
        m_collisionManager = new CollisionManager();

        m_tilemap = new Tilemap(new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4), 20, sideLength);
        m_tilemapManager = new TilemapManager(m_tilemap);

        m_classicGestureDetector = new CustomGestureDetector(new ClassicModeInputListener());
        m_spinTheCoreGestureDetector = new CustomGestureDetector(new SpinTheCoreModeInputListener(m_tilemap.getPositionInWorld()));

        isGameActive = true;
        m_tilemapManager.addObserver(this);

        m_gameUI = new GameUI();
        m_resultUI = new ResultUI();
        m_streakUI = new StreakUI(m_skin);

        m_scoreManager = new ScoreManager();
        m_scoreManager.addObserver(m_streakUI);
        m_tilemapManager.addObserver(m_scoreManager);

        m_stage = new Stage(game.getWorldViewport());

        m_root = new Stack(m_gameUI.getRoot(), m_streakUI.getRoot());
        m_root.setFillParent(true);
    }

    private void setInputHanlderFor(GameMode mode) {
        screenInputMultiplexer.clear();
        screenInputMultiplexer.addProcessor(m_stage);
        switch (mode) {
            case CLASSIC:
                screenInputMultiplexer.addProcessor(m_classicGestureDetector);
                break;

            case SPIN_THE_CORE:
                screenInputMultiplexer.addProcessor(m_spinTheCoreGestureDetector);
                break;
        }
    }

    public void initialize(GameSettings settings) {
        m_time = 0;
        m_lives = 3;
        m_roundEndListener = null;
        isGameActive = true;
        m_tilemapManager.initHexTilemap(m_tilemap, settings.initRadius);
        m_gameMode = settings.gameMode;
        m_movingTileManager.reset();
        m_scoreManager.reset();

        m_stage.clear();
        m_stage.addActor(m_root);
        m_gameUI.livesLbl.setText(String.valueOf(m_lives));

        switch (m_gameMode) {
            case CLASSIC:
                m_tilemapManager.setAutoRotation(true);
                m_tilemapManager.setMinMaxRotationSpeed(settings.minRotationSpeed, settings.maxRotationSpeed);
                m_movingTileManager.setDefaultBallSpeed(15);
                m_movingTileManager.setAutoEject(false);
                m_gameUI.highscoreLbl.setText(String.valueOf(Gdx.app.getPreferences("highscores").getInteger("classic_highscore", 0)));
                break;

            case SPIN_THE_CORE:
                m_tilemapManager.setAutoRotation(false);
                m_movingTileManager.setLaunchDelay(settings.launcherCooldown);
                m_movingTileManager.setDefaultBallSpeed(settings.movingTileSpeed);
                m_movingTileManager.setAutoEject(true);
                m_gameUI.highscoreLbl.setText(String.valueOf(Gdx.app.getPreferences("highscores").getInteger("spinthecore_highscore", 0)));
                break;
        }

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
            renderManager.drawLauncher(m_movingTileManager.getLauncherQueue(), m_movingTileManager.getLauncherPos());
//            renderManager.drawOnLeftSide(m_movingTileManager.getLauncherQueue());
            renderManager.draw(m_movingTileManager.getActiveList());
            renderManager.end();

//            renderManager.DBdraw(m_tilemapManager,m_tilemap);
            renderManager.renderCenterDot(m_camera.combined);
        }
        m_stage.draw();
    }

    private void update(float delta) {
        if (isGameActive) {
            m_time += delta;
            m_movingTileManager.update(delta);
            m_tilemapManager.update(delta);
            m_collisionManager.checkForCollision(m_movingTileManager, m_tilemapManager);
            m_scoreManager.update();
            updateStage();
        }
    }

    private void updateStage() {
        m_stage.act();
        m_gameUI.timeLbl.setText(String.format("%d:%02d", (int) m_time / 60, (int) m_time % 60));
        m_gameUI.scoreLbl.setText(String.valueOf(m_scoreManager.getScore()));
    }

    public void handleGameEnd() {
        m_resultUI.update();
        m_stage.clear();
        m_stage.addActor(m_resultUI.getRoot());
        int score = m_scoreManager.getScore();

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

            case NOTIFICATION_TYPE_NO_COLOR_MATCH:
                if (m_lives == 1) {
                    roundWon = false;
                    isGameActive = false;
                    handleGameEnd();
                } else {
                    --m_lives;
                    m_gameUI.livesLbl.setText(String.valueOf(m_lives));
                }
        }
    }

    private class ClassicModeInputListener implements GestureDetector.GestureListener {

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            m_movingTileManager.eject();
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

        public GameUI() {
            Label staticTimeLbl, staticScoreLbl, staticLivesLbl;
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
            mainTable.add(highscoreLbl);

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
            timeLbl.setText(m_gameUI.timeLbl.getText());
            scoreLbl.setText(String.valueOf(m_scoreManager.getScore()));
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











