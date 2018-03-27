package com.breakthecore.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.breakthecore.MovingTileManager;
import com.breakthecore.RenderManager;
import com.breakthecore.Tile;
import com.breakthecore.TileMap;

/**
 * Created by Michail on 17/3/2018.
 */

public class GameScreen extends ScreenAdapter implements GestureDetector.GestureListener {
    private Game m_game;
    private ScreenViewport screenViewport;
    private OrthographicCamera screenCamera;
    private TileMap tileMap;
    private RenderManager renderManager;

    //===========
    Stage stage;
    Label fpslbl, coordsLbl;
    //===========
    private final int colorCount = 7;
    private final int sideLength = 64;

    private MovingTileManager movingTileManager;

    public GameScreen(Game game) {
        m_game = game;
        screenViewport = new ScreenViewport();
        screenCamera = (OrthographicCamera) screenViewport.getCamera();
        screenCamera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        renderManager = new RenderManager(sideLength, colorCount);
        movingTileManager = new MovingTileManager(sideLength, colorCount);


//        tileMap = new TileMap(new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - Gdx.graphics.getHeight() / 4
//        ), 37, sideLength);

        tileMap = new TileMap(new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - Gdx.graphics.getHeight() / 4), 25, sideLength);

        GestureDetector gd = new GestureDetector(this);
        Gdx.input.setInputProcessor(gd);

        initHexTilemap(tileMap, 3);
        setupStage();
    }

    @Override
    public void render(float delta) {
        movingTileManager.update(delta);
        tileMap.update(delta);
        tileMap.handleCollision(movingTileManager.getActiveList());
        updateStage();

        renderManager.start(screenCamera.combined);
        renderManager.draw(tileMap);
        renderManager.drawLauncher(movingTileManager.getLauncherQueue(), movingTileManager.getLauncherPos());
        renderManager.draw(movingTileManager.getActiveList());
        renderManager.end();

        renderManager.renderCenterDot(screenCamera.combined);

        stage.draw();
    }

    private void updateStage() {
        fpslbl.setText(String.valueOf(Gdx.graphics.getFramesPerSecond()));
    }

    @Override
    public void resize(int width, int height) {
        screenViewport.update(width, height);
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

    private void setupStage() {
        stage = new Stage(new ScreenViewport());
        Table debugTbl = new Table();
        fpslbl = new Label("null", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        coordsLbl = new Label("null", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        debugTbl.top().left();
        debugTbl.setFillParent(true);
        debugTbl.add(fpslbl).left().row();
        debugTbl.add(coordsLbl).left().row();
        stage.addActor(debugTbl);
    }

    private void initHexTilemap(TileMap tm, int radius) {
        Tile[][] tiles = tm.getTiles();
        int center_tile = tm.getSize() / 2;
        boolean firstY,firstX = firstY = true;

        if (radius == 0) {
            tiles[center_tile][center_tile] = new TileMap.TilemapTile(
                    center_tile, center_tile,
                    movingTileManager.getRandomColor());
                    return;
        }


        for (int y = -radius * 3; y < radius * 3; ++y) {
            float xOffset = ((y) % 2 == 0)? 0 : .75f;
            for (int x = -radius; x < radius; ++x) {
                float yOffset = ((x) % 2 == 0)? 0 :  0.5f;
                if (Vector2.dst(x*1.5f+xOffset,y*0.5f,0,0) <= radius) {
                    tiles[y + center_tile][x + center_tile] = new TileMap.TilemapTile(
                            x, y,
                            movingTileManager.getRandomColor());
                }
            }
        }

        if (false) {
            for (int y = -radius * 3; y < radius * 3; ++y) {
                for (int x = -radius; x < radius; ++x) {
                    if (firstY) continue;
                    if (firstX) {
                        if (y % 2 == 0) {
                            firstX = false;
                            continue;
                        }
                    }
                    tiles[y + center_tile][x + center_tile] = new TileMap.TilemapTile(
                            x, y,
                            movingTileManager.getRandomColor());
                }
                firstY = false;
                firstX = true;
            }
        }

    }

    // fills entire tilemap with tiles
    private void fillEntireTilemap(TileMap tm) {
        Tile[][] tiles = tm.getTiles();
        int center_tile = tm.getSize() / 2;
        int oddOrEvenFix = tm.getSize()%2 == 1 ? 1: 0;
        int tmp = (tm.getSize()*3)/2;
        for (int y = -tmp; y < tmp; ++y) {
            for (int x = -tm.getSize()/2; x < tm.getSize()/2+oddOrEvenFix; ++x) {
                tiles[y+(center_tile)*3+oddOrEvenFix][x+center_tile] = new TileMap.TilemapTile(
                        x, y,
                        movingTileManager.getRandomColor());
            }
        }
    }

}



