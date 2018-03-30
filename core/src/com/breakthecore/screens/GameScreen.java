package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.NotificationType;
import com.breakthecore.Observer;
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
    private FitViewport m_fitViewport;
    private OrthographicCamera m_camera;
    private Tilemap m_tilemap;
    private TilemapManager m_tilemapManager;
    InputMultiplexer m_inputMultiplexer;
    private RenderManager renderManager;
    GestureDetector gd;
    Label m_timeLbl, m_scoreLbl;
    boolean isGameActive;
    boolean roundWon;
    //===========
    Skin m_skin;
    Label staticTimeLbl, staticScoreLbl;
    Stage stage;
    private MovingTileManager movingTileManager;
    Label dblb1, dblb2, dblb3, dblb4, dblb5;
    Stack m_stack;
    Table mainTable, debugTable, resultTable;
    private int m_score;

    private float m_time;
    //===========
    private int colorCount = 7;
    private int sideLength = 64;


    public GameScreen(BreakTheCoreGame game) {
        m_game = game;
        m_fitViewport = new FitViewport(WorldSettings.getWorldWidth(), WorldSettings.getWorldHeight());
        m_camera = (OrthographicCamera) m_fitViewport.getCamera();
        renderManager = new RenderManager(sideLength, colorCount);
        movingTileManager = new MovingTileManager(sideLength, colorCount);

        setupUI();

        m_tilemap = new Tilemap(new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4), 20, sideLength);
        m_tilemapManager = new TilemapManager(m_tilemap);

        gd = new GestureDetector(new GameInputListener());
        m_inputMultiplexer = new InputMultiplexer(stage, gd);
        isGameActive = true;

        m_tilemapManager.addObserver(this);
    }

    public void initializeGameScreen(GameSettings settings) {
        m_score = 0;
        m_time = 0;
        isGameActive = true;
        stage.clear();
        stage.addActor(m_stack);
        m_tilemapManager.initHexTilemap(m_tilemap, settings.initRadius);
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
        stage.draw();
    }

    private void update(float delta) {
        if (isGameActive) {
            m_time += delta;
            movingTileManager.update(delta);
            m_tilemapManager.update(delta);
            m_tilemapManager.checkForCollision(movingTileManager.getActiveList());
            updateStage();
        }
    }

    @Override
    public void resize(int width, int height) {
        m_fitViewport.update(width, height, true);
    }

    private void updateStage() {
        m_timeLbl.setText(String.format("%d:%02d", (int) m_time / 60, (int) m_time % 60));
        m_scoreLbl.setText(String.valueOf(m_score));

        dblb1.setText("ballCount: " + m_tilemap.getTileCount());
        dblb2.setText(String.format("rotSpeed: %.3f", m_tilemapManager.getRotationSpeed()));

        dblb3.setText("");
        dblb4.setText("");
        dblb5.setText("");
    }

    private void setupUI() {
        m_skin = m_game.getSkin();

        stage = new Stage(new FitViewport(WorldSettings.getWorldWidth(), WorldSettings.getWorldHeight()));
        m_stack = new Stack();
        m_stack.setFillParent(true);
        mainTable = new Table();

        m_timeLbl = new Label("null", m_skin, "comic1_48");
        m_timeLbl.setAlignment(Align.center);
        m_timeLbl.setWidth(mainTable.getWidth() / 2);

        m_scoreLbl = new Label("null", m_skin, "comic1_48");
        m_scoreLbl.setAlignment(Align.center);
        m_scoreLbl.setWidth(mainTable.getWidth() / 2);

        staticTimeLbl = new Label("Time:", m_skin, "comic1_48b");
        staticScoreLbl = new Label("Score:", m_skin, "comic1_48b");

        Stack grpTime = new Stack();
        Stack grpScore = new Stack();

        Image img = new Image(m_skin.getDrawable("box_white_5"));
        grpTime.addActor(img);
        grpTime.addActor(m_timeLbl);

        img = new Image(m_skin.getDrawable("box_white_5"));
        grpScore.addActor(img);
        grpScore.addActor(m_scoreLbl);

        mainTable.setFillParent(true);
        mainTable.top().left();
        mainTable.add(staticTimeLbl, null, staticScoreLbl);
        mainTable.row();
        mainTable.add(grpTime).width(200).height(100).padLeft(-10);
        mainTable.add().expandX();
        mainTable.add(grpScore).width(200).height(100).padRight(-10).row();

        debugTable = createDebugTable();

        m_stack.add(mainTable);
        m_stack.add(debugTable);
        stage.addActor(m_stack);
    }
    // fills entire tilemap with tiles

    public Table createDebugTable() {
        Table dbtb = new Table();
        dblb1 = new Label("db1:", m_skin, "comic1_24b");
        dblb1.setAlignment(Align.left);
        dblb2 = new Label("db1:", m_skin, "comic1_24b");
        dblb2.setAlignment(Align.left);
        dblb3 = new Label("db1:", m_skin, "comic1_24b");
        dblb3.setAlignment(Align.left);
        dblb4 = new Label("db1:", m_skin, "comic1_24b");
        dblb4.setAlignment(Align.left);
        dblb5 = new Label("db1:", m_skin, "comic1_24b");
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

        Label m_resultLabel = new Label(resultText, m_skin, "comic1_96b");
        Label staticTime = new Label("Time:", m_skin, "comic1_48b");
        Label staticScore = new Label("Score:", m_skin, "comic1_48b");
        Label time = new Label(m_timeLbl.getText(), m_skin, "comic1_48");
        Label score = new Label(m_scoreLbl.getText(), m_skin, "comic1_48");

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

    public void handleGameEnd() {
        resultTable = createResultTable();
        stage.clear();
        stage.addActor(resultTable);
//        SharedPrefernces
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

        }
    }

    @Override
    public InputProcessor getScreenInputProcessor() {
        return m_inputMultiplexer;
    }

    public static class GameSettings {
        int initRadius;
    }

    private class GameInputListener implements GestureDetector.GestureListener {
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
}



