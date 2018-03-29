package com.breakthecore.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.NotificationType;
import com.breakthecore.Observer;
import com.breakthecore.TilemapManager;
import com.breakthecore.MovingTileManager;
import com.breakthecore.RenderManager;
import com.breakthecore.Tile;
import com.breakthecore.Tilemap;
import com.breakthecore.TilemapTile;
import com.breakthecore.WorldSettings;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenAdapter implements GestureDetector.GestureListener, Observer {
    private BreakTheCoreGame m_game;
    private FitViewport m_fitViewport;
    private OrthographicCamera m_camera;
    private Tilemap m_tilemap;
    private TilemapManager m_tilemapManager;
    private RenderManager renderManager;
    GestureDetector gd;
    boolean isGameActive;
    boolean roundWon;
    //===========
    Skin m_skin;
    Label staticTimeLbl, staticScoreLbl;
    Stage stage;
    Label m_timeLbl, m_scoreLbl, m_resultLabel;
    Label dblb1, dblb2, dblb3, dblb4, dblb5;
    Stack m_stack;
    Table mainTable, debugTable, resultTable;
    private int m_score;

    private float m_time;
    //===========
    private final int colorCount = 7;
    private final int sideLength = 64;

    private MovingTileManager movingTileManager;

    public GameScreen(BreakTheCoreGame game) {
        m_game = game;
        m_fitViewport = new FitViewport(WorldSettings.getWorldWidth(), WorldSettings.getWorldHeight());
        m_camera = (OrthographicCamera) m_fitViewport.getCamera();
        renderManager = new RenderManager(sideLength, colorCount);
        movingTileManager = new MovingTileManager(sideLength, colorCount);

        setupUI();

        m_tilemap = new Tilemap(new Vector2(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4), 20, sideLength);
        m_tilemapManager = new TilemapManager(m_tilemap);
        m_tilemapManager.initHexTilemap(m_tilemap, 5);

        // XXX(3/28/2018): I need a standalone way to handle input here...
        gd = new GestureDetector(this);
        game.addInputHandler(gd);

        isGameActive = true;

        m_tilemapManager.addObserver(this);
    }

    @Override
    public void render(float delta) {
        update(delta);

        if (isGameActive) {
            renderManager.start(m_camera.combined);
            renderManager.draw(m_tilemap);
            renderManager.drawLauncher(movingTileManager.getLauncherQueue(), movingTileManager.getLauncherPos());
            renderManager.draw(movingTileManager.getActiveList());
//        renderManager.DBTileDistances(m_tilemap.getTilemapTiles());
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
        m_skin = createSkin();


        stage = new Stage(new FitViewport(WorldSettings.getWorldWidth(), WorldSettings.getWorldHeight()));
        m_stack = new Stack();
        m_stack.setFillParent(true);
        mainTable = new Table();

        m_timeLbl = new Label("null", m_skin, "ddd");
        m_timeLbl.setAlignment(Align.center);
        m_timeLbl.setWidth(mainTable.getWidth() / 2);

        m_scoreLbl = new Label("null", m_skin, "ddd");
        m_scoreLbl.setAlignment(Align.center);
        m_scoreLbl.setWidth(mainTable.getWidth() / 2);

        staticTimeLbl = new Label("Time:", m_skin, "small");
        staticScoreLbl = new Label("Score:", m_skin, "small");

        mainTable.setFillParent(true);
        mainTable.top().left();
        mainTable.add(staticTimeLbl, null, staticScoreLbl);
        mainTable.row();
        mainTable.add(m_timeLbl).width(200).height(100).padLeft(-10);
        mainTable.add().expandX();
        mainTable.add(m_scoreLbl).width(200).height(100).padRight(-10).row();

        debugTable = createDebugTable();
        m_stack.add(mainTable);
        m_stack.add(debugTable);
        stage.addActor(m_stack);
    }
    // fills entire tilemap with tiles

    private Skin createSkin() {
        Skin skin = new Skin();
        Label.LabelStyle ls;
        Pixmap pix;
        Texture tex;
        BitmapFont bf;

        int pixHeight = 100;
        pix = new Pixmap(WorldSettings.getWorldWidth() / 5, pixHeight, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        pix.setColor(Color.BLACK);
        pix.fillRectangle(5, 5, pix.getWidth() - 10, pixHeight - 10);
        tex = new Texture(pix);
        skin.add("topBox", tex);

        ls = new Label.LabelStyle();
        bf = new BitmapFont();
        bf.getData().setScale(4);
        bf.setColor(Color.WHITE);
        ls.font = bf;
        ls.background = skin.getDrawable("topBox");
        skin.add("ddd", ls);

        bf = new BitmapFont();
        bf.getData().setScale(2);
        ls = new Label.LabelStyle(bf, Color.WHITE);
        skin.add("small", ls);

        SplitPane.SplitPaneStyle sps = new SplitPane.SplitPaneStyle();
        sps.handle = skin.getDrawable("topBox");
        skin.add("default-horizontal", sps);

        return skin;
    }

    public Table createDebugTable() {
        Table dbtb = new Table();
        dblb1 = new Label("db1:", m_skin, "small");
        dblb1.setAlignment(Align.left);
        dblb2 = new Label("db1:", m_skin, "small");
        dblb2.setAlignment(Align.left);
        dblb3 = new Label("db1:", m_skin, "small");
        dblb3.setAlignment(Align.left);
        dblb4 = new Label("db1:", m_skin, "small");
        dblb4.setAlignment(Align.left);
        dblb5 = new Label("db1:", m_skin, "small");
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
        res.setDebug(false);
        String resultText = roundWon ? "Congratulations!" : "You lost";

        Label.LabelStyle small = m_skin.get("small", Label.LabelStyle.class);
        Label.LabelStyle big = m_skin.get("ddd", Label.LabelStyle.class);
        big.font.getData().setScale(10);
        small.font.getData().setScale(7);
        big.background = null;
        small.background = null;

        m_resultLabel = new Label(resultText, big);
        staticScoreLbl.setStyle(big);
        staticTimeLbl.setStyle(big);
        m_timeLbl.setStyle(small);
        m_scoreLbl.setStyle(small);

        res.center();
        res.add(m_resultLabel).colspan(2).row();
        res.add(staticTimeLbl).padRight(100);
        res.add(staticScoreLbl).row();
        res.add(m_timeLbl);
        res.add(m_scoreLbl);

        return res;
    }

    public void handleGameEnd() {
        resultTable = createResultTable();
        stage.clear();
        stage.addActor(resultTable);
        m_game.removeInputHandler(gd);

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
}



