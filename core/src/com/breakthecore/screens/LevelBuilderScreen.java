package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.Coords2D;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.tiles.RegularTile;
import com.breakthecore.tiles.TilemapTile;
import com.breakthecore.ui.UIComponent;

import java.util.Locale;

public class LevelBuilderScreen extends ScreenBase {
    private RenderManager renderManager;
    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private TilemapManager tilemapManager;
    private Stage stage;
    private Skin skin;
    private UIComponent uiTools;
    private UIDebug uiDebug;

    private ScreenToWorldTranslator screenToWorld;

    private FreeMode freeMode;
    private DrawMode drawMode;
    private EraseMode eraseMode;
    private RotateMode rotateMode;
    private Mode activeMode;

    public LevelBuilderScreen(BreakTheCoreGame game) {
        super(game);
        renderManager = game.getRenderManager();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1080, 1920, camera);
        camera.position.set(WorldSettings.getWorldWidth()/2, WorldSettings.getWorldHeight()/2, 0);
        camera.update();

        tilemapManager = new TilemapManager();
        tilemapManager.init(1);

        tilemapManager.getTilemap(0).setRelativeTile(0, 0, new TilemapTile(new RegularTile(0)));

        stage = setupStage();

        screenToWorld = new ScreenToWorldTranslator(camera);
        drawMode = new DrawMode();
        eraseMode = new EraseMode();
        rotateMode = new RotateMode();
        freeMode = new FreeMode();
        activeMode = freeMode;

        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(new GestureDetector(new LevelBuilderGestureListner()));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height);
    }

    @Override
    public void render(float delta) {
        tilemapManager.update(delta);
        drawTilemap();
        stage.act();
        stage.draw();
    }

    private Stage setupStage() {
        Stage stage = new Stage(gameInstance.getWorldViewport());
        skin = gameInstance.getSkin();
        uiTools = new UITools();
        uiDebug = new UIDebug();

        Table main = new Table();
        main.setFillParent(true);

        main.center().left().add(uiDebug.getRoot()).expandX().align(Align.left);
        main.center().right().add(uiTools.getRoot()).expandX().align(Align.right);

        stage.addActor(main);
        return stage;
    }

    private void drawTilemap() {
        int tilemapCount = tilemapManager.getTilemapCount();

        renderManager.start(camera.combined);
        for (int i = 0; i < tilemapCount; ++i) {
            renderManager.draw(tilemapManager.getTilemap(i));
        }
        renderManager.end();
        renderManager.renderCenterDot(tilemapManager.getTilemapPosition(), camera.combined);
    }

    private class UITools extends UIComponent {
        TextButton tbDraw, tbErase, tbRotate;

        UITools() {
            TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
            tbs.checked = skin.newDrawable("box_white_5", Color.GREEN);
            tbs.up = skin.newDrawable("box_white_5", Color.GRAY);
            tbs.font = skin.getFont("comic_24b");

            Table main = new Table(skin);
            main.setBackground("box_white_5");
            main.defaults().pad(20);

            tbDraw = new TextButton("Draw", tbs);
            tbDraw.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tbDraw.isChecked()) {
                        activeMode = drawMode;
                    } else {
                        activeMode = freeMode;
                    }
                }
            });
            tbErase = new TextButton("Erase", tbs);
            tbErase.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tbErase.isChecked()) {
                        activeMode = eraseMode;
                    } else {
                        activeMode = freeMode;
                    }
                }
            });
            tbRotate = new TextButton("Rotate", tbs);
            tbRotate.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tbRotate.isChecked()) {
                        activeMode = rotateMode;
                    } else {
                        activeMode = freeMode;
                    }
                }
            });

            ButtonGroup<TextButton> btGroup = new ButtonGroup<TextButton>(tbDraw, tbErase, tbRotate);
            btGroup.setMaxCheckCount(1);
            btGroup.setMinCheckCount(0);

            main.add(tbDraw).width(80).height(80).row();
            main.add(tbErase).width(80).height(80).row();
            main.add(tbRotate).width(80).height(80).row();

            setRoot(main);
        }

    }


    private class UIDebug extends UIComponent {
        Label lblDebug[];

        UIDebug() {
            Table main = new Table();
            lblDebug = new Label[2];

            for (int i = 0; i < lblDebug.length; ++i) {
                lblDebug[i] = new Label("", skin, "comic_24b");
                main.add(lblDebug[i]).left().row();
            }

            setRoot(main);
        }
    }


    private class DrawMode extends Mode {
        @Override
        public boolean tap(float x, float y, int count, int button) {
            screenToWorld.set(x, y);
            Tilemap tm = tilemapManager.getTilemap(0);
            Coords2D res = tm.worldToTilemapCoords(screenToWorld.get());

            if (tm.getRelativeTile(res.x, res.y) == null) {
                tm.setRelativeTile(res.x, res.y, new TilemapTile(new RegularTile(9)));
            }

            return true;
        }
    }


    private class EraseMode extends Mode {
        @Override
        public boolean tap(float x, float y, int count, int button) {
            screenToWorld.set(x, y);
            Tilemap tm = tilemapManager.getTilemap(0);
            Coords2D res = tm.worldToTilemapCoords(screenToWorld.get());

            TilemapTile tile = tm.getRelativeTile(res.x, res.y);
            if (tile != null) {
                Coords2D pos = tile.getRelativePosition();
                if (pos.x != 0 && pos.y != 0) {
                    tm.destroyRelativeTile(res.x, res.y);
                }
            }

            return true;
        }
    }


    private class RotateMode extends Mode {
        private boolean isPanning;
        private Coords2D tmPos = new Coords2D();
        private Vector3 scrPos = new Vector3();
        private float initAngle;
        private Vector2 currPoint = new Vector2();

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (isPanning) {
                float currAngle;
                scrPos.set(x, y, 0);
                scrPos = camera.unproject(scrPos);
                currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                currAngle = currPoint.angle();
                tilemapManager.getTilemap(0).rotate((initAngle - currAngle) * 2.5f);
                initAngle = currAngle;
            } else {
                isPanning = true;
                scrPos.set(x, y, 0);
                scrPos = camera.unproject(scrPos);
                tmPos = tilemapManager.getTilemapPosition();
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
    }


    private class FreeMode extends Mode {
        float currentZoom = 1;

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            camera.position.add(-deltaX * currentZoom, deltaY * currentZoom, 0);

            uiDebug.lblDebug[0].setText(String.format(Locale.ENGLISH, "CamPos| X: %.0f Y: %.0f", camera.position.x, camera.position.y));

            camera.update();
            return true;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            camera.zoom = MathUtils.clamp(initialDistance / distance * currentZoom, 0.16f, 3.f);//currentZoom;
            camera.update();

            uiDebug.lblDebug[1].setText(String.format(Locale.ENGLISH, "Zoom| %.2f", camera.zoom));
            return true;
        }

        @Override
        public void pinchStop() {
            currentZoom = camera.zoom;
        }
    }


    private abstract class Mode implements GestureDetector.GestureListener {
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
            return false;
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


    private class ScreenToWorldTranslator {
        private Camera camera;
        private Vector3 screenCoords = new Vector3();
        private Vector3 worldCoords = new Vector3();

        private ScreenToWorldTranslator(Camera camera) {
            this.camera = camera;
        }

        public void set(float x, float y) {
            screenCoords.set(x, y, 0);
        }

        public Vector3 get() {
            worldCoords = camera.unproject(screenCoords);
            return worldCoords;
        }
    }


    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                gameInstance.setPrevScreen();
            }
            return false;
        }
    }

    private class LevelBuilderGestureListner implements GestureDetector.GestureListener {

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return activeMode != null && activeMode.touchDown(x, y, pointer, button);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            return activeMode != null && activeMode.tap(x, y, count, button);
        }

        @Override
        public boolean longPress(float x, float y) {
            return activeMode != null && activeMode.longPress(x, y);
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return activeMode != null && activeMode.fling(velocityX, velocityY, button);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return activeMode != null && activeMode.pan(x, y, deltaX, deltaY);
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            return activeMode != null && activeMode.panStop(x, y, pointer, button);
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            return activeMode != null && activeMode.zoom(initialDistance, distance);
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return activeMode != null && activeMode.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        }

        @Override
        public void pinchStop() {
            if (activeMode != null) {
                activeMode.pinchStop();
            }
        }
    }
}
