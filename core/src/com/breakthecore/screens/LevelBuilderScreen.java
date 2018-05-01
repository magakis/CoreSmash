package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.breakthecore.CoreSmash;
import com.breakthecore.Coords2D;
import com.breakthecore.LevelFormatParser;
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
    private Container prefsContainer;
    private UIComponent uiTools, uiToolbarTop;
    private UIDebug uiDebug;

    private ScreenToWorldTranslator screenToWorld;

    private FreeMode freeMode;
    private DrawMode drawMode;
    private EraseMode eraseMode;
    private RotateMode rotateMode;
    private Mode activeMode;

    public LevelBuilderScreen(CoreSmash game) {
        super(game);
        renderManager = game.getRenderManager();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1080, 1920, camera);
        camera.position.set(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() / 2, 0);
        camera.update();

        tilemapManager = new TilemapManager();

        Tilemap tm = tilemapManager.newTilemap();
        tm.setRelativeTile(0, 0, new RegularTile(0));

        stage = setupStage();

        screenToWorld = new ScreenToWorldTranslator(camera);
        drawMode = new DrawMode();
        eraseMode = new EraseMode();
        rotateMode = new RotateMode();
        freeMode = new FreeMode();
        activeMode = freeMode;

        uiDebug.lblDebug[0].setText(String.format(Locale.ENGLISH, "CamPos| X: %.0f Y: %.0f", camera.position.x, camera.position.y));
        uiDebug.lblDebug[1].setText(String.format(Locale.ENGLISH, "Zoom| %.2f", camera.zoom));

        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(new GestureDetector(new LevelBuilderGestureListner()));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        uiToolbarTop = new UIToolbarTop();

        prefsContainer = new Container();
        prefsContainer.fill();
        prefsContainer.setTouchable(Touchable.enabled);
        prefsContainer.addCaptureListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                event.handle();
                return true;
            }
        });

        Stack mainStack = new Stack();
        mainStack.setFillParent(true);

        Table prefs = new Table();
        prefs.center().bottom().add(prefsContainer).expandX().fill();
        mainStack.addActor(prefs);

        mainStack.addActor(uiTools.getRoot());
        mainStack.addActor(uiToolbarTop.getRoot());
        mainStack.addActor(uiDebug.getRoot());


        stage.addActor(mainStack);
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


    private class UIToolbarTop extends UIComponent {
        private final TextButton tbSave, tbLoad;
        private final Dialog dlgToast;

        UIToolbarTop() {
            TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
            tbs.up = skin.newDrawable("box_white_5", Color.DARK_GRAY);
            tbs.font = skin.getFont("comic_24b");

            Window.WindowStyle ws = new Window.WindowStyle();
            ws.background = skin.getDrawable("toast1");
            ws.titleFont = skin.getFont("comic_24b");
            dlgToast = new Dialog("", ws);
            dlgToast.text(new Label("", skin, "comic_48"));
            dlgToast.getContentTable().pad(5);
            dlgToast.setTouchable(Touchable.disabled);

            tbSave = new TextButton("Save", tbs);
            tbSave.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.input.getTextInput(new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            int length = text.length();
                            if (length > 2 && length < 17) {
                                LevelFormatParser.saveTo(text, tilemapManager);
                                ((Label) dlgToast.getContentTable().getCells().get(0).getActor()).setText("File Saved!");
                                dlgToast.show(stage, Actions.sequence(
                                        Actions.alpha(0),
                                        Actions.fadeIn(.4f),
                                        Actions.delay(2),
                                        Actions.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                dlgToast.hide(Actions.fadeOut(.4f));
                                            }
                                        })));
                                dlgToast.setPosition(camera.viewportWidth / 2 - dlgToast.getWidth() / 2, camera.viewportHeight * .90f);
                            }
                        }

                        @Override
                        public void canceled() {

                        }
                    }, "File name:", "", "Chars Min 3 Max 16");
                }
            });
            tbLoad = new TextButton("Load", tbs);
            tbLoad.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.input.getTextInput(new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            if (text.length() == 0) return;
                            if (!LevelFormatParser.fileExists(text)) {
                                ((Label) dlgToast.getContentTable().getCells().get(0).getActor()).setText("Error: File not found");
                                dlgToast.show(stage, Actions.sequence(
                                        Actions.alpha(0),
                                        Actions.fadeIn(.4f, Interpolation.fade),
                                        Actions.delay(2),
                                        Actions.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                dlgToast.hide(Actions.fadeOut(.4f));
                                            }
                                        })));
                                dlgToast.setPosition(camera.viewportWidth / 2 - dlgToast.getWidth() / 2, camera.viewportHeight * .90f);
                                return;
                            }
                            tilemapManager.reset();
                            LevelFormatParser.load(text, tilemapManager);
                        }

                        @Override
                        public void canceled() {

                        }
                    }, "File name:", "", "");
                }
            });

            Container<Table> container = new Container<>();

            Table main = new Table(skin);
            container.top().right().setActor(main);
            main.setBackground("box_white_5");
            main.defaults().pad(20);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            main.add(tbSave).width(80).height(80);
            main.add(tbLoad).width(80).height(80);

            setRoot(container);
        }
    }


    private class UITools extends UIComponent {
        private TextButton tbDraw, tbErase, tbRotate;

        UITools() {
            Container<Table> container = new Container<>();

            TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
            tbs.checked = skin.newDrawable("box_white_5", Color.GREEN);
            tbs.up = skin.newDrawable("box_white_5", Color.GRAY);
            tbs.font = skin.getFont("comic_24b");

            Table main = new Table(skin);
            container.center().right().setActor(main);

            main.setBackground("box_white_5");
            main.defaults().pad(20);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            tbDraw = new TextButton("Draw", tbs);
            tbDraw.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tbDraw.isChecked()) {
                        drawMode.activate();
                    } else {
                        freeMode.activate();
                    }
                }
            });
            tbErase = new TextButton("Erase", tbs);
            tbErase.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tbErase.isChecked()) {
                        eraseMode.activate();
                    } else {
                        freeMode.activate();
                    }
                }
            });
            tbRotate = new TextButton("Rotate", tbs);
            tbRotate.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tbRotate.isChecked()) {
                        rotateMode.activate();
                    } else {
                        freeMode.activate();
                    }
                }
            });


            ButtonGroup<TextButton> btGroup = new ButtonGroup<TextButton>(tbDraw, tbErase, tbRotate);
            btGroup.setMaxCheckCount(1);
            btGroup.setMinCheckCount(0);

            main.add(tbDraw).width(80).height(80).row();
            main.add(tbErase).width(80).height(80).row();
            main.add(tbRotate).width(80).height(80).row();

            setRoot(container);
        }

    }


    private class UIDebug extends UIComponent {
        Label lblDebug[];

        UIDebug() {
            Table main = new Table();
            lblDebug = new Label[4];

            for (int i = 0; i < lblDebug.length; ++i) {
                lblDebug[i] = new Label("", skin, "comic_24b");
                main.left().top().add(lblDebug[i]).left().row();
            }

            setRoot(main);
        }
    }


    private class DrawMode extends Mode {
        private int tileId;
        private ImageButton materialButtons[];

        DrawMode() {
            Table main = new Table(skin);
            main.setBackground("box_white_5");

            HorizontalGroup materialGroup = new HorizontalGroup();
            main.add(materialGroup).grow();
            materialGroup.space(10);
            materialGroup.pad(10);
            materialGroup.wrap(true);
            materialGroup.wrapSpace(10);


            Drawable checked = skin.newDrawable("box_white_5", Color.GREEN);
            Drawable unchecked = skin.newDrawable("box_white_5", Color.GRAY);
            ButtonGroup<ImageButton> imgbGroup = new ButtonGroup<ImageButton>();
            imgbGroup.setMinCheckCount(1);
            imgbGroup.setMaxCheckCount(1);

            ImageButton.ImageButtonStyle imgbs;

            final Color[] colors = gameInstance.getRenderManager().getColorList();
            materialButtons = new ImageButton[colors.length + 1];
            int buttonIndex = 0;
            for (int i = 0; i < colors.length; ++i) {
                imgbs = new ImageButton.ImageButtonStyle();
                imgbs.imageUp = skin.newDrawable("asteroid", colors[buttonIndex]);
                imgbs.checked = checked;
                imgbs.up = unchecked;

                ImageButton imgb = new ImageButton(imgbs);
                imgbGroup.add(imgb);
                materialButtons[buttonIndex] = imgb;
                materialGroup.addActor(imgb);
                imgb.getImage().setScaling(Scaling.fill);
                imgb.getImageCell().height(100).width(100);

                final int finalI = buttonIndex;
                imgb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (materialButtons[finalI].isChecked()) {
                            tileId = finalI;
                            uiDebug.lblDebug[2].setText("TileActive| " + tileId);
                        }
                    }
                });
                if (buttonIndex == 8) main.row();

                ++buttonIndex;
            }

            imgbs = new ImageButton.ImageButtonStyle();
            imgbs.imageUp = skin.newDrawable("ball");
            imgbs.checked = checked;
            imgbs.up = unchecked;

            ImageButton imgb = new ImageButton(imgbs);
            imgb.getImageCell().height(100).width(100);
            imgb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (materialButtons[colors.length].isChecked()) {
                        tileId = -1;
                        uiDebug.lblDebug[2].setText("TileActive| " + tileId);
                    }
                }
            });
            imgbGroup.add(imgb);
            materialButtons[buttonIndex++] = imgb;
            materialGroup.addActor(imgb);

            setPreferencesRoot(main);
        }


        @Override
        public boolean tap(float x, float y, int count, int button) {
            screenToWorld.set(x, y);
            Tilemap tm = tilemapManager.getTilemap(0);
            Coords2D res = tm.worldToTilemapCoords(screenToWorld.get());

            if (tm.getRelativeTile(res.x, res.y) == null) {
                tm.setRelativeTile(res.x, res.y, new RegularTile(tileId));
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
                if (pos.x != 0 || pos.y != 0) {
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


    private abstract class Mode implements GestureDetector.GestureListener {
        private Group preferences;


        public void activate() {
            activeMode = this;
            prefsContainer.setActor(preferences);
        }

        void setPreferencesRoot(Group preferences) {
            this.preferences = preferences;
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


    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
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
