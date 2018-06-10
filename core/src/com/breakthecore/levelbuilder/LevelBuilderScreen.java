package com.breakthecore.levelbuilder;

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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
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
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.breakthecore.Coords2D;
import com.breakthecore.CoreSmash;
import com.breakthecore.levels.Level;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.ScreenBase;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tiles.TileIndex;
import com.breakthecore.ui.ActorFactory;
import com.breakthecore.ui.Components;
import com.breakthecore.ui.LoadFileDialog;
import com.breakthecore.ui.SaveFileDialog;
import com.breakthecore.ui.UIComponent;
import com.breakthecore.ui.UIComponentStack;

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

    private UIComponentStack prefsStack;
    private UIComponent uiToolbarTop;
    private UITools uiTools;
    private UIInfo uiInfo;

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

        drawMode = new DrawMode();
        eraseMode = new EraseMode();
        rotateMode = new RotateMode();
        freeMode = new FreeMode();
        freeMode.activate();

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
        levelBuilder.draw(renderManager);
        renderManager.end();
        renderManager.renderCenterDot(tilemapManager.getTilemapPosition(), camera.combined);
    }

    private Stage setupStage() {
        Stage stage = new Stage(gameInstance.getUIViewport());
        skin = gameInstance.getSkin();
        uiTools = new UITools();
        uiInfo = new UIInfo();
        uiToolbarTop = new UIToolbarTop();

        prefsStack = new UIComponentStack();
        prefsStack.setBackground(skin.getDrawable("box_white_5"));

        Stack mainStack = new Stack();
        mainStack.setFillParent(true);

        Table prefs = new Table();
        prefs.center().bottom().add(prefsStack.show()).expandX().fill().padBottom(-5);

        mainStack.addActor(uiTools.show());
        mainStack.addActor(uiToolbarTop.show());
        mainStack.addActor(prefs);
        mainStack.addActor(uiInfo.show());

        stage.addActor(mainStack);
        return stage;
    }

    public void saveProgress() {
        levelBuilder.saveAs("_editor_");
    }

    private abstract class UILayer implements UIComponent {
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
                    onLayerChange(levelBuilder.getLayer());
                }
            });
            btnUp.add().size(Value.percentWidth(0.3f, root));

            lblLayer = new Label(String.valueOf(levelBuilder.getLayer()), skin, "h4");

            Button btnDown = new Button(skin.get("default", TextButton.TextButtonStyle.class));
            btnDown.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelBuilder.downLayer();
                    updateLayer();
                    onLayerChange(levelBuilder.getLayer());
                }
            });
            btnDown.add().size(Value.percentWidth(0.3f, root));

            group.addActor(btnUp);
            group.addActor(lblLayer);
            group.addActor(btnDown);
            group.center().space(20).pad(5);
        }

        private void updateLayer() {
            lblLayer.setText(levelBuilder.getLayer());
        }

        public abstract void onLayerChange(int layer);

        @Override
        public Group show() {
            return root;
        }
    }

    private class UIToolbarTop implements UIComponent {
        private Container<Table> root;
        private SaveFileDialog saveFileDialog;
        private LoadFileDialog loadFileDialog;
        private Level testLevel;
        private final TextButton tbSave, tbLoad, tbDeploy, tbAssign;
        private String filenameCache = "";

        UIToolbarTop() {
            tbSave = new TextButton("Save", skin, "box_gray_5");
            tbSave.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    saveFileDialog.show(stage, filenameCache);
                }
            });
            tbLoad = new TextButton("Load", skin, "box_gray_5");
            tbLoad.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    loadFileDialog.show(stage);
                }
            });

            tbDeploy = new TextButton("Deploy", skin, "box_gray_5");
            tbDeploy.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelBuilder.saveAs("_editor_");
                    gameScreen.deployLevel(testLevel);
                }
            });

            tbAssign = new TextButton("Assign", skin, "box_gray_5");
            tbAssign.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {

                }
            });

            root = new Container<>();

            Table main = new Table(skin);
            root.top().right().padTop(-5).padRight(-5).setActor(main);
            main.setBackground("box_white_5");
            main.pad(5);
            main.defaults().pad(Value.percentWidth(.1f, tbDeploy)).padRight(0).padTop(0);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            float minWidth = tbDeploy.getPrefWidth();

            main.add(tbDeploy).width(minWidth);
            main.add(tbSave).width(minWidth);
            main.add(tbLoad).width(minWidth);

            Window.WindowStyle wsLoad = new Window.WindowStyle();
            wsLoad.background = skin.getDrawable("box_white_10");
            wsLoad.titleFont = skin.getFont("h3");

            loadFileDialog = new LoadFileDialog(skin, wsLoad, stage) {
                @Override
                protected void result(Object object) {
                    filenameCache = (String) object;
                    if (levelBuilder.load(filenameCache)) {
                        freeMode.optionsMenu.updateValues(levelBuilder.getLayer());
                        Components.showToast("File '" + filenameCache + "' loaded", stage);
                    } else {
                        Components.showToast("Error: Couldn't load '" + filenameCache + "'", stage);
                    }
                }
            };

            saveFileDialog = new SaveFileDialog(skin, wsLoad) {
                @Override
                protected void result(Object object) {
                    if (object == null) {
                        Components.showToast("Error: Invalid file name", stage);
                    } else {
                        filenameCache = (String) object;
                        levelBuilder.saveAs(filenameCache);
                        Components.showToast("Level '" + filenameCache + "' saved", stage);
                    }
                }
            };


            testLevel = new Level() {
                @Override
                public void initialize(GameScreen.GameScreenController gameScreenController) {
                    gameScreenController.loadLevelMap("_editor_");
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
        public Group show() {
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
            tbs.down = skin.newDrawable("box_white_5", Color.DARK_GRAY);
            tbs.font = skin.getFont("h4");

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


            btnGroup = new ButtonGroup<>(tbDraw, tbErase, tbRotate);
            btnGroup.setMaxCheckCount(1);
            btnGroup.setMinCheckCount(0);

            float minWidth = tbRotate.getPrefWidth();


            Table main = new Table(skin);
            root.center().right().padRight(-5).setActor(main);

            main.setBackground("box_white_5");
            main.pad(5);
            main.defaults().pad(Value.percentWidth(.1f,tbRotate)).padRight(0);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            main.add(tbDraw).width(minWidth).row();
            main.add(tbErase).width(minWidth).row();
            main.add(tbRotate).width(minWidth).row();

        }

        @Override
        public Group show() {
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
                lbl[i] = new Label("", skin, "h6");
                root.left().top().add(lbl[i]).left().row();
            }
        }

        public void reset() {
            for (Label lbl : lbl) {
                lbl.setText("");
            }
        }

        @Override
        public Group show() {
            return root;
        }
    }

    private class DrawMode extends Mode {
        private UIDrawPalette uiDrawPalette;

        DrawMode() {
            uiDrawPalette = new UIDrawPalette();
            setPreferencesRoot(uiDrawPalette);
        }

        @Override
        void onActivate() {
            uiInfo.reset();
            uiDrawPalette.updateTileID();
            uiDrawPalette.updateLayer();
        }

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            levelBuilder.paintAt(x, y);
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            levelBuilder.paintAt(x, y);
            return true;
        }

        private class UIDrawPalette implements UIComponent {
            private ImageButton materialButtons[];
            private UILayer uiLayer;
            private final Table root;

            private UIDrawPalette() {
                HorizontalGroup materialGroup = new HorizontalGroup();
                materialGroup.space(5);
                materialGroup.pad(0);
                materialGroup.wrap(true);
                materialGroup.wrapSpace(5);

                Drawable checked = skin.newDrawable("box_white_5", Color.GREEN);
                Drawable unchecked = skin.newDrawable("box_white_5", Color.GRAY);
                ButtonGroup<ImageButton> imgbGroup = new ButtonGroup<>();
                imgbGroup.setMinCheckCount(1);
                imgbGroup.setMaxCheckCount(1);

                final List<Integer> knownIds = TileIndex.get().getAllPlaceableIDs();

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
                    imgb.getImageCell().size(32);

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

                uiLayer = new UILayer(){
                    @Override
                    public void onLayerChange(int layer) {

                    }
                };
                root = new Table(skin);
                root.add(uiLayer.root);
                root.add(scrollPane).fill().expandX().height(Value.prefHeight.get(uiLayer.root));
                root.debug();
            }

            private void updateLayer() {
                uiLayer.updateLayer();
            }

            @Override
            public Group show() {
                uiInfo.reset();
                updateTileID();
                return root;
            }

            private void updateTileID() {
                uiInfo.lbl[0].setText("TileID: " + levelBuilder.getCurrentTileID());
            }
        }
    }

    private class EraseMode extends Mode {
        UILayer uiLayer;

        EraseMode() {
            uiLayer = new UILayer(){
                @Override
                public void onLayerChange(int layer) {

                }
            };
            setPreferencesRoot(uiLayer);
        }

        @Override
        public boolean touchDown(float x, float y, int count, int button) {
            levelBuilder.eraseAt(x, y);
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
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
            uiLayer = new UILayer() {
                @Override
                public void onLayerChange(int layer) {
                }
            };
            setPreferencesRoot(uiLayer);
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
        private OptionsMenu optionsMenu;
        float currentZoom = 1;

        FreeMode() {
            optionsMenu = new OptionsMenu();
            setPreferencesRoot(optionsMenu);
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

        private class OptionsMenu implements UIComponent {
            final HorizontalGroup root;
            private UILevelSettings gameSettings = new UILevelSettings();
            private UIMapSettings mapSettings = new UIMapSettings();

            private OptionsMenu() {

                TextButton btnLevelSettings = new TextButton("Level", skin, "box_gray_5");
                btnLevelSettings.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        prefsStack.push(gameSettings);
                    }
                });

                TextButton btnMapSettings = new TextButton("Layer", skin, "box_gray_5");
                btnMapSettings.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        mapSettings.updateValues(levelBuilder.getLayer());
                        prefsStack.push(mapSettings);
                    }
                });

//                btnLevelSettings.getLabelCell().pad(10);
//                btnMapSettings.getLabelCell().pad(10);

                root = new HorizontalGroup();
                root.space(5);
                root.wrap(true);

                root.addActor(btnLevelSettings);
                root.addActor(btnMapSettings);
            }

            public void updateValues(int layer) {
                gameSettings.updateValues();
                mapSettings.updateValues(layer);
            }

            @Override
            public Group show() {
                uiInfo.reset();
                updateCamInfo();
                return root;
            }
        }

        private class UILevelSettings implements UIComponent {
            Table root;
            Slider sldrBallSpeed, sldrLauncherCooldown, sldrLauncherSize;
            Label lblBallSpeed, lblLauncherCooldown, lblLauncherSize;
            Label lblMoves, lblLives, lblTime;
            TextField tfMoves, tfLives, tfTime;

            final int settingsPadding = 5;

            // XXX(14/4/2018): *TOO* many magic values
            private UILevelSettings() {
                root = new Table();

                Label dummy = new Label
                        (":Level Setup:", skin, "h4");
                root.padLeft(0).padRight(0).padTop(0).padBottom(0).top();
                root.add(dummy).padBottom(0).colspan(3).row();

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

                lblLives = new Label("Lives:", skin, "h4");
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
                        levelBuilder.setLives(amount);
                    }
                });

                lblMoves = new Label("Moves:", skin, "h4");

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
                        levelBuilder.setMoves(amount);
                        updateMovesPercent(amount);
                    }
                });

                lblTime = new Label("Time:", skin, "h4");

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
                        levelBuilder.setTime(amount);
                    }
                });

                sldrBallSpeed = new Slider(levelBuilder.getBallSpeed(), 20, 1, false, skin);
                lblBallSpeed = new Label(String.format(Locale.ROOT, "BallSpeed: %2d", (int) sldrBallSpeed.getValue()), skin, "h4");
                sldrBallSpeed.addListener(stopTouchDown);
                sldrBallSpeed.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblBallSpeed.setText(String.format(Locale.ROOT, "BallSpeed: %2d", (int) sldrBallSpeed.getValue()));
                        levelBuilder.setBallSpeed((int) sldrBallSpeed.getValue());
                    }
                });
                Table grpBallSpeed = createSliderGroup(lblBallSpeed, sldrBallSpeed);

                sldrLauncherCooldown = new Slider(0f, 4.8f, .16f, false, skin);
                lblLauncherCooldown = new Label(String.format(Locale.ENGLISH, "LauncherCD: %1.2f", sldrLauncherCooldown.getValue()), skin, "h4");
                sldrLauncherCooldown.addListener(stopTouchDown);
                sldrLauncherCooldown.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblLauncherCooldown.setText(String.format(Locale.ENGLISH, "LauncherCD: %1.2f", sldrLauncherCooldown.getValue()));
                        levelBuilder.setLauncherCooldown(sldrLauncherCooldown.getValue());
                    }
                });
                Table grpLauncherCD = createSliderGroup(lblLauncherCooldown, sldrLauncherCooldown);

                sldrLauncherSize = new Slider(levelBuilder.getLauncherSize(), 5, 1, false, skin);
                lblLauncherSize = new Label("LauncherSize: " + (int) sldrLauncherSize.getValue(), skin, "h4");
                sldrLauncherSize.addListener(stopTouchDown);
                sldrLauncherSize.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblLauncherSize.setText("LauncherSize: " + (int) sldrLauncherSize.getValue());
                        levelBuilder.setLauncherSize((int) sldrLauncherSize.getValue());
                    }
                });

                Table grpLauncherSize = createSliderGroup(lblLauncherSize, sldrLauncherSize);

                Table grpTextFields = new Table();
                grpTextFields.columnDefaults(0).padRight(10);
                grpTextFields.defaults().padBottom(settingsPadding);
                grpTextFields.add(lblLives).right();
                grpTextFields.add(tfLives).row();
                grpTextFields.add(lblMoves).right();
                grpTextFields.add(tfMoves).row();
                grpTextFields.add(lblTime).padBottom(0).right();
                grpTextFields.add(tfTime).padBottom(0).row();

                root.defaults().padBottom(settingsPadding);
                root.columnDefaults(0).padRight(10);

                root.add(grpTextFields).colspan(2).left().row();
                root.add(grpLauncherSize).growX();
                root.add(grpLauncherCD).growX().row();
                root.add(grpBallSpeed).growX();

            }

            void updateValues() {
                sldrLauncherSize.setValue(levelBuilder.getLauncherSize());
                sldrBallSpeed.setValue(levelBuilder.getBallSpeed());
                sldrLauncherCooldown.setValue(levelBuilder.getLauncherCooldown());

                tfLives.setText(String.valueOf(levelBuilder.getLives()));
                tfMoves.setText(String.valueOf(levelBuilder.getMoves()));
                tfTime.setText(String.valueOf(levelBuilder.getTime()));
            }

            private void updateMovesPercent(int amount) {
                float totalTiles = tilemapManager.getTotalTileCount();
                int percent = (int) ((amount / (totalTiles == 0 ? 1 : totalTiles)) * 100f);
                uiInfo.lbl[0].setText("Moves/Balls: " + percent + "%");
            }

            private Table createSliderGroup(Label label, Slider slider) {
                Table result = new Table();
                result.padBottom(5);
                result.add(label).row();
                result.add(slider).growX();
                return result;
            }

            private TextField createTextField(TextField.TextFieldListener backOnNewLineListener) {
                TextField tf = new TextField("", skin);
                tf.setAlignment(Align.center);
                tf.setMaxLength(3);
                tf.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                tf.setTextFieldListener(backOnNewLineListener);
                return tf;
            }

            @Override
            public Group show() {
                uiInfo.reset();
                String txtAmount = tfMoves.getText();
                // check to avoid parsing empty string
                int amount = txtAmount.length() > 0 ? Integer.parseInt(txtAmount) : 0;
                updateMovesPercent(amount);
                return root;
            }
        }

        private class UIMapSettings implements UIComponent {
            Container<Table> root;
            UILayer uiLayer;
            Slider sldrMinRot, sldrMaxRot, sldrColorCount;
            Label lblMinRot, lblMaxRot, lblColorCount, lblLayer;
            CheckBox cbRotateCCW;
            int activeLayer;

            UIMapSettings() {
                uiLayer = new UILayer() {
                    @Override
                    public void onLayerChange(int layer) {
                        updateValues(layer);
                    }
                };

                activeLayer = levelBuilder.getLayer();
                sldrMinRot = new Slider(0, 120, 1, false, skin);
                lblMinRot = new Label(String.format(Locale.ROOT, "Min:%3d", (int) sldrMinRot.getValue()), skin, "h4");
                sldrMinRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrMinRot.getValue();
                        lblMinRot.setText(String.format(Locale.ROOT, "Min:%3d", min));
                        levelBuilder.setMinSpeed(min);
                    }
                });
                sldrMinRot.addListener(new DragListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        compute();
                        return super.touchDown(event, x, y, pointer, button);
                    }

                    @Override
                    public void drag(InputEvent event, float x, float y, int pointer) {
                        compute();
                    }

                    private void compute() {
                        int min = (int) sldrMinRot.getValue();
                        int max = (int) sldrMaxRot.getValue();
                        if (Integer.compare(min, max) == 1) {
                            sldrMaxRot.setValue(min);
                        }
                        lblMinRot.setText(String.format(Locale.ROOT, "Min:%3d", min));
                        levelBuilder.setMinSpeed(min);

                    }
                });

                sldrMaxRot = new Slider(0, 120, 1, false, skin);
                lblMaxRot = new Label(String.format(Locale.ROOT, "Max:%3d", (int) sldrMaxRot.getValue()), skin, "h4");
                sldrMaxRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int max = (int) sldrMaxRot.getValue();
                        lblMaxRot.setText(String.format(Locale.ROOT, "Max:%3d", max));
                        levelBuilder.setMaxSpeed(max);
                    }
                });
                sldrMaxRot.addListener(new DragListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        compute();
                        return super.touchDown(event, x, y, pointer, button);
                    }

                    @Override
                    public void drag(InputEvent event, float x, float y, int pointer) {
                        compute();
                    }

                    private void compute() {
                        int min = (int) sldrMinRot.getValue();
                        int max = (int) sldrMaxRot.getValue();
                        if (Integer.compare(min, max) == 1) {
                            sldrMinRot.setValue(max);
                        }
                        lblMaxRot.setText(String.format(Locale.ROOT, "Max:%3d", max));
                        levelBuilder.setMaxSpeed(max);
                    }
                });

                sldrColorCount = new Slider(levelBuilder.getColorCount(), 8, 1, false, skin);
                lblColorCount = new Label(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()), skin, "h4");
                sldrColorCount.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblColorCount.setText(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()));
                        levelBuilder.setColorCount((int) sldrColorCount.getValue());
                    }
                });

                cbRotateCCW = ActorFactory.createCheckBox("Rotate CCW", skin);
                cbRotateCCW.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setCCWRotation(cbRotateCCW.isChecked());
                    }
                });

                Button btnUp = new Button(skin.get("default", TextButton.TextButtonStyle.class));
                btnUp.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int newLayer = levelBuilder.upLayer();
                        if (Integer.compare(newLayer, activeLayer) != 0) {
                            updateValues(newLayer);
                        }
                    }
                });

                Label slblRotation = new Label(":Rotation:", skin, "h4");

                Table rotLabels = new Table();
                rotLabels.defaults().expandX().padBottom(5);
                rotLabels.columnDefaults(0).padRight(5);
                rotLabels.add(slblRotation).colspan(2).padRight(0).padBottom(5).row();
                rotLabels.add(lblMinRot).padBottom(5);
                rotLabels.add(lblMaxRot).padBottom(5).row();
                rotLabels.add(sldrMinRot).fill();
                rotLabels.add(sldrMaxRot).fill().row();
                rotLabels.add(cbRotateCCW).colspan(2).padRight(0).left().row();

                rotLabels.add(lblColorCount).colspan(2).padRight(0).padBottom(5).row();
                rotLabels.add(sldrColorCount).colspan(2).padRight(0).fill().row();

                VerticalGroup vgroup = new VerticalGroup();
                vgroup.grow().pad(5);
                vgroup.addActor(rotLabels);

                Table main = new Table();
                main.add(uiLayer.root);
                main.add(vgroup).grow();

                root = new Container<>(main);
                root.fill();
            }

            private void updateValues(int layer) {
                activeLayer = layer;
                uiLayer.updateLayer();
                sldrMaxRot.setValue(levelBuilder.getMaxSpeed());
                sldrMinRot.setValue(levelBuilder.getMinSpeed());
                sldrColorCount.setValue(levelBuilder.getColorCount());
                cbRotateCCW.setChecked(levelBuilder.isCCWRotationEnabled());
            }

            @Override
            public Group show() {
                return root;
            }
        }

    }

    private abstract class Mode extends GestureDetector.GestureAdapter {
        private UIComponent preferences;

        public void activate() {
            activeMode = this;

            prefsStack.setRoot(preferences);
            onActivate();
        }

        void setPreferencesRoot(UIComponent preferences) {
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
