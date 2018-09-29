package com.archapp.coresmash.levelbuilder;

import com.archapp.coresmash.Coords2D;
import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.GameController;
import com.archapp.coresmash.PersistentString;
import com.archapp.coresmash.levels.Level;
import com.archapp.coresmash.managers.RenderManager;
import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.screens.GameScreen;
import com.archapp.coresmash.screens.ScreenBase;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tiles.TileType;
import com.archapp.coresmash.ui.AssignLevelDialog;
import com.archapp.coresmash.ui.Components;
import com.archapp.coresmash.ui.LoadFileDialog;
import com.archapp.coresmash.ui.SaveFileDialog;
import com.archapp.coresmash.ui.StageInputCapture;
import com.archapp.coresmash.ui.UIComponent;
import com.archapp.coresmash.ui.UIComponentStack;
import com.archapp.coresmash.ui.UIFactory;
import com.archapp.coresmash.ui.UIUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.List;
import java.util.Locale;

public class LevelBuilderScreen extends ScreenBase {
    private ExtendViewport viewport;
    private OrthographicCamera camera;
    private RenderManager renderManager;

    private GameScreen gameScreen; // XXX:TODO: GET THIS SHIT OUT OF HERE Q.Q

    private LevelBuilder levelBuilder;
    private Stage stage;
    private Skin skin;

    private UIComponentStack prefsStack;
    private UIToolbarTop uiToolbarTop;
    private UITools uiTools;
    private UIInfo uiInfo;

    private FreeMode freeMode;
    private DrawMode drawMode;
    private EraseMode eraseMode;
    private RotateMode rotateMode;
    private Mode activeMode;

    private GestureDetector worldInputHandler;

    public LevelBuilderScreen(CoreSmash game) {
        super(game);
        gameScreen = new GameScreen(game);

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1080, 1920, camera);
        camera.position.set(viewport.getMinWorldWidth() / 2, viewport.getMinWorldHeight() / 2, 0);
        camera.update();

        renderManager = game.getRenderManager();

        levelBuilder = new LevelBuilder(camera);
        levelBuilder.load("_editor_"); // Fails silently if it doesn't exist
        stage = setupStage();

        drawMode = new DrawMode();
        eraseMode = new EraseMode();
        rotateMode = new RotateMode();
        freeMode = new FreeMode();
        freeMode.activate();

        worldInputHandler = new GestureDetector(new LevelBuilderGestureListner());

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(worldInputHandler);
        screenInputMultiplexer.addProcessor(new InputAdapter() {
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
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiToolbarTop.saveFileDialog.hide(null);
        uiToolbarTop.loadFileDialog.hide(null);
    }

    @Override
    public void render(float delta) {
        draw();
        stage.act();
        stage.draw();
    }

    public void draw() {
        levelBuilder.draw(renderManager);
    }

    private Stage setupStage() {
        Stage stage = new Stage(gameInstance.getUIViewport());
        skin = gameInstance.getSkin();
        uiTools = new UITools();
        uiInfo = new UIInfo();
        uiToolbarTop = new UIToolbarTop();

        Drawable frame = skin.getDrawable("EditorBigFrame");

        prefsStack = new UIComponentStack();

        Container<Group> stackContainer = new Container<>(prefsStack.getRoot());
        stackContainer.setBackground(frame);
        stackContainer.fill();
        stackContainer.padTop(frame.getTopHeight() / 4).padLeft(frame.getTopHeight() / 4).padRight(frame.getTopHeight() / 4);


        Stack mainStack = new Stack();
        mainStack.setFillParent(true);

        Table prefs = new Table();
//        prefsStack.setMaxHeight(Value.percentHeight(.25f, prefs));
        prefs.center().bottom()
                .add(stackContainer)
                .growX()
                .padBottom(-frame.getBottomHeight());

        mainStack.addActor(uiTools.getRoot());
        mainStack.addActor(uiToolbarTop.getRoot());
        mainStack.addActor(prefs);
        mainStack.addActor(uiInfo.getRoot());

        stage.addActor(mainStack);
        return stage;
    }

    public void saveProgress() {
        levelBuilder.saveAs("_editor_");
    }

    private abstract class UILayer implements UIComponent {
        Label lblLayer;
        Table root;

        UILayer() {
            root = new Table();
            Button btnUp = new Button(skin.get("default", TextButton.TextButtonStyle.class));
            btnUp.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelBuilder.upLayer();
                    updateLayer();
                    onLayerChange(levelBuilder.getLayer());
                }
            });

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


            root.add(btnUp).size(Value.percentHeight(1.1f, lblLayer)).padBottom(Value.prefHeight.get(lblLayer)).row();
            root.add(lblLayer).padBottom(Value.prefHeight.get(lblLayer)).row();
            root.add(btnDown).size(Value.percentHeight(1.1f, lblLayer));
            root.center().pad(Value.percentHeight(.2f, lblLayer));
        }

        private void updateLayer() {
            lblLayer.setText(String.valueOf(levelBuilder.getLayer()));
        }

        public abstract void onLayerChange(int layer);

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIToolbarTop implements UIComponent {
        private Container<Table> root;
        private final AssignLevelDialog assignLevelDialog;
        private final SaveFileDialog saveFileDialog;
        private final LoadFileDialog loadFileDialog;
        private Level testLevel;
        private final TextButton tbSave, tbLoad, tbDeploy, tbAssign;
        private PersistentString filenameCache;

        UIToolbarTop() {
            filenameCache = new PersistentString("inner_build", "levelbuilder_map_name");

            tbSave = UIFactory.createTextButton("Save", skin, "levelBuilderButton");
            tbSave.getLabelCell().pad(Value.percentHeight(.5f, tbSave.getLabel()));
            tbSave.getLabelCell().padTop(Value.percentHeight(.25f, tbSave.getLabel()));
            tbSave.getLabelCell().padBottom(Value.percentHeight(.25f, tbSave.getLabel()));
            tbSave.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    int one = levelBuilder.getTargetScoreOne();
                    int two = levelBuilder.getTargetScoreTwo();
                    int three = levelBuilder.getTargetScoreThree();
                    if (three <= two) {
                        Components.showToast("ERROR: TargetScoreThree isn't greater than TargetScoreTwo!", stage, 3);
                        return;
                    } else if (two <= one) {
                        Components.showToast("ERROR: TargetScoreTwo isn't greater than TargetScoreOne!", stage, 3);
                        return;
                    }

                    saveFileDialog.show(stage, filenameCache.getValue());
                }
            });
            tbLoad = UIFactory.createTextButton("Load", skin, "levelBuilderButton");
            tbLoad.getLabelCell().pad(Value.percentHeight(.5f, tbLoad.getLabel()));
            tbLoad.getLabelCell().padTop(Value.percentHeight(.25f, tbLoad.getLabel()));
            tbLoad.getLabelCell().padBottom(Value.percentHeight(.25f, tbLoad.getLabel()));
            tbLoad.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    loadFileDialog.show(stage);
                }
            });

            tbDeploy = UIFactory.createTextButton("Deploy", skin, "levelBuilderButton");
            tbDeploy.getLabelCell().pad(Value.percentHeight(.5f, tbDeploy.getLabel()));
            tbDeploy.getLabelCell().padTop(Value.percentHeight(.25f, tbDeploy.getLabel()));
            tbDeploy.getLabelCell().padBottom(Value.percentHeight(.25f, tbDeploy.getLabel()));
            tbDeploy.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (levelBuilder.saveAs("_editor_")) {
                        gameScreen.deployLevel(testLevel);
                    } else {
                        Components.showToast("[Error] Could not deploy level", stage);
                    }
                }
            });

            tbAssign = UIFactory.createTextButton("Assign", skin, "levelBuilderButton");
            tbAssign.getLabelCell().pad(Value.percentHeight(.5f, tbAssign.getLabel()));
            tbAssign.getLabelCell().padTop(Value.percentHeight(.25f, tbAssign.getLabel()));
            tbAssign.getLabelCell().padBottom(Value.percentHeight(.25f, tbAssign.getLabel()));
            tbAssign.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    assignLevelDialog.show(stage);
                }
            });

            root = new Container<>();

            Drawable frame = skin.getDrawable("EditorBigFrame");

            Table main = new Table(skin);
            root.top().right()
                    .padTop(-frame.getTopHeight())
                    .setActor(main);
            main.setBackground(frame);
            main.pad(frame.getTopHeight() / 2).
                    padTop(frame.getTopHeight() * 5 / 4);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            float minWidth = tbDeploy.getPrefWidth();

            main.row().padRight(Value.percentHeight(.5f, tbDeploy)).width(minWidth);
            main.add(tbDeploy);
            main.add(tbSave);
            main.add(tbLoad);
            main.add(tbAssign).padRight(0);

            loadFileDialog = new LoadFileDialog(skin) {
                @Override
                protected void result(Object object) {
                    filenameCache.setValue((String) object);

                    if (levelBuilder.load(filenameCache.getValue())) {
                        freeMode.activate();
                        Components.showToast("File '" + filenameCache.getValue() + "' loaded", stage);
                    } else {
                        Components.showToast("Error: Couldn't load '" + filenameCache.getValue() + "'", stage);
                    }
                }
            };

            saveFileDialog = new SaveFileDialog(skin) {
                @Override
                protected void result(Object object) {
                    if (object == null) {
                        Components.showToast("Error: Invalid file name", stage);
                    } else {
                        filenameCache.setValue((String) object);
                        if (levelBuilder.saveAs(filenameCache.getValue()))
                            Components.showToast("Level '" + filenameCache.getValue() + "' saved", stage);
                        else
                            Components.showToast("[Error] Invalid level", stage);
                    }
                }
            };

            assignLevelDialog = new AssignLevelDialog(skin);

            testLevel = new Level() {
                @Override
                public void initialize(GameController gameScreenController) {
                    gameScreenController.loadLevelMap("_editor_", LevelListParser.Source.EXTERNAL);
                    RoundManager stats = gameScreenController.getBehaviourPack().roundManager;
                    stats.debug();
                    for (TileType.PowerupType type : TileType.PowerupType.values()) {
                        stats.enablePowerup(type, 10);
                    }
                }

                @Override
                public void update(float delta, TilemapManager tilemapManager) {
                }

                @Override
                public void end(RoundManager.GameStats stats) {
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

            final TextButton tbDraw, tbErase;

            tbDraw = UIFactory.createTextButton("Draw", skin, "levelBuilderButtonChecked");
            tbDraw.getLabelCell().padTop(Value.percentHeight(.25f, tbDraw.getLabel()));
            tbDraw.getLabelCell().padBottom(Value.percentHeight(.25f, tbDraw.getLabel()));
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
            tbErase = UIFactory.createTextButton("Erase", skin, "levelBuilderButtonChecked");
            tbErase.getLabelCell().padTop(Value.percentHeight(.25f, tbErase.getLabel()));
            tbErase.getLabelCell().padBottom(Value.percentHeight(.25f, tbErase.getLabel()));
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

//            tbRotate = UIFactory.createTextButton("Rotate", skin, "levelBuilderButtonChecked");
//            tbRotate.getLabelCell().pad(Value.percentHeight(.5f, tbRotate.getLabel()));
//            tbRotate.getLabelCell().padTop(Value.percentHeight(.25f, tbRotate.getLabel()));
//            tbRotate.getLabelCell().padBottom(Value.percentHeight(.25f, tbRotate.getLabel()));
//            tbRotate.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    if (tbRotate.isChecked()) {
//                        rotateMode.activate();
//                    } else {
//                        freeMode.activate();
//                    }
//                }
//            });


            btnGroup = new ButtonGroup<>(tbDraw, tbErase);
            btnGroup.setMaxCheckCount(1);
            btnGroup.setMinCheckCount(0);


            Drawable frame = skin.getDrawable("EditorBigFrame");

            Table main = new Table(skin);
            main.setBackground(frame);
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

            main.pad(frame.getTopHeight() / 3).padRight(frame.getRightWidth());
            main.defaults().width(tbErase.getWidth()).pad(5 * Gdx.graphics.getDensity());
            main.add(tbDraw).row();
            main.add(tbErase).padTop(0).row();
            root.center().right().padRight(-frame.getRightWidth()).setActor(main);

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
            root.setTouchable(Touchable.disabled);
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
        public Group getRoot() {
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
                root = new Table(skin);

                HorizontalGroup materialGroup = new HorizontalGroup();
                materialGroup.space(5);
                materialGroup.pad(0);
                materialGroup.wrap(true);
                materialGroup.wrapSpace(5);

                Drawable checked = skin.newDrawable("boxSmall", Color.GREEN);
                Drawable unchecked = skin.newDrawable("boxSmall", Color.GRAY);
                ButtonGroup<ImageButton> imgbGroup = new ButtonGroup<>();
                imgbGroup.setMinCheckCount(1);
                imgbGroup.setMaxCheckCount(1);

                final List<TileType> placeables = TileType.getAllPlaceables();

                materialButtons = new ImageButton[placeables.size()];
                int buttonIndex = 0;
                float ballSize = skin.get("h2", Label.LabelStyle.class).font.getLineHeight();
                for (int i = 0; i < placeables.size(); ++i) {
                    ImageButton.ImageButtonStyle imgbs;
                    imgbs = new ImageButton.ImageButtonStyle();
                    imgbs.imageUp = new TextureRegionDrawable(new TextureRegion(renderManager.getTextureFor(placeables.get(i).getID())));
                    imgbs.checked = checked;
                    imgbs.up = unchecked;

                    ImageButton imgb = new ImageButton(imgbs);
                    materialGroup.addActor(imgb);
                    imgb.getImage().setScaling(Scaling.fill);
                    imgb.getImageCell().size(ballSize);

                    final int finalButtonIndex = buttonIndex;
                    imgb.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            if (materialButtons[finalButtonIndex].isChecked()) {
                                levelBuilder.setTileType(placeables.get(finalButtonIndex));
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

                uiLayer = new UILayer() {
                    @Override
                    public void onLayerChange(int layer) {

                    }
                };
                root.add(uiLayer.root);
                root.add(scrollPane).fill().expandX();
            }

            private void updateLayer() {
                uiLayer.updateLayer();
            }

            @Override
            public Group getRoot() {
                uiInfo.reset();
                updateTileID();
                return root;
            }

            private void updateTileID() {
                uiInfo.lbl[0].setText("TileID: " + levelBuilder.getCurrentTileType().getID());
            }
        }
    }

    private class EraseMode extends Mode {
        UILayer uiLayer;

        EraseMode() {
            uiLayer = new UILayer() {
                @Override
                public void onLayerChange(int layer) {

                }
            };
            final Container<Group> container = new Container<>(uiLayer.getRoot());
            container.top().left();
            setPreferencesRoot(new UIComponent() {
                @Override
                public Group getRoot() {
                    return container;
                }
            });
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
            final Container<Group> container = new Container<>(uiLayer.getRoot());
            container.top().left();
            setPreferencesRoot(new UIComponent() {
                @Override
                public Group getRoot() {
                    return container;
                }
            });
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            int layer = levelBuilder.getLayer();
            tmPos.set(
                    (int) levelBuilder.getPositionX(layer),
                    (int) levelBuilder.getPositionY(layer)
            );

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
        private float currentZoom = 1;
        private boolean isMovingOffset;
        private StageInputCapture textfieldInputCapture;

        FreeMode() {
            textfieldInputCapture = new StageInputCapture();
            textfieldInputCapture.setInputListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    Gdx.input.setOnscreenKeyboardVisible(false);
                    stage.unfocusAll();
                    textfieldInputCapture.stop();
                    return false;
                }
            });
            optionsMenu = new OptionsMenu();
            setPreferencesRoot(optionsMenu);
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            float multiplier = currentZoom;
            if (isMovingOffset) {
                levelBuilder.moveOffsetBy(deltaX * multiplier, -deltaY * multiplier);
                optionsMenu.mapSettings.updatePositionValues();
            } else {
                camera.position.add(-deltaX * multiplier, deltaY * multiplier, 0);
                updateCamInfo();
                camera.update();
            }
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
            private final HorizontalGroup root;
            private UILevelSettings gameSettings = new UILevelSettings();
            private UIMapSettings mapSettings = new UIMapSettings();

            private OptionsMenu() {
                TextButton btnLevelSettings = UIFactory.createTextButton("Level", skin, "levelBuilderButton");
                btnLevelSettings.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        prefsStack.push(gameSettings);
                        gameSettings.updateValues();
                    }
                });

                TextButton btnMapSettings = UIFactory.createTextButton("Layer", skin, "levelBuilderButton");
                btnMapSettings.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        mapSettings.updateValues(levelBuilder.getLayer());
                        prefsStack.push(mapSettings);
                    }
                });


                btnLevelSettings.pad(Value.percentHeight(1, btnLevelSettings.getLabel()));
                btnLevelSettings.padTop(Value.percentHeight(.5f, btnLevelSettings.getLabel()));
                btnLevelSettings.padBottom(Value.percentHeight(.5f, btnLevelSettings.getLabel()));

                btnMapSettings.pad(Value.percentHeight(1, btnMapSettings.getLabel()));
                btnMapSettings.padTop(Value.percentHeight(.5f, btnMapSettings.getLabel()));
                btnMapSettings.padBottom(Value.percentHeight(.5f, btnMapSettings.getLabel()));

                root = new HorizontalGroup();
                root.pad(5 * Gdx.graphics.getDensity());
                root.addActor(btnLevelSettings);
                root.addActor(btnMapSettings);
                root.space(0.5f * btnLevelSettings.getLabel().getMinHeight());
                root.wrap(true);

            }

            public void updateValues(int layer) {
                gameSettings.updateValues();
                mapSettings.updateValues(layer);
            }

            @Override
            public Group getRoot() {
                uiInfo.reset();
                updateCamInfo();
                return root;
            }
        }

        private class UILevelSettings implements UIComponent {
            Table root;
            Slider sldrBallSpeed, sldrLauncherCooldown, sldrLauncherSize;
            Label lblBallSpeed, lblLauncherCooldown, lblLauncherSize;
            StringBuilder stringBuilder;
            TextField tfMoves, tfLives, tfTime,
                    tfTargetScoreOne, tfTargetScoreTwo, tfTargetScoreThree;

            // XXX(14/4/2018): *TOO* many magic values
            private UILevelSettings() {
                InputListener stopTouchDown = new InputListener() {
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        event.stop();
                        return false;
                    }
                };

                final TextField.TextFieldListener returnOnNewLineListener = new TextField.TextFieldListener() {
                    public void keyTyped(TextField textField, char key) {
                        if (key == '\n' || key == '\r') {
                            if (textField.getText().length() == 0) {
                                textField.setText("0");
                            }
                            textField.getOnscreenKeyboard().show(false);
                            stage.unfocusAll();
                            textfieldInputCapture.stop();
                        }
                    }
                };

                stringBuilder = new StringBuilder();

                tfLives = createTextField(returnOnNewLineListener);
                tfLives.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setLives(tfLives.getText().isEmpty() ?
                                0 : Integer.parseInt(tfLives.getText()));
                    }
                });


                tfMoves = createTextField(returnOnNewLineListener);
                tfMoves.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int amount = tfMoves.getText().isEmpty() ?
                                0 : Integer.parseInt(tfMoves.getText());
                        levelBuilder.setMoves(amount);
                        updateUIInfo();
                    }
                });


                tfTime = createTextField(returnOnNewLineListener);
                tfTime.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setTime(tfTime.getText().isEmpty() ?
                                0 : Integer.parseInt(tfTime.getText()));
                    }
                });

                tfTargetScoreOne = createTextField(returnOnNewLineListener);
                tfTargetScoreOne.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setTargetScoreOne(tfTargetScoreOne.getText().isEmpty() ?
                                0 : Integer.parseInt(tfTargetScoreOne.getText()));
                    }
                });

                tfTargetScoreTwo = createTextField(returnOnNewLineListener);
                tfTargetScoreTwo.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setTargetScoreTwo(tfTargetScoreTwo.getText().isEmpty() ?
                                0 : Integer.parseInt(tfTargetScoreTwo.getText()));
                    }
                });

                tfTargetScoreThree = createTextField(returnOnNewLineListener);
                tfTargetScoreThree.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setTargetScoreThree(tfTargetScoreThree.getText().isEmpty() ?
                                0 : Integer.parseInt(tfTargetScoreThree.getText()));
                    }
                });

                sldrBallSpeed = new Slider(5, 20, 1, false, skin);
                lblBallSpeed = new Label(String.format(Locale.ROOT, "BallSpeed: %2d", (int) sldrBallSpeed.getValue()), skin, "h5");
                sldrBallSpeed.addListener(stopTouchDown);
                sldrBallSpeed.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblBallSpeed.setText(String.format(Locale.ROOT, "BallSpeed: %2d", (int) sldrBallSpeed.getValue()));
                        levelBuilder.setBallSpeed((int) sldrBallSpeed.getValue());
                    }
                });
                Table grpBallSpeed = createSliderGroup(lblBallSpeed, sldrBallSpeed);

                sldrLauncherCooldown = new Slider(.16f, 3.2f, .04f, false, skin);
                lblLauncherCooldown = new Label(String.format(Locale.ENGLISH, "LauncherCD: %1.2f", sldrLauncherCooldown.getValue()), skin, "h5");
                sldrLauncherCooldown.addListener(stopTouchDown);
                sldrLauncherCooldown.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblLauncherCooldown.setText(String.format(Locale.ENGLISH, "LauncherCD: %1.2f", sldrLauncherCooldown.getValue()));
                        levelBuilder.setLauncherCooldown(sldrLauncherCooldown.getValue());
                    }
                });
                Table grpLauncherCD = createSliderGroup(lblLauncherCooldown, sldrLauncherCooldown);

                sldrLauncherSize = new Slider(1, 5, 1, false, skin);
                lblLauncherSize = new Label("LauncherSize: " + (int) sldrLauncherSize.getValue(), skin, "h5");
                sldrLauncherSize.addListener(stopTouchDown);
                sldrLauncherSize.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblLauncherSize.setText("LauncherSize: " + (int) sldrLauncherSize.getValue());
                        levelBuilder.setLauncherSize((int) sldrLauncherSize.getValue());
                    }
                });

                Table grpLauncherSize = createSliderGroup(lblLauncherSize, sldrLauncherSize);
                GlyphLayout gLayout = new GlyphLayout(skin.getFont("h4"), " 999 ");

                Table grpTextFields = new Table();
                grpTextFields.add(new Label("Lives:", skin, "h5")).padRight(Value.percentHeight(.25f, tfMoves)).right();
                grpTextFields.add(tfLives).width(gLayout.width).padRight(Value.percentHeight(.5f, tfMoves));
                grpTextFields.add(new Label("Moves:", skin, "h5")).padRight(Value.percentHeight(.25f, tfMoves)).right();
                grpTextFields.add(tfMoves).width(gLayout.width).padRight(Value.percentHeight(.5f, tfMoves));
                grpTextFields.add(new Label("Time:", skin, "h5")).padRight(Value.percentHeight(.25f, tfMoves)).right();
                grpTextFields.add(tfTime).width(gLayout.width).row();

                Table grpTargetScoreFields = new Table(skin);
                grpTargetScoreFields.defaults().padBottom(tfTargetScoreOne.getPrefHeight() * .1f);
                grpTargetScoreFields.add("Target Score One:", "h5").padRight(Value.percentHeight(.25f, tfMoves)).right();
                grpTargetScoreFields.add(tfTargetScoreOne).growX().row();
                grpTargetScoreFields.add("Target Score Two:", "h5").padRight(Value.percentHeight(.25f, tfMoves)).right();
                grpTargetScoreFields.add(tfTargetScoreTwo).growX().row();
                grpTargetScoreFields.add("Target Score Three:", "h5").padRight(Value.percentHeight(.25f, tfMoves)).right();
                grpTargetScoreFields.add(tfTargetScoreThree).growX().row();

                root = new Table();

                root.pad(0).top();
                root.add(new Label(":Level Setup:", skin, "h4")).padBottom(Value.percentHeight(.5f, lblBallSpeed)).colspan(2).row();

                root.defaults()
                        .padBottom(Value.percentHeight(.5f, sldrBallSpeed))
                        .padLeft(Value.percentHeight(.5f, sldrBallSpeed))
                        .padRight(Value.percentHeight(.5f, sldrBallSpeed));
                root.columnDefaults(0).padRight(Value.percentHeight(.25f, sldrBallSpeed));

                root.add(grpTextFields).colspan(2).row();
                root.add(grpLauncherSize).growX();
                root.add(grpLauncherCD).growX().row();
                root.add(grpBallSpeed).growX().row();
                root.add(grpTargetScoreFields).colspan(2).growX();

                updateValues();
            }

            void updateValues() {
                sldrLauncherSize.setValue(levelBuilder.getLauncherSize());
                sldrBallSpeed.setValue(levelBuilder.getBallSpeed());
                sldrLauncherCooldown.setValue(levelBuilder.getLauncherCooldown());

                tfLives.setText(String.valueOf(levelBuilder.getLives()));
                tfMoves.setText(String.valueOf(levelBuilder.getMoves()));
                tfTime.setText(String.valueOf(levelBuilder.getTime()));

                tfTargetScoreOne.setText(String.valueOf(levelBuilder.getTargetScoreOne()));
                tfTargetScoreTwo.setText(String.valueOf(levelBuilder.getTargetScoreTwo()));
                tfTargetScoreThree.setText(String.valueOf(levelBuilder.getTargetScoreThree()));

                updateUIInfo();
            }

            private void updateUIInfo() {
                int totalBalls = levelBuilder.getTotalTileCount();
                int moves = levelBuilder.getMoves();

                int percent = (int) ((moves / (totalBalls == 0 ? 1f : (float) totalBalls)) * 100f);

                int totalBallsTimesTen = totalBalls * 10;
                int movesTimesTen = moves * 10;
                int totalBallsAndMovesTimesTen = totalBallsTimesTen + movesTimesTen;
                int totalBallsAndMovesAverage = (totalBallsTimesTen + movesTimesTen) / 2;
                int halfBallsAndMovesAverage = totalBallsAndMovesAverage / 2;

                float multiplier = 1.22f;

                int min = (int) (totalBallsAndMovesAverage * multiplier);
                int lower = (int) (min * multiplier);
                int mid = (int) (lower * multiplier);
                int high = (int) (mid * multiplier);
                int higher = (int) (high * multiplier);
                int max = (int) (higher * multiplier);

                float sub1 = .75f, sub2 = .5f;

                stringBuilder.setLength(0);
                uiInfo.lbl[1].setText(String.format(Locale.getDefault(),
                        stringBuilder
                                .append("TB: ").append(totalBalls).append("\n")
                                .append("M/TB: ").append(percent).append("%%\n")
                                .append("(A)TBx10: ").append(totalBallsTimesTen).append("\n")
                                .append("(B)Mx10: ").append(movesTimesTen).append("\n")
                                .append("(T)A+B: ").append(totalBallsAndMovesTimesTen).append("\n")
                                .append("(C)(A+B)/2: ").append(totalBallsAndMovesAverage).append("\n")
                                .append("(D) C/2: ").append(halfBallsAndMovesAverage).append("\n")
                                .append("Min:   %4d")
                                .append(" *.75f: %4d")
                                .append(" *.5f: %4d")
                                .append("\n")
                                .append("Lower: %4d")
                                .append(" *.75f: %4d")
                                .append(" *.5f: %4d")
                                .append("\n")
                                .append("Mid:   %4d")
                                .append(" *.75f: %4d")
                                .append(" *.5f: %4d")
                                .append("\n")
                                .append("High:   %4d")
                                .append(" *.75f: %4d")
                                .append(" *.5f: %4d")
                                .append("\n")
                                .append("Higher:  %4d")
                                .append(" *.75f: %4d")
                                .append(" *.5f: %4d")
                                .append("\n")
                                .append("Max:   %4d")
                                .append(" *.75f: %4d")
                                .append(" *.5f: %4d")
                                .append("\n")
                                .toString(),
                        min, (int) (min * sub1), (int) (min * sub2),
                        lower, (int) (lower * sub1), (int) (lower * sub2),
                        mid, (int) (mid * sub1), (int) (mid * sub2),
                        high, (int) (high * sub1), (int) (high * sub2),
                        higher, (int) (higher * sub1), (int) (higher * sub2),
                        max, (int) (max * sub1), (int) (max * sub2)));
            }

            private Table createSliderGroup(Label label, Slider slider) {
                Table result = new Table();
                result.padBottom(5);
                result.add(label).row();
                result.add(slider).growX();
                return result;
            }

            private TextField createTextField(TextField.TextFieldListener backOnNewLineListener) {
                final TextField tf = new TextField("", skin);
                tf.setAlignment(Align.center);
                tf.setMaxLength(7);
                tf.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                tf.setTextFieldListener(backOnNewLineListener);
                tf.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        tf.setCursorPosition(tf.getText().length());
                        textfieldInputCapture.capture(stage);
                        if (tf.getText().equals("0"))
                            tf.setText("");

                        event.stop();
                        return true;
                    }
                });
                tf.addListener(new FocusListener() {
                    @Override
                    public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                        if (!focused) {
                            if (tf.getText().length() == 0)
                                tf.setText("0");
                        }
                    }
                });
                return tf;
            }


            @Override
            public Group getRoot() {
                updateUIInfo();
                return root;
            }
        }

        private class UIMapSettings implements UIComponent {
            Container<Table> root;
            StageInputCapture moveInputCapture;
            UILayer uiLayer;
            TextField tfCircleX, tfCircleY, tfCrossX, tfCrossY;
            Slider sldrCrossMinRot, sldrCrossMaxRot, sldrColorCount, sldrCircleMinRot, sldrCircleMaxRot;
            Label lblCrossMinRot, lblCrossMaxRot, lblColorCount, lblCircleMinRot, lblCircleMaxRot;
            CheckBox cbRotateCCW, cbIsChained;
            int activeLayer;
            boolean settingInitialValuesFlag;

            UIMapSettings() {
                uiLayer = new UILayer() {
                    @Override
                    public void onLayerChange(int layer) {
                        updateValues(layer);
                    }
                };
                moveInputCapture = new StageInputCapture();

                InputListener stopTouchDown = new InputListener() {
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        event.stop();
                        return false;
                    }
                };

                activeLayer = levelBuilder.getLayer();
                sldrCrossMinRot = new Slider(0, 150, 1, false, skin);
                lblCrossMinRot = new Label(String.format(Locale.ROOT, "Min:%3d", (int) sldrCrossMinRot.getValue()), skin, "h5");
                sldrCrossMinRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrCrossMinRot.getValue();
                        int max = (int) sldrCrossMaxRot.getValue();
                        if (!settingInitialValuesFlag) {
                            if (Integer.compare(min, max) > 0) {
                                sldrCrossMaxRot.setValue(min);
                            }
                            levelBuilder.setCrossMinSpeed(min);
                        }
                        lblCrossMinRot.setText(String.format(Locale.ROOT, "Min:%3d", (int) sldrCrossMinRot.getValue()));
                    }
                });
                sldrCrossMinRot.addListener(stopTouchDown);

                sldrCrossMaxRot = new Slider(0, 150, 1, false, skin);
                lblCrossMaxRot = new Label(String.format(Locale.ROOT, "Max:%3d", (int) sldrCrossMaxRot.getValue()), skin, "h5");
                sldrCrossMaxRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrCrossMinRot.getValue();
                        int max = (int) sldrCrossMaxRot.getValue();
                        if (!settingInitialValuesFlag) {
                            if (Integer.compare(min, max) > 0) {
                                sldrCrossMinRot.setValue(max);
                            }
                            levelBuilder.setCrossMaxSpeed(max);
                        }
                        lblCrossMaxRot.setText(String.format(Locale.ROOT, "Max:%3d", (int) sldrCrossMaxRot.getValue()));
                    }
                });
                sldrCrossMaxRot.addListener(stopTouchDown);

                sldrCircleMinRot = new Slider(0, 150, 1, false, skin);
                lblCircleMinRot = new Label(String.format(Locale.ROOT, "Min:%3d", (int) sldrCircleMinRot.getValue()), skin, "h5");
                sldrCircleMinRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrCircleMinRot.getValue();
                        int max = (int) sldrCircleMaxRot.getValue();
                        if (!settingInitialValuesFlag) {
                            if (Integer.compare(min, max) == 1) {
                                sldrCircleMaxRot.setValue(min);
                            }
                            levelBuilder.setOriginMinSpeed(min);
                        }
                        lblCircleMinRot.setText(String.format(Locale.ROOT, "Min:%3d", (int) sldrCircleMinRot.getValue()));
                    }
                });
                sldrCircleMinRot.addListener(stopTouchDown);

                sldrCircleMaxRot = new Slider(0, 150, 1, false, skin);
                lblCircleMaxRot = new Label(String.format(Locale.ROOT, "Max:%3d", (int) sldrCircleMaxRot.getValue()), skin, "h5");
                sldrCircleMaxRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrCircleMinRot.getValue();
                        int max = (int) sldrCircleMaxRot.getValue();
                        if (!settingInitialValuesFlag) {
                            if (Integer.compare(min, max) == 1) {
                                sldrCircleMinRot.setValue(max);
                            }
                            levelBuilder.setOriginMaxSpeed(max);
                        }
                        lblCircleMaxRot.setText(String.format(Locale.ROOT, "Max:%3d", (int) sldrCircleMaxRot.getValue()));
                    }
                });
                sldrCircleMaxRot.addListener(stopTouchDown);

                sldrColorCount = new Slider(1, 8, 1, false, skin);
                lblColorCount = new Label(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()), skin, "h5");
                sldrColorCount.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblColorCount.setText(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()));
                        levelBuilder.setColorCount((int) sldrColorCount.getValue());
                    }
                });
                sldrColorCount.addListener(stopTouchDown);

                cbRotateCCW = UIFactory.createCheckBox("Rotate CCW", skin);
                cbRotateCCW.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setCCWRotation(cbRotateCCW.isChecked());
                    }
                });

                cbIsChained = UIFactory.createCheckBox("Chained", skin);
                cbIsChained.setChecked(true);
                cbIsChained.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setChained(cbIsChained.isChecked());
                    }
                });

                Table positionSettings = createPositionGroup();

                float labelPercent = .15f;

                Table rotLabels = new Table();
                rotLabels.defaults().expandX().padBottom(Value.percentHeight(.5f, sldrCrossMinRot));
                rotLabels.columnDefaults(0).padRight(Value.percentHeight(.5f, sldrCrossMinRot));
                rotLabels.add(new Label(":Position:", skin, "h5")).colspan(2).padRight(0).padBottom(Value.percentHeight(.5f, sldrCrossMinRot)).row();
                rotLabels.add(positionSettings).colspan(2).padBottom(Value.percentHeight(.5f, sldrCrossMinRot)).padRight(0).row();
                rotLabels.add(new Label(":Cross Rotation:", skin, "h5")).colspan(2).padRight(0).padBottom(0).row();
                rotLabels.row().padBottom(Value.percentHeight(labelPercent, sldrCrossMinRot));
                rotLabels.add(lblCrossMinRot);
                rotLabels.add(lblCrossMaxRot).row();
                rotLabels.add(sldrCrossMinRot).fill();
                rotLabels.add(sldrCrossMaxRot).fill().row();

                rotLabels.add(new Label(":Circle Rotation:", skin, "h5")).colspan(2).padRight(0).padBottom(0).row();
                rotLabels.row().padBottom(Value.percentHeight(labelPercent, sldrCircleMinRot));
                rotLabels.add(lblCircleMinRot);
                rotLabels.add(lblCircleMaxRot).row();
                rotLabels.add(sldrCircleMinRot).fill();
                rotLabels.add(sldrCircleMaxRot).fill().row();

                rotLabels.add(cbRotateCCW).left().padBottom(0);
                rotLabels.add(cbIsChained).right().padRight(0).padBottom(0).row();

                rotLabels.add(lblColorCount).colspan(2).padRight(0).padBottom(Value.percentHeight(labelPercent, sldrCrossMinRot)).row();
                rotLabels.add(sldrColorCount).colspan(2).padRight(0).fill().row();

                VerticalGroup vgroup = new VerticalGroup();
                vgroup.grow();
                vgroup.addActor(rotLabels);

                ScrollPane scrollPane = new ScrollPane(vgroup);
                scrollPane.setOverscroll(false, false);
                scrollPane.setSmoothScrolling(false);
                scrollPane.setScrollingDisabled(true, false);
                scrollPane.setCancelTouchFocus(false);

                Table main = new Table();
                main.add(uiLayer.root);
                main.add(scrollPane).grow()
                        .maxHeight(Value.percentHeight(.35f, UIUtils.getScreenActor(scrollPane)))
                        .padLeft(Value.percentHeight(.5f, lblCrossMinRot))
                        .padRight(Value.percentHeight(.5f, lblCrossMinRot));

                root = new Container<>(main);
                root.fill();
            }

            private void updateValues(int layer) {
                settingInitialValuesFlag = true;
                activeLayer = layer;
                uiLayer.updateLayer();
                sldrCrossMaxRot.setValue(levelBuilder.getMaxSpeed());
                sldrCrossMinRot.setValue(levelBuilder.getMinSpeed());
                sldrCircleMaxRot.setValue(levelBuilder.getOriginMaxSpeed());
                sldrCircleMinRot.setValue(levelBuilder.getOriginMinSpeed());
                sldrColorCount.setValue(levelBuilder.getColorCount());
                cbRotateCCW.setChecked(levelBuilder.isCCWRotationEnabled());
                cbIsChained.setChecked(levelBuilder.isChained());
                tfCrossX.setText(String.valueOf((int) levelBuilder.getOffsetX()));
                tfCrossY.setText(String.valueOf((int) levelBuilder.getOffsetY()));
                tfCircleX.setText(String.valueOf((int) levelBuilder.getOriginX()));
                tfCircleY.setText(String.valueOf((int) levelBuilder.getOriginY()));
                uiInfo.lbl[1].setText(String.format(Locale.ROOT, "Origin-Offset Dist: %.3f", Math.hypot(levelBuilder.getOffsetX(), levelBuilder.getOffsetY())));
                settingInitialValuesFlag = false;
            }

            private void updatePositionValues() {
                tfCrossX.setText(String.valueOf((int) levelBuilder.getOffsetX()));
                tfCrossY.setText(String.valueOf((int) levelBuilder.getOffsetY()));
                tfCircleX.setText(String.valueOf((int) levelBuilder.getOriginX()));
                tfCircleY.setText(String.valueOf((int) levelBuilder.getOriginY()));
                uiInfo.lbl[1].setText(String.format(Locale.ROOT, "Origin-Offset Dist: %.3f", Math.hypot(levelBuilder.getOffsetX(), levelBuilder.getOffsetY())));
            }

            private Table createPositionGroup() {
                tfCircleX = new TextField("0", skin);
                tfCircleX.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfCircleX.setAlignment(Align.center);
                tfCircleX.setMaxLength(5);
                tfCircleX.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        textfieldInputCapture.capture(stage);
                        tfCircleX.setCursorPosition(tfCircleX.getText().length());
                        event.stop();
                        return true;
                    }
                });

                tfCircleY = new TextField("0", skin);
                tfCircleY.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfCircleY.setAlignment(Align.center);
                tfCircleY.setMaxLength(5);
                tfCircleY.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        textfieldInputCapture.capture(stage);
                        tfCircleY.setCursorPosition(tfCircleY.getText().length());
                        event.stop();
                        return true;
                    }
                });

                tfCrossX = new TextField("0", skin);
                tfCrossX.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfCrossX.setAlignment(Align.center);
                tfCrossX.setMaxLength(5);
                tfCrossX.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        textfieldInputCapture.capture(stage);
                        tfCrossX.setCursorPosition(tfCrossX.getText().length());
                        event.stop();
                        return true;
                    }
                });

                tfCrossY = new TextField("0", skin);
                tfCrossY.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfCrossY.setAlignment(Align.center);
                tfCrossY.setMaxLength(5);
                tfCrossY.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        textfieldInputCapture.capture(stage);
                        tfCrossY.setCursorPosition(tfCrossY.getText().length());
                        event.stop();
                        return true;
                    }
                });

                TextField.TextFieldListener offsetListener = new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField textField, char c) {
                        if (c == '\n' || c == '\r') {
                            textField.getOnscreenKeyboard().show(false);
                            stage.unfocusAll();
                            textfieldInputCapture.stop();
                        } else {
                            try {
                                String txt = tfCrossX.getText();
                                int x = txt.equals("") ? 0 : Integer.valueOf(txt);
                                txt = tfCrossY.getText();
                                int y = txt.equals("") ? 0 : Integer.valueOf(txt);
                                levelBuilder.setOffset(x, y);
                                uiInfo.lbl[1].setText(String.format(Locale.ROOT, "Origin-Offset Dist: %.3f", Math.hypot(x, y)));
                            } catch (NumberFormatException ignore) {
                                Components.showToast("[Error] " + ignore.getLocalizedMessage(), stage);
                            }
                        }
                    }
                };

                TextField.TextFieldListener originListener = new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField textField, char c) {
                        if (c == '\n' || c == '\r') {
                            textField.getOnscreenKeyboard().show(false);
                            stage.unfocusAll();
                            textfieldInputCapture.stop();
                        } else {
                            try {
                                String txt = tfCircleX.getText();
                                int x = txt.equals("") ? 0 : Integer.valueOf(txt);
                                txt = tfCircleY.getText();
                                int y = txt.equals("") ? 0 : Integer.valueOf(txt);
                                levelBuilder.setOrigin(x, y);
                            } catch (NumberFormatException ignore) {
                                Components.showToast("[Error] " + ignore.getLocalizedMessage(), stage);
                            }
                        }
                    }
                };


                final TextButton freeMove = UIFactory.createTextButton("Move", skin, "levelBuilderButtonChecked");
                freeMove.getLabelCell().pad(Value.percentHeight(.3f, freeMove.getLabel()));
                freeMove.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (isMovingOffset) {
                            moveInputCapture.stop();
                            isMovingOffset = false;
                            Components.showToast("Stopped: Moving offset", stage);
                        } else {
                            moveInputCapture.capture(stage);
                            isMovingOffset = true;
                            Components.showToast("Press Back Button to stop", stage);
                        }
                    }
                });

                InputListener moveOffsetListener = new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return worldInputHandler.touchDown(x, y, pointer, button);
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        worldInputHandler.touchUp(x, y, pointer, button);
                    }

                    @Override
                    public void touchDragged(InputEvent event, float x, float y, int pointer) {
                        worldInputHandler.touchDragged(x, y, pointer);
                    }

                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                            freeMove.setChecked(false);
                            return true;
                        }
                        return false;
                    }
                };
                moveInputCapture.setInputListener(moveOffsetListener);
                moveInputCapture.setKeyboardCapture(true);


                tfCircleX.setTextFieldListener(originListener);
                tfCircleY.setTextFieldListener(originListener);
                tfCrossX.setTextFieldListener(offsetListener);
                tfCrossY.setTextFieldListener(offsetListener);

                GlyphLayout glayout = new GlyphLayout(skin.getFont("h4"), " 6666 ");

                Table positionSettings = new Table(skin);
                positionSettings.defaults().padRight(3 * Gdx.graphics.getDensity());
                positionSettings.columnDefaults(1).width(glayout.width).padRight(10 * Gdx.graphics.getDensity());
                positionSettings.columnDefaults(3).width(glayout.width);
                positionSettings.row().padBottom(Value.percentHeight(.5f, tfCircleX));

                positionSettings.add("Circle: X", "h5");
                positionSettings.add(tfCircleX);

                positionSettings.add("Y", "h5");
                positionSettings.add(tfCircleY).row();

                positionSettings.add("Cross: X", "h5");
                positionSettings.add(tfCrossX);

                positionSettings.add("Y", "h5");
                positionSettings.add(tfCrossY).padRight(10 * Gdx.graphics.getDensity());
                positionSettings.add(freeMove).height(Value.percentHeight(1f, tfCrossX));

                return positionSettings;
            }

            @Override
            public Group getRoot() {
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
