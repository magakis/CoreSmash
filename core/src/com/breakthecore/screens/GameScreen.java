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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.breakthecore.CoreSmash;
import com.breakthecore.Coords2D;
import com.breakthecore.levels.Level;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.StreakUI;
import com.breakthecore.managers.CollisionManager;
import com.breakthecore.NotificationType;
import com.breakthecore.Observer;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.MovingTile;
import com.breakthecore.ui.UIComponent;

import java.util.Locale;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenBase implements Observer {
    private OrthographicCamera m_camera;

    private TilemapManager tilemapManager;
    private MovingTileManager movingTileManager;
    private RenderManager renderManager;
    private CollisionManager collisionManager;
    private StatsManager statsManager;
    private StreakUI streakUI;

    private Level activeLevel;

    private boolean isGameActive;
    private boolean roundWon;

    //===========
    private DebugUI debugUI;
    private GameUI gameUI;
    private ResultUI m_resultUI;
    private Skin skin;
    private Stage stage;
    private Stack rootUIStack;
    //===========

    private int sideLength = WorldSettings.getTileSize();

    public GameScreen(CoreSmash game) {
        super(game);
        m_camera = (OrthographicCamera) gameInstance.getWorldViewport().getCamera();
        skin = gameInstance.getSkin();

        renderManager = gameInstance.getRenderManager();
        movingTileManager = new MovingTileManager(sideLength);
        collisionManager = new CollisionManager();

        tilemapManager = new TilemapManager();

        InputProcessor gameGestureDetector = new CustomGestureDetector(new GameInputListener());

        isGameActive = true;

        statsManager = new StatsManager();

        streakUI = new StreakUI(skin);
        statsManager.addObserver(this);
        statsManager.addObserver(streakUI);

        tilemapManager.addObserver(this);
        tilemapManager.addObserver(statsManager);

        movingTileManager.addObserver(statsManager);

        stage = new Stage(game.getWorldViewport());

        gameUI = new GameUI();
        m_resultUI = new ResultUI();
        debugUI = new DebugUI();

        rootUIStack = new Stack();
        rootUIStack.setFillParent(true);
        stage.addActor(rootUIStack);

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(gameGestureDetector);
    }

    private void checkEndingConditions() {
        if (roundWon) {
            endGame();
        }
        if (statsManager.isTimeEnabled() && statsManager.getTime() < 0) {
            endGame();
        }
        if (statsManager.isLivesEnabled() && statsManager.getLives() == 0) {
            endGame();
        }
        if (statsManager.isMovesEnabled() && statsManager.getMoves() == 0 && movingTileManager.getActiveList().size() == 0) {
            endGame();
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

//        if (isGameActive) {
        renderManager.start(m_camera.combined);
        for (int i = 0; i < tilemapManager.getTilemapCount(); ++i) {
            renderManager.draw(tilemapManager.getTilemap(i));
        }
        renderManager.drawLauncher(movingTileManager.getLauncherQueue(), movingTileManager.getLauncherPos());
        renderManager.draw(movingTileManager.getActiveList());
        renderManager.end();

        renderManager.renderCenterDot(tilemapManager.getTilemapPosition(), m_camera.combined);
//        }
        stage.draw();
    }

    private void update(float delta) {
        if (isGameActive) {
            if (activeLevel != null) {
                activeLevel.update(delta, tilemapManager);
            }
            movingTileManager.update(delta);
            tilemapManager.update(delta);
            collisionManager.checkForCollision(movingTileManager, tilemapManager);

            statsManager.update(delta);
            updateStage();
            checkEndingConditions();
        }
    }

    private void updateStage() {
        stage.act();
        if (statsManager.isTimeEnabled()) {
            float time = statsManager.getTime();
            gameUI.lblTime.setText(String.format("%d:%02d", (int) time / 60, (int) time % 60));
        }
        gameUI.lblScore.setText(String.valueOf(statsManager.getScore()));

        debugUI.dblb2.setText("FPS: "+ Gdx.graphics.getFramesPerSecond());
    }

    private void endGame() {
        isGameActive = false;
        m_resultUI.update();
        stage.clear();
        stage.addActor(m_resultUI.getRoot());
        int score = statsManager.getScore();

        if (activeLevel != null) {
            if (roundWon) {
                Preferences prefs = Gdx.app.getPreferences("account");
                if (score > prefs.getInteger("level" + activeLevel.getLevelNumber(), 0)) {
                    prefs.putInteger("level" + activeLevel.getLevelNumber(), score);
                    prefs.flush();
                }
                gameInstance.getUserAccount().addScore(score, statsManager.getDifficultyMultiplier());
            }
            activeLevel.end(roundWon, statsManager);
        }

    }

    public void deployLevel(Level level) {
        reset();
        activeLevel = level;
        level.initialize(statsManager, tilemapManager, movingTileManager);

        gameUI.setup();
        if (activeLevel != null) {
            gameUI.lblHighscore.setText(String.valueOf(Gdx.app.getPreferences("account").getInteger("level" + activeLevel.getLevelNumber(), 0)));
        }
        gameUI.lblLives.setText(String.valueOf(statsManager.getLives()));
        gameUI.tbPower1.setText(String.valueOf(statsManager.getSpecialBallCount()));
        gameUI.lblMoves.setText(String.valueOf(statsManager.getMoves()));

        rootUIStack.add(gameUI.getRoot());
        rootUIStack.add(streakUI.getRoot());
        rootUIStack.add(debugUI.getRoot());
        stage.addActor(rootUIStack);

        debugUI.dblb3.setText(String.format(Locale.ENGLISH,"Diff: %.2f",statsManager.getDifficultyMultiplier()));
        isGameActive = true;
        gameInstance.setScreen(this);
    }

    private void reset() {
        tilemapManager.reset();
        movingTileManager.reset();
        statsManager.reset();
        streakUI.reset();

        isGameActive = false;
        roundWon = false;
        activeLevel = null;
        stage.clear();
        rootUIStack.clear();
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED:
                roundWon = true;
                break;

            case NOTIFICATION_TYPE_LIVES_CHANGED:
                int lives = statsManager.getLives();
                if (lives == 0) {
                    isGameActive = false;
                    roundWon = false;
                    endGame();
                } else {
                    gameUI.lblLives.setText(String.valueOf(lives));
                }
                break;

            case MOVES_AMOUNT_CHANGED:
                int moves = statsManager.getMoves();
                gameUI.lblMoves.setText(String.valueOf(moves));
                if (statsManager.isMovesEnabled() && moves == movingTileManager.getLauncherSize()) {
                    movingTileManager.setLastTileColor(tilemapManager.getTilemap(0).getRelativeTile(0, 0).getColor());
                    movingTileManager.setAutoReloadEnabled(false);
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
                    movingTileManager.eject();
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
                    if (isGameActive) {
                        if (isPanning) {
                            float currAngle;
                            scrPos.set(x, y, 0);
                            scrPos = m_camera.unproject(scrPos);
                            currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                            currAngle = currPoint.angle();
                            tilemapManager.getTilemap(0).rotate((initAngle - currAngle) * 2.5f);
                            initAngle = currAngle;
                        } else {
                            isPanning = true;
                            scrPos.set(x, y, 0);
                            scrPos = m_camera.unproject(scrPos);
                            currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                            initAngle = currPoint.angle();
                        }
                    }
                    break;

                case SHOOT_EM_UP:
                    MovingTile mt = movingTileManager.getFirstActiveTile();
                    if (mt == null) {
                        movingTileManager.eject();
                        mt = movingTileManager.getFirstActiveTile();
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
            if (keycode == Input.Keys.BACK  || keycode == Input.Keys.ESCAPE) {
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

    private class GameUI extends UIComponent {
        Table tblMain, tblPowerUps;
        Table tblTime, tblScore;
        HorizontalGroup grpTop, grpLives, grpMoves;
        Label lblTime, lblScore, lblLives, lblMoves, lblHighscore;
        TextButton tbPower1;
        final Label lblStaticTime, lblStaticScore, lblStaticLives, lblStaticMoves;

        public GameUI() {
            tblMain = new Table();

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
                    statsManager.consumeSpecialBall(movingTileManager);
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

            tblMain.setFillParent(true);

            setRoot(tblMain);
        }

        public void setup() {
            tblMain.clear();
            grpTop.clear();

            lblStaticTime.setVisible(statsManager.isTimeEnabled());
            tblTime.setVisible(statsManager.isTimeEnabled());

            tbPower1.setDisabled(false);
            tblPowerUps.setVisible(statsManager.getSpecialBallCount() != 0);

            if (statsManager.isLivesEnabled()) {
                grpTop.addActor(grpLives);
            }
            if (statsManager.isMovesEnabled()) {
                grpTop.addActor(grpMoves);
            }

            tblMain.top().left();
            tblMain.add(lblStaticTime, grpTop, lblStaticScore);
            tblMain.row();
            tblMain.add(tblTime).width(200).height(100).padLeft(-10);
            tblMain.add().expandX();
            tblMain.add(tblScore).width(200).height(100).padRight(-10).row();
            tblMain.add().colspan(2);
            tblMain.add(new Label("Highscore:", skin, "comic_32b")).row();
            tblMain.add().colspan(2);
            tblMain.add(lblHighscore).row();
            tblMain.add().colspan(2);
            tblMain.add(tblPowerUps).height(300).width(140).expandY().fill().bottom().padBottom(150);
        }
    }

    private class ResultUI extends UIComponent {
        Label resultTextLbl, timeLbl, scoreLbl;

        public ResultUI() {
            Table tbl = new Table();
            tbl.setFillParent(true);

            Label staticTime = new Label("Time:", skin, "comic_48b");
            Label staticScore = new Label("Score:", skin, "comic_48b");
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

            HorizontalGroup hg = new HorizontalGroup();
            hg.align(Align.center);
            hg.addActor(tbMenu);


            tbl.center();
            tbl.add(resultTextLbl).colspan(2).padBottom(50).row();
            tbl.add(staticTime);
            tbl.add(staticScore).row();
            tbl.add(timeLbl);
            tbl.add(scoreLbl).row();
            tbl.add(hg).colspan(2).padTop(100);

            setRoot(tbl);
        }

        public void update() {
            String resultText = roundWon ? "Congratulations!" : "You Failed!";
            resultTextLbl.setText(resultText);
            timeLbl.setText(gameUI.lblTime.getText());
            scoreLbl.setText(String.valueOf(statsManager.getScore()));
        }
    }

    private class DebugUI extends UIComponent {
        private Label dblb1, dblb2, dblb3, dblb4, dblb5;

        public DebugUI() {
            Table dbtb = new Table();
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

            dbtb.bottom().left();
            dbtb.add(dblb1).fillX().row();
            dbtb.add(dblb2).fillX().row();
            dbtb.add(dblb3).fillX().row();
            dbtb.add(dblb4).fillX().row();
            dbtb.add(dblb5).fillX();

            setRoot(dbtb);
        }
    }
}