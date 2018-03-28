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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.breakthecore.BreakTheCoreGame;
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

public class GameScreen extends ScreenAdapter implements GestureDetector.GestureListener {
    private BreakTheCoreGame m_game;
    private FitViewport m_fitViewport;
    private OrthographicCamera m_camera;
    private Tilemap m_tilemap;
    private TilemapManager m_tilemapManager;
    private RenderManager renderManager;

    Skin m_skin;

    //===========
    Stage stage;
    Label m_timeLbl, m_scoreLbl;
    Table mainTable;
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

        // XXX(3/28/2018): I need a standalone way to handle input here...
        GestureDetector gd = new GestureDetector(this);
        game.addInputHandler(gd);

        initHexTilemap(m_tilemap, 5);
    }

    @Override
    public void render(float delta) {
        update(delta);

        renderManager.start(m_camera.combined);
        renderManager.draw(m_tilemap);
        renderManager.drawLauncher(movingTileManager.getLauncherQueue(), movingTileManager.getLauncherPos());
        renderManager.draw(movingTileManager.getActiveList());
//        renderManager.debugTileDistances(m_tilemap.getTilemapTiles());
        renderManager.end();

        renderManager.renderCenterDot(m_camera.combined);

        stage.draw();
    }

    private void update(float delta) {
        m_time += delta;
        movingTileManager.update(delta);
        m_tilemapManager.update(delta);
        m_tilemapManager.checkForCollision(movingTileManager.getActiveList());
        updateStage();
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

    private void initHexTilemap(Tilemap tm, int radius) {
        if (radius == 0) {
            tm.setTile(0, 0, new TilemapTile(movingTileManager.getRandomColor()));
            return;
        }

        for (int y = -radius * 3; y < radius * 3; ++y) {
            float xOffset = ((y) % 2 == 0) ? 0 : .75f;
            for (int x = -radius; x < radius; ++x) {
                if (Vector2.dst(x * 1.5f + xOffset, y * 0.5f, 0, 0) <= radius) {
                    tm.setTile(x, y, new TilemapTile(movingTileManager.getRandomColor()));
                }
            }
        }
    }

    private void fillEntireTilemap(Tilemap tm) {
        Tile[][] tiles = tm.getTilemapTiles();
        int center_tile = tm.getSize() / 2;
        int oddOrEvenFix = tm.getSize() % 2 == 1 ? 1 : 0;
        int tmp = (tm.getSize() * 3) / 2;
        for (int y = -tmp; y < tmp; ++y) {
            for (int x = -tm.getSize() / 2; x < tm.getSize() / 2 + oddOrEvenFix; ++x) {
                tiles[y + (center_tile) * 3 + oddOrEvenFix][x + center_tile] = new TilemapTile(
                        x, y,
                        movingTileManager.getRandomColor());
            }
        }
    }

    private void updateStage() {
        m_timeLbl.setText(String.format("%d:%02d", (int) m_time / 60, (int) m_time % 60));
        m_scoreLbl.setText(String.valueOf(m_tilemapManager.getTmpScore()));
    }

    private void setupUI() {
        m_skin = createSkin();
        Label staticTimeLbl, staticScoreLbl;

        stage = new Stage(new FitViewport(WorldSettings.getWorldWidth(), WorldSettings.getWorldHeight()));
        mainTable = new Table();
        mainTable.setDebug(false);

        m_timeLbl = new Label("null", m_skin, "ddd");
        m_timeLbl.setAlignment(Align.center);
        m_timeLbl.setWidth(mainTable.getWidth() / 2);
        m_timeLbl.getStyle().font.getData().setScale(4);

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
        stage.addActor(mainTable);
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
        bf.getData().setScale(2);
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

}



