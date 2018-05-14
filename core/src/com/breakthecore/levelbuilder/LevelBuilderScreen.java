package com.breakthecore.levelbuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.breakthecore.CoreSmash;
import com.breakthecore.Coords2D;
import com.breakthecore.levels.Level;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.ScreenBase;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tiles.TileDictionary;
import com.breakthecore.ui.ActorFactory;
import com.breakthecore.ui.GroupStack;
import com.breakthecore.ui.UIComponent;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LevelBuilderScreen extends ScreenBase {
    private ExtendViewport viewport;
    private OrthographicCamera camera;
    private RenderManager renderManager;
    private TilemapManager tilemapManager;

    private GameScreen gameScreen; // XXX:TODO: GET THIS SHIT OUT OF HERE Q.Q

    private LevelBuilder levelBuilder;
    private Stage stage;
    private Skin skin;

    private com.breakthecore.ui.GroupStack prefsStack;
    private UIComponent uiToolbarTop;
    private UITools uiTools;
    private UIInfo uiInfo;
    private final Dialog dlgToast;

    private FreeMode freeMode;
    private DrawMode drawMode;
    private EraseMode eraseMode;
    private RotateMode rotateMode;
    private Mode activeMode;

    public LevelBuilderScreen(CoreSmash game) {
        super(game);
        gameScreen = new GameScreen(game);

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1080, 1920, camera);
        camera.position.set(viewport.getMinWorldWidth() / 2, viewport.getMinWorldHeight() / 2, 0);
        camera.update();

        tilemapManager = new TilemapManager();
        renderManager = game.getRenderManager();

        levelBuilder = new LevelBuilder(tilemapManager, camera);
        stage = setupStage();

        drawMode = new DrawMode(renderManager);
        eraseMode = new EraseMode();
        rotateMode = new RotateMode();
        freeMode = new FreeMode();
        freeMode.activate();

        Window.WindowStyle ws = new Window.WindowStyle();
        ws.background = skin.getDrawable("toast1");
        ws.titleFont = skin.getFont("comic_24b");

        dlgToast = new Dialog("", ws);
        dlgToast.text(new Label("", skin, "comic_24b"));
        dlgToast.setTouchable(Touchable.disabled);

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
        draw();

        stage.act();
        stage.draw();
    }

    public void draw() {
        renderManager.start(camera.combined);
        tilemapManager.draw(renderManager);
        renderManager.end();
        renderManager.renderCenterDot(tilemapManager.getTilemapPosition(), camera.combined);
    }

    private Stage setupStage() {
        Stage stage = new Stage(gameInstance.getUIViewport());
        skin = gameInstance.getSkin();
        uiTools = new UITools();
        uiInfo = new UIInfo();
        uiToolbarTop = new UIToolbarTop();

        prefsStack = new GroupStack();
        prefsStack.setBackground(skin.getDrawable("box_white_10"));

        Stack mainStack = new Stack();
        mainStack.setFillParent(true);

        Table prefs = new Table();
        prefs.center().bottom().add(prefsStack.getRoot()).expandX().fill().padBottom(-10);

        mainStack.addActor(uiTools.getRoot());
        mainStack.addActor(uiToolbarTop.getRoot());
        mainStack.addActor(prefs);
        mainStack.addActor(uiInfo.getRoot());

        stage.addActor(mainStack);
        return stage;
    }

    public void saveProgress() {
        applySettings();
        levelBuilder.saveAs("_editor_");
    }

    private void applySettings() {
        freeMode.mapSettings.saveSettings();
    }

    private void showToast(String text) {
        showToast(text, 2.5f);
    }

    private void showToast(String text, float duration) {
        ((Label) dlgToast.getContentTable().getCells().get(0).getActor()).setText(text);
        dlgToast.show(stage, Actions.sequence(
                Actions.alpha(0),
                Actions.fadeIn(.4f),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        dlgToast.hide(Actions.fadeOut(.4f));
                    }
                })));
        dlgToast.setPosition(camera.viewportWidth / 2 - dlgToast.getWidth() / 2, camera.viewportHeight * .90f);

    }

    private class UILayer implements UIComponent {
        Container<VerticalGroup> root;
        Label lblLayer;

        UILayer() {
            VerticalGroup group = new VerticalGroup();
            root = new Container<>(group);
            root.center().left();

            Button btnUp = new Button(skin.get("default", TextButton.TextButtonStyle.class));
            btnUp.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelBuilder.upLayer();
                    updateLayer();
                }
            });
            btnUp.add().size(50, 50);

            lblLayer = new Label(String.valueOf(levelBuilder.getLayer()), skin, "comic_48b");

            Button btnDown = new Button(skin.get("default", TextButton.TextButtonStyle.class));
            btnDown.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelBuilder.downLayer();
                    updateLayer();
                }
            });
            btnDown.add().size(50, 50);

            group.addActor(btnUp);
            group.addActor(lblLayer);
            group.addActor(btnDown);
            group.center().space(20).pad(10);
        }

        private void updateLayer() {
            lblLayer.setText(levelBuilder.getLayer());
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIToolbarTop implements UIComponent {
        private Container<Table> root;
        private Level testLevel;
        private final TextButton tbSave, tbLoad, tbDeploy;
        private String cacheFileName = "";

        UIToolbarTop() {
            TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
            tbs.up = skin.newDrawable("box_white_5", Color.DARK_GRAY);
            tbs.font = skin.getFont("comic_24b");

            tbSave = new TextButton("Save", tbs);
            tbSave.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.input.getTextInput(new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            int length = text.length();
                            if (length == 0) return;
                            cacheFileName = text;
                            if (length > 2 && length < 17) {
                                applySettings();
                                if (levelBuilder.saveAs(text)) {
                                    showToast("Level Saved");
                                } else {
                                    showToast("Error: Level not saved");
                                }
                            } else {
                                showToast("Error: Invalid name length");
                            }
                        }

                        @Override
                        public void canceled() {

                        }
                    }, "Save File:", cacheFileName.isEmpty() ? "level" : cacheFileName, "Chars Min 3 Max 16");
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
                            cacheFileName = text;
                            if (levelBuilder.load(text)) {
                                freeMode.gameSettings.updateValues();
                                freeMode.mapSettings.updateValues(levelBuilder.getLayer());
                                showToast("File Loaded");
                            } else {
                                showToast("Error: File not found");
                            }
                        }

                        @Override
                        public void canceled() {

                        }
                    }, "Load File:", cacheFileName.isEmpty() ? "level" : cacheFileName, "File name");
                }
            });

            tbDeploy = new TextButton("Deploy", tbs);
            tbDeploy.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelBuilder.saveAs("_editor_");
                        gameScreen.deployLevel(testLevel);
                }
            });

            root = new Container<>();

            Table main = new Table(skin);
            root.top().right().padTop(-10).padRight(-10).setActor(main);
            main.setBackground("box_white_10");
            main.defaults().pad(20);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            main.add(tbDeploy).width(80).height(80);
            main.add(tbSave).width(80).height(80);
            main.add(tbLoad).width(80).height(80);

            testLevel = new Level() {
                @Override
                public void initialize(GameScreen.GameScreenController gameScreenController) {
                    gameScreenController.loadLevel("_editor_");
                }

                @Override
                public void update(float delta, TilemapManager tilemapManager) {
                }

                @Override
                public void end(StatsManager statsManager) {
                }

                @Override
                public int getLevelNumber() {
                    return 999;
                }
            };
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UITools implements UIComponent {
        private Container<Table> root;
        private ButtonGroup<TextButton> btnGroup;

        UITools() {
            root = new Container<>();

            TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
            tbs.checked = skin.newDrawable("box_white_5", Color.GREEN);
            tbs.up = skin.newDrawable("box_white_5", Color.GRAY);
            tbs.font = skin.getFont("comic_24b");

            Table main = new Table(skin);
            root.center().right().padRight(-10).setActor(main);

            main.setBackground("box_white_10");
            main.defaults().pad(20);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            final TextButton tbDraw, tbErase, tbRotate;

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


            btnGroup = new ButtonGroup<TextButton>(tbDraw, tbErase, tbRotate);
            btnGroup.setMaxCheckCount(1);
            btnGroup.setMinCheckCount(0);

            main.add(tbDraw).width(80).height(80).row();
            main.add(tbErase).width(80).height(80).row();
            main.add(tbRotate).width(80).height(80).row();

        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIInfo implements UIComponent {
        Label lbl[];
        Table root;

        UIInfo() {
            root = new Table();
            lbl = new Label[8];

            for (int i = 0; i < lbl.length; ++i) {
                lbl[i] = new Label("", skin, "comic_24b");
                root.left().top().add(lbl[i]).left().row();
            }
        }

        public void reset() {
            for (Label lbl : lbl) {
                lbl.setText("");
            }
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class DrawMode extends Mode {
        private ImageButton materialButtons[];
        private UILayer uiLayer;

        DrawMode(RenderManager renderManager) {
            HorizontalGroup materialGroup = new HorizontalGroup();
            materialGroup.space(10);
            materialGroup.pad(10);
            materialGroup.wrap(true);
            materialGroup.wrapSpace(10);

            Drawable checked = skin.newDrawable("box_white_5", Color.GREEN);
            Drawable unchecked = skin.newDrawable("box_white_5", Color.GRAY);
            ButtonGroup<ImageButton> imgbGroup = new ButtonGroup<>();
            imgbGroup.setMinCheckCount(1);
            imgbGroup.setMaxCheckCount(1);

            final List<Integer> knownIds = TileDictionary.getAllPlaceableIDs();

            materialButtons = new ImageButton[knownIds.size()];
            int buttonIndex = 0;
            for (int i = 0; i < knownIds.size(); ++i) {
                ImageButton.ImageButtonStyle imgbs;
                imgbs = new ImageButton.ImageButtonStyle();
                imgbs.imageUp = new TextureRegionDrawable(new TextureRegion(renderManager.getTextureFor(knownIds.get(i))));
                imgbs.checked = checked;
                imgbs.up = unchecked;

                ImageButton imgb = new ImageButton(imgbs);
                materialGroup.addActor(imgb);
                imgb.getImage().setScaling(Scaling.fill);
                imgb.getImageCell().height(100).width(100);

                final int finalButtonIndex = buttonIndex;
                imgb.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (materialButtons[finalButtonIndex].isChecked()) {
                            levelBuilder.setTileID(knownIds.get(finalButtonIndex));
                            updateTileID();
                        }
                    }
                });

                materialButtons[buttonIndex] = imgb;
                imgbGroup.add(imgb);
                ++buttonIndex;
            }

            materialGroup.center();
            ScrollPane scrollPane = new ScrollPane(materialGroup);

            scrollPane.setOverscroll(false, false);

            uiLayer = new UILayer();
            Table main = new Table(skin);
            main.add(uiLayer.root);
            main.add(scrollPane).fill().expandX().height(270);


            setPreferencesRoot(main);
        }

        @Override
        void onActivate() {
            uiInfo.reset();
            updateTileID();
            uiLayer.updateLayer();
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            levelBuilder.paintAt(x, y);
            return true;
        }


        private void updateTileID() {
            uiInfo.lbl[0].setText("TileID: " + levelBuilder.getCurrentTileID());
        }
    }

    private class EraseMode extends Mode {
        UILayer uiLayer;

        EraseMode() {
            uiLayer = new UILayer();
            setPreferencesRoot(uiLayer.root);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            levelBuilder.eraseAt(x, y);
            return true;
        }

        @Override
        void onActivate() {
            uiInfo.reset();
            uiLayer.updateLayer();
        }
    }

    private class RotateMode extends Mode {
        UILayer uiLayer;
        private boolean isPanning;
        private Coords2D tmPos = new Coords2D();
        private Vector3 scrPos = new Vector3();
        private float initAngle;
        private Vector2 currPoint = new Vector2();

        RotateMode() {
            uiLayer = new UILayer();
            setPreferencesRoot(uiLayer.root);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (isPanning) {
                float currAngle;
                scrPos.set(x, y, 0);
                scrPos = camera.unproject(scrPos);
                currPoint.set(scrPos.x - tmPos.x, scrPos.y - tmPos.y);
                currAngle = currPoint.angle();
                levelBuilder.rotateLayer((initAngle - currAngle) * 2.5f);
                initAngle = currAngle;
            } else {
                isPanning = true;
                scrPos.set(x, y, 0);
                scrPos = camera.unproject(scrPos);
                tmPos = levelBuilder.getLayerPosition();
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
        void onActivate() {
            uiLayer.updateLayer();
        }
    }

    private class FreeMode extends Mode {
        private UIGameSettings gameSettings = new UIGameSettings();
        private UIMapSettings mapSettings = new UIMapSettings();
        float currentZoom = 1;

        FreeMode() {
            HorizontalGroup root = new HorizontalGroup();
            root.pad(20);
            root.space(20);
            root.wrap(true);

            TextButton btnLevelSettings = new TextButton("Level Settings", skin);
            btnLevelSettings.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    prefsStack.push(gameSettings.getRoot());
                }
            });

            TextButton btnMapSettings = new TextButton("Layer Settings", skin);
            btnMapSettings.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    mapSettings.updateValues(levelBuilder.getLayer());
                    prefsStack.push(mapSettings.getRoot());
                }
            });

            root.addActor(btnLevelSettings);
            root.addActor(btnMapSettings);
            setPreferencesRoot(root);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            camera.position.add(-deltaX * currentZoom, deltaY * currentZoom, 0);

            updateCamInfo();
            camera.update();
            return true;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            camera.zoom = MathUtils.clamp(initialDistance / distance * currentZoom, 0.16f, 3.f);//currentZoom;
            camera.update();

            updateCamInfo();
            return true;
        }

        @Override
        public void pinchStop() {
            currentZoom = camera.zoom;
        }

        @Override
        void onActivate() {
            uiInfo.reset();
            updateCamInfo();
        }

        private void updateCamInfo() {
            uiInfo.lbl[0].setText(String.format(Locale.ENGLISH, "Cam Z:%4.2f X:%4.0f Y:%4.0f", camera.zoom, camera.position.x, camera.position.y));
        }

        private class UIGameSettings implements UIComponent {
            Table root;
            Slider sldrBallSpeed, sldrLauncherCooldown, sldrLauncherSize;
            Label lblBallSpeed, lblLauncherCooldown, lblLauncherSize;
            CheckBox cbUseMoves, cbUseLives, cbUseTime, cbSpinTheCoreMode;
            TextField tfMoves, tfLives, tfTime;
            Table tblCheckboxesWithValues;

            final int settingsPadding = 40;

            // XXX(14/4/2018): *TOO* many magic values
            private UIGameSettings() {
                root = new Table();

                Label dummy = new Label
                        (":Level Setup:", skin, "comic_48b");
                root.padLeft(30).padRight(30).padTop(10).padBottom(10).top();
                root.add(dummy).padBottom(20).colspan(3).row();

                Table settingsTbl = new Table();
                settingsTbl.defaults().padBottom(settingsPadding);

                final ScrollPane scrollPane = new ScrollPane(settingsTbl);
                scrollPane.setScrollingDisabled(true, false);
                scrollPane.setCancelTouchFocus(false);
                scrollPane.setOverscroll(false, false);
                root.add(scrollPane).colspan(3).expand().fill().row();

                InputListener stopTouchDown = new InputListener() {
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        event.stop();
                        return false;
                    }
                };

                final TextField.TextFieldListener returnOnNewLineListener = new TextField.TextFieldListener() {
                    public void keyTyped(TextField textField, char key) {
                        if (key == '\n' || key == '\r') {
                            textField.getOnscreenKeyboard().show(false);
                            stage.setKeyboardFocus(null);
                        }
                    }
                };

                cbUseLives = createCheckBox("Use Lives");
                tfLives = createTextField(returnOnNewLineListener);
                tfLives.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        tfLives.setCursorPosition(tfLives.getText().length());
                    }
                });
                tfLives.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        String txtAmount = tfLives.getText();
                        // check to avoid parsing empty string
                        int amount = txtAmount.length() > 0 ? Integer.parseInt(txtAmount) : 0;
                        // ensure positive or 0 number
                        levelBuilder.setLives(amount < 0 ? 0 : amount);
                    }
                });

                cbUseMoves = createCheckBox("Use Moves");
                tfMoves = createTextField(returnOnNewLineListener);
                tfMoves.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        tfMoves.setCursorPosition(tfMoves.getText().length());
                    }
                });
                tfMoves.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        String txtAmount = tfMoves.getText();
                        // check to avoid parsing empty string
                        int amount = txtAmount.length() > 0 ? Integer.parseInt(txtAmount) : 0;
                        // ensure positive or 0 number
                        levelBuilder.setMoves(amount < 0 ? 0 : amount);
                    }
                });

                cbUseTime = createCheckBox("Use Time");
                tfTime = createTextField(returnOnNewLineListener);
                tfTime.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        tfTime.setCursorPosition(tfTime.getText().length());
                    }
                });
                tfTime.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        String txtAmount = tfTime.getText();
                        // check to avoid parsing empty string
                        int amount = txtAmount.length() > 0 ? Integer.parseInt(txtAmount) : 0;
                        // ensure positive or 0 number
                        levelBuilder.setTime(amount < 0 ? 0 : amount);
                    }
                });


                tblCheckboxesWithValues = new Table();
                tblCheckboxesWithValues.defaults().padBottom(settingsPadding);

                tblCheckboxesWithValues.add(cbUseLives).left().padRight(15);
                tblCheckboxesWithValues.add(tfLives).center().width(101).padRight(60);
                tblCheckboxesWithValues.add(cbSpinTheCoreMode).left();
                tblCheckboxesWithValues.add().row();

                tblCheckboxesWithValues.add(cbUseMoves).left().padRight(15);
                tblCheckboxesWithValues.add(tfMoves).center().width(101).padRight(60);
                tblCheckboxesWithValues.add().left();
                tblCheckboxesWithValues.add().row();

                tblCheckboxesWithValues.add(cbUseTime).left().padRight(15).padBottom(0);
                tblCheckboxesWithValues.add(tfTime).center().width(101).padRight(60).padBottom(0);
                tblCheckboxesWithValues.add().padBottom(0);
                tblCheckboxesWithValues.add().padBottom(0).row();

                settingsTbl.add(tblCheckboxesWithValues).colspan(3).expandX().left().row();

                sldrBallSpeed = new Slider(levelBuilder.getBallSpeed(), 20, 1, false, skin);
                lblBallSpeed = new Label(String.valueOf((int) sldrBallSpeed.getValue()), skin, "comic_48");
                sldrBallSpeed.addListener(stopTouchDown);
                sldrBallSpeed.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblBallSpeed.setText(String.valueOf((int) sldrBallSpeed.getValue()));
                        levelBuilder.setBallSpeed((int) sldrBallSpeed.getValue());
                    }
                });
                attachSliderToTable("Ball Speed", sldrBallSpeed, lblBallSpeed, settingsTbl);

                sldrLauncherCooldown = new Slider(0f, 4.8f, .16f, false, skin);
                lblLauncherCooldown = new Label(String.format(Locale.ENGLISH, "%.2f", sldrLauncherCooldown.getValue()), skin, "comic_48");
                sldrLauncherCooldown.addListener(stopTouchDown);
                sldrLauncherCooldown.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblLauncherCooldown.setText(String.format(Locale.ENGLISH, "%.2f", sldrLauncherCooldown.getValue()));
                        levelBuilder.setLauncherCooldown(sldrLauncherCooldown.getValue());
                    }
                });
                attachSliderToTable("Launcher Cooldown", sldrLauncherCooldown, lblLauncherCooldown, settingsTbl);

                sldrLauncherSize = new Slider(levelBuilder.getLauncherSize(), 5, 1, false, skin);
                lblLauncherSize = new Label(String.valueOf((int) sldrLauncherSize.getValue()), skin, "comic_48");
                sldrLauncherSize.addListener(stopTouchDown);
                sldrLauncherSize.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblLauncherSize.setText(String.valueOf((int) sldrLauncherSize.getValue()));
                        levelBuilder.setLauncherSize((int) sldrLauncherSize.getValue());
                    }
                });
                attachSliderToTable("Launcher Size", sldrLauncherSize, lblLauncherSize, settingsTbl);
            }

            void updateValues() {
                sldrLauncherSize.setValue(levelBuilder.getLauncherSize());
                sldrBallSpeed.setValue(levelBuilder.getBallSpeed());
                sldrLauncherCooldown.setValue(levelBuilder.getLauncherCooldown());

                tfLives.setText(String.valueOf(levelBuilder.getLives()));
                tfMoves.setText(String.valueOf(levelBuilder.getMoves()));
                tfTime.setText(String.valueOf(levelBuilder.getTime()));
            }

            private TextField createTextField(TextField.TextFieldListener backOnNewLineListener) {
                TextField tf = new TextField("", skin);
                tf.setAlignment(Align.center);
                tf.setMaxLength(3);
                tf.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                tf.setTextFieldListener(backOnNewLineListener);
                return tf;
            }

            private CheckBox createCheckBox(String name) {
                CheckBox cb = new CheckBox(name, skin);
                cb.getImageCell().width(cb.getLabel().getPrefHeight()).height(cb.getLabel().getPrefHeight()).padRight(15);
                cb.getImage().setScaling(Scaling.fill);
                return cb;
            }

            private void attachSliderToTable(String name, Slider slider, Label amountLbl, Table tbl) {
                Label dummy = new Label(name + ":", skin, "comic_32b");
                tbl.add(dummy).padRight(30).align(Align.right).padBottom(5);
                tbl.add(amountLbl).align(Align.left).padBottom(5).expandX().row();
                tbl.add(slider).colspan(tbl.getColumns()).growX().row();
            }

            @Override
            public Group getRoot() {
                return root;
            }
        }

        private class UIMapSettings implements UIComponent {
            Container<Table> root;
            Slider sldrMinRot, sldrMaxRot, sldrColorCount;
            Label lblMinRot, lblMaxRot, lblColorCount, lblLayer;
            CheckBox cbRotateCounterCW;
            int activeLayer;

            UIMapSettings() {
                activeLayer = levelBuilder.getLayer();
                sldrMinRot = new Slider(0, 120, 1, false, skin);
                lblMinRot = new Label(String.format(Locale.ROOT, "Min:%3d", (int) sldrMinRot.getValue()), skin, "comic_48");
                sldrMinRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrMinRot.getValue();
                        int max = (int) sldrMaxRot.getValue();
                        if (Integer.compare(min, max) == 1) {
                            sldrMaxRot.setValue(min);
                        }
                        lblMinRot.setText(String.format(Locale.ROOT, "Min:%3d", min));
                    }
                });

                sldrMaxRot = new Slider(0, 120, 1, false, skin);
                lblMaxRot = new Label(String.format(Locale.ROOT, "Max:%3d", (int) sldrMaxRot.getValue()), skin, "comic_48");
                sldrMaxRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrMinRot.getValue();
                        int max = (int) sldrMaxRot.getValue();
                        if (Integer.compare(min, max) == 1) {
                            sldrMinRot.setValue(max);
                        }
                        lblMaxRot.setText(String.format(Locale.ROOT, "Max:%3d", max));
                    }
                });

                sldrColorCount = new Slider(levelBuilder.getColorCount(), 8, 1, false, skin);
                lblColorCount = new Label(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()), skin, "comic_48");
                sldrColorCount.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblColorCount.setText(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()));
                    }
                });

                cbRotateCounterCW = ActorFactory.createCheckBox("Rotate CCW", skin);

                VerticalGroup grpLayer = new VerticalGroup();

                Button btnUp = new Button(skin.get("default", TextButton.TextButtonStyle.class));
                btnUp.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        saveSettings();
                        int newLayer = levelBuilder.upLayer();
                        if (Integer.compare(newLayer, activeLayer) != 0) {
                            updateValues(newLayer);
                        }
                    }
                });
                btnUp.add().size(50, 50);

                lblLayer = new Label(String.valueOf(levelBuilder.getLayer()), skin, "comic_48b");

                Button btnDown = new Button(skin.get("default", TextButton.TextButtonStyle.class));
                btnDown.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        saveSettings();
                        int newLayer = levelBuilder.downLayer();
                        if (Integer.compare(newLayer, activeLayer) != 0) {
                            updateValues(newLayer);
                        }
                    }
                });
                btnDown.add().size(50, 50);

                grpLayer.addActor(btnUp);
                grpLayer.addActor(lblLayer);
                grpLayer.addActor(btnDown);
                grpLayer.center().space(20).pad(10);

                Label slblRotation = new Label(":Rotation:", skin, "comic_48b");

                Table rotLabels = new Table();
                rotLabels.defaults().expandX().padBottom(30);
                rotLabels.columnDefaults(0).padRight(40);
                rotLabels.add(slblRotation).colspan(2).padRight(0).padBottom(5).row();
                rotLabels.add(lblMinRot).padBottom(5);
                rotLabels.add(lblMaxRot).padBottom(5).row();
                rotLabels.add(sldrMinRot).fill();
                rotLabels.add(sldrMaxRot).fill().row();
                rotLabels.add(cbRotateCounterCW).colspan(2).padRight(0).left().row();

                rotLabels.add(lblColorCount).colspan(2).padRight(0).padBottom(5).row();
                rotLabels.add(sldrColorCount).colspan(2).padRight(0).fill().row();

                VerticalGroup vgroup = new VerticalGroup();
                vgroup.grow().pad(30);
                vgroup.addActor(rotLabels);

                Table main = new Table();
                main.add(grpLayer);
                main.add(vgroup).grow();

                root = new Container<>(main);
                root.fill();
            }

            private void updateValues(int layer) {
                activeLayer = layer;
                lblLayer.setText(layer);
                sldrMaxRot.setValue(levelBuilder.getMaxSpeed());
                sldrMinRot.setValue(levelBuilder.getMinSpeed());
                sldrColorCount.setValue(levelBuilder.getColorCount());
                cbRotateCounterCW.setChecked(levelBuilder.isCCWRotationEnabled());
            }

            public void saveSettings() {
                levelBuilder.setMinSpeed((int) sldrMinRot.getValue());
                levelBuilder.setMaxSpeed((int) sldrMaxRot.getValue());
                levelBuilder.setColorCount((int) sldrColorCount.getValue());
                levelBuilder.setCCWRotation(cbRotateCounterCW.isChecked());
            }

            @Override
            public Group getRoot() {
                return root;
            }
        }

    }

    private abstract class Mode extends GestureDetector.GestureAdapter {
        private Group preferences;

        public void activate() {
            activeMode = this;

            prefsStack.setRoot(preferences);
            onActivate();
        }

        void setPreferencesRoot(Group preferences) {
            this.preferences = preferences;
        }

        abstract void onActivate();
    }

    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                if (prefsStack.size() == 1) {
                    if (activeMode == freeMode) {
                        gameInstance.setPrevScreen();
                    } else {
                        uiTools.btnGroup.uncheckAll();
                    }
                } else {
                    prefsStack.pop();
                }
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
