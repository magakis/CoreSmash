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
import com.breakthecore.NotificationType;
import com.breakthecore.Observer;
import com.breakthecore.RoundEndListener;
import com.breakthecore.TilemapManager;
import com.breakthecore.MovingTileManager;
import com.breakthecore.RenderManager;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenBase implements Observer {
    private BreakTheCoreGame m_game;
    private OrthographicCamera m_camera;

    private Tilemap m_tilemap;
    private TilemapManager m_tilemapManager;
    private RenderManager renderManager;

    private RoundEndListener m_roundEndListener;

    private InputProcessor m_classicGestureDetector, m_spinTheCoreGestureDetector, m_backButtonHandler;

    private Label m_timeLbl, m_scoreLbl, m_highscoreLbl, m_livesLbl;
    private boolean isGameActive;
    private boolean roundWon;
    private int m_score;
    private float m_time;
    private int m_lives;

    private GameMode m_gameMode;

    //===========
    private Skin m_skin;
    private Stage m_stage;
    private MovingTileManager m_movingTileManager;
    private Label dblb1, dblb2, dblb3, dblb4, dblb5;
    private Stack m_stack;
    private Table mainTable, debugTable, resultTable;
    //===========
    private int colorCount = 7;
    private int sideLength = 64;


    public GameScreen(BreakTheCoreGame game) {
        m_game = game;
        m_camera = (OrthographicCamera) m_game.getWorldViewport().getCamera();
        m_skin = m_game.getSkin();

        renderManager = new RenderManager(sideLength, colorCount);
        m_movingTileManager = new MovingTileManager(sideLength, colorCount);

        setupUI();

        m_tilemap = new Tilemap(new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4), 20, sideLength);
        m_tilemapManager = new TilemapManager(m_tilemap);

        m_classicGestureDetector = new CustomGestureDetector(new ClassicModeInputListener());
        m_spinTheCoreGestureDetector = new CustomGestureDetector(new SpinTheCoreModeInputListener(m_tilemap.getPositionInWorld()));

        isGameActive = true;
        m_tilemapManager.addObserver(this);
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
        m_score = 0;
        m_time = 0;
        m_lives = 3;
        m_roundEndListener = null;
        isGameActive = true;
        m_stage.clear();
        m_stage.addActor(m_stack);
        m_tilemapManager.initHexTilemap(m_tilemap, settings.initRadius);
        m_gameMode = settings.gameMode;
        m_movingTileManager.clear();

        m_livesLbl.setText(String.valueOf(m_lives));

        switch (m_gameMode) {
            case CLASSIC:
                m_tilemapManager.setAutoRotation(true);
                m_tilemapManager.setMinMaxRotationSpeed(settings.minRotationSpeed, settings.maxRotationSpeed);
                m_movingTileManager.setDefaultBallSpeed(15);
                m_movingTileManager.setAutoEject(false);
                m_highscoreLbl.setText(String.valueOf(Gdx.app.getPreferences("highscores").getInteger("classic_highscore", 0)));
                break;

            case SPIN_THE_CORE:
                m_tilemapManager.setAutoRotation(false);
                m_movingTileManager.setLaunchDelay(settings.launcherCooldown);
                m_movingTileManager.setDefaultBallSpeed(settings.movingTileSpeed);
                m_movingTileManager.setAutoEject(true);
                m_highscoreLbl.setText(String.valueOf(Gdx.app.getPreferences("highscores").getInteger("spinthecore_highscore", 0)));
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

            renderManager.renderCenterDot(m_camera.combined);
        }
        m_stage.draw();
    }

    private void update(float delta) {
        if (isGameActive) {
            m_time += delta;
            m_movingTileManager.update(delta);
            m_tilemapManager.update(delta);
            m_tilemapManager.checkForCollision(m_movingTileManager.getActiveList());
            updateStage();
        }
    }

    private void updateStage() {
        m_timeLbl.setText(String.format("%d:%02d", (int) m_time / 60, (int) m_time % 60));
        m_scoreLbl.setText(String.valueOf(m_score));

        dblb1.setText("ballCount: " + m_tilemap.getTileCount());
        dblb2.setText(String.format("rotSpeed: %.3f", m_tilemapManager.getRotationSpeed()));

    }

    public void handleGameEnd() {
        resultTable = createResultTable();
        m_stage.clear();
        m_stage.addActor(resultTable);

        // NOTE: It's questionable whether I want to store such scores because mainly of this campaign.
        if (roundWon) {
            Preferences prefs = Gdx.app.getPreferences("highscores");
            switch (m_gameMode) {
                case CLASSIC:
                    if (prefs.getInteger("classic_highscore", 0) < m_score) {
                        prefs.putInteger("classic_highscore", m_score);
                        prefs.flush();
                    }
                    break;
                case SPIN_THE_CORE:
                    if (prefs.getInteger("spinthecore_highscore", 0) < m_score) {
                        prefs.putInteger("spinthecore_highscore", m_score);
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

            case NOTIFICATION_TYPE_TILE_DESTROYED:
                if (isGameActive) {
                    m_score++;
                }
                break;
            case NOTIFICATION_TYPE_NO_COLOR_MATCH:
                if (m_lives == 1) {
                    roundWon = false;
                    isGameActive = false;
                    handleGameEnd();
                } else {
                    --m_lives;
                    m_livesLbl.setText(String.valueOf(m_lives));
                }
        }
    }

    public enum GameMode {
        CLASSIC,
        SPIN_THE_CORE,
        SHOOT_EM_UP;

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
                m_game.setMainMenuScreen();
                return false;
            }
            return false;
        }
    }

    private void setupUI() {
        Label staticTimeLbl, staticScoreLbl, staticLivesLbl;
        HorizontalGroup hgrp;

        m_stage = new Stage(m_game.getWorldViewport());
        m_stack = new Stack();
        m_stack.setFillParent(true);
        mainTable = new Table();

        m_timeLbl = new Label("0", m_skin, "comic_48");
        m_timeLbl.setAlignment(Align.center);

        m_scoreLbl = new Label("0", m_skin, "comic_48");
        m_scoreLbl.setAlignment(Align.center);

        m_livesLbl = new Label("null", m_skin, "comic_48");
        m_livesLbl.setAlignment(Align.center);

        m_highscoreLbl = new Label("", m_skin, "comic_32b");
        m_highscoreLbl.setAlignment(Align.center);

        staticTimeLbl = new Label("Time:", m_skin, "comic_48b");
        staticScoreLbl = new Label("Score:", m_skin, "comic_48b");
        staticLivesLbl = new Label("Lives: ", m_skin, "comic_48b");

        Stack grpTime = new Stack();
        Stack grpScore = new Stack();

        Image img = new Image(m_skin.getDrawable("box_white_5"));
        grpTime.addActor(img);
        grpTime.addActor(m_timeLbl);

        img = new Image(m_skin.getDrawable("box_white_5"));
        grpScore.addActor(img);
        grpScore.addActor(m_scoreLbl);

        hgrp = new HorizontalGroup();
        hgrp.addActor(staticLivesLbl);
        hgrp.addActor(m_livesLbl);

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
        mainTable.add(m_highscoreLbl);


        m_stack.add(mainTable);
        // WARNING: If you skip the creation of a debug table the game will crash
        // cause it tries to update labels that have not been initialized
        debugTable = createDebugTable();
//        m_stack.add(debugTable);
        m_stage.addActor(m_stack);
    }

    public Table createDebugTable() {
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

        return dbtb;
    }

    public Table createResultTable() {
        Table res = new Table();
        res.setFillParent(true);
        String resultText = roundWon ? "Congratulations!" : "You lost";

        Label m_resultLabel = new Label(resultText, m_skin, "comic_96b");
        Label staticTime = new Label("Time:", m_skin, "comic_48b");
        Label staticScore = new Label("Score:", m_skin, "comic_48b");
        Label time = new Label(m_timeLbl.getText(), m_skin, "comic_48");
        Label score = new Label(String.valueOf(m_score), m_skin, "comic_48");

        TextButton tb = new TextButton("Menu", m_skin);
        tb.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_game.setMainMenuScreen();
            }
        });

        tb.getLabelCell().width(200).height(150);

        HorizontalGroup hg = new HorizontalGroup();
        hg.align(Align.center);
        hg.addActor(tb);


        res.center();
        res.add(m_resultLabel).colspan(2).padBottom(50).row();
        res.add(staticTime);
        res.add(staticScore).row();
        res.add(time);
        res.add(score).row();
        res.add(hg).colspan(2).padTop(50);

        return res;
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

}



