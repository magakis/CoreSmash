package com.breakthecore.levelbuilder;

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
import com.breakthecore.ui.UIUtils;

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

        renderManager = game.getRenderManager();

        levelBuilder = new LevelBuilder(camera);
        levelBuilder.load("_editor_"); // Fails silently if it doesn't exist
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
        levelBuilder.draw(renderManager);
    }

    private Stage setupStage() {
        Stage stage = new Stage(gameInstance.getUIViewport());
        skin = gameInstance.getSkin();
        uiTools = new UITools();
        uiInfo = new UIInfo();
        uiToolbarTop = new UIToolbarTop();

        prefsStack = new UIComponentStack();
        prefsStack.setBackground(skin.getDrawable("boxBig"));

        Stack mainStack = new Stack();
        mainStack.setFillParent(true);

        Table prefs = new Table();
//        prefsStack.setMaxHeight(Value.percentHeight(.25f, prefs));
        prefs.center().bottom().add(prefsStack.getRoot()).growX().padBottom(-5);

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
            lblLayer.setText(levelBuilder.getLayer());
        }

        public abstract void onLayerChange(int layer);

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIToolbarTop implements UIComponent {
        private Container<Table> root;
        private SaveFileDialog saveFileDialog;
        private LoadFileDialog loadFileDialog;
        private Level testLevel;
        private final TextButton tbSave, tbLoad, tbDeploy;
        private String filenameCache = "";

        UIToolbarTop() {
            tbSave = new TextButton("Save", skin, "levelBuilderButton");
            tbSave.getLabelCell().pad(Value.percentHeight(.5f, tbSave.getLabel()));
            tbSave.getLabelCell().padTop(Value.percentHeight(.25f, tbSave.getLabel()));
            tbSave.getLabelCell().padBottom(Value.percentHeight(.25f, tbSave.getLabel()));
            tbSave.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    saveFileDialog.show(stage, filenameCache);
                }
            });
            tbLoad = new TextButton("Load", skin, "levelBuilderButton");
            tbLoad.getLabelCell().pad(Value.percentHeight(.5f, tbLoad.getLabel()));
            tbLoad.getLabelCell().padTop(Value.percentHeight(.25f, tbLoad.getLabel()));
            tbLoad.getLabelCell().padBottom(Value.percentHeight(.25f, tbLoad.getLabel()));
            tbLoad.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    loadFileDialog.show(stage);
                }
            });

            tbDeploy = new TextButton("Deploy", skin, "levelBuilderButton");
            tbDeploy.getLabelCell().pad(Value.percentHeight(.5f, tbDeploy.getLabel()));
            tbDeploy.getLabelCell().padTop(Value.percentHeight(.25f, tbDeploy.getLabel()));
            tbDeploy.getLabelCell().padBottom(Value.percentHeight(.25f, tbDeploy.getLabel()));
            tbDeploy.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelBuilder.saveAs("_editor_");
                    gameScreen.deployLevel(testLevel);
                }
            });

            root = new Container<>();

            Table main = new Table(skin);
            root.top().right().padTop(-6 * Gdx.graphics.getDensity()).padRight(-6 * Gdx.graphics.getDensity()).setActor(main);
            main.setBackground("boxBig");
            main.pad(10 * Gdx.graphics.getDensity());
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

            Window.WindowStyle wsLoad = new Window.WindowStyle();
            wsLoad.background = skin.getDrawable("boxBig");
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
        public Group getRoot() {
            return root;
        }
    }

    private class UITools implements UIComponent {
        private Container<Table> root;
        private ButtonGroup<TextButton> btnGroup;

        UITools() {
            root = new Container<>();

            final TextButton tbDraw, tbErase, tbRotate;

            tbDraw = new TextButton("Draw", skin, "levelBuilderButtonChecked");
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
            tbErase = new TextButton("Erase", skin, "levelBuilderButtonChecked");
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

            tbRotate = new TextButton("Rotate", skin, "levelBuilderButtonChecked");
            tbRotate.getLabelCell().pad(Value.percentHeight(.5f, tbRotate.getLabel()));
            tbRotate.getLabelCell().padTop(Value.percentHeight(.25f, tbRotate.getLabel()));
            tbRotate.getLabelCell().padBottom(Value.percentHeight(.25f, tbRotate.getLabel()));
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


            Table main = new Table(skin);
            main.setBackground("boxBig");
            main.setTouchable(Touchable.enabled);
            main.addCaptureListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    event.handle();
                    return true;
                }
            });

//            main.defaults().pad(Value.percentWidth(.1f, tbRotate)).padRight(0);

            main.defaults().width(tbRotate.getWidth()).pad(5 * Gdx.graphics.getDensity());
            main.add(tbDraw).row();
            main.add(tbErase).padTop(0).row();
            main.add(tbRotate).padTop(0).row();
            root.center().right().padRight(-6 * Gdx.graphics.getDensity()).setActor(main);

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

                final List<Integer> knownIds = TileIndex.get().getAllPlaceableIDs();

                materialButtons = new ImageButton[knownIds.size()];
                int buttonIndex = 0;
                float ballSize = skin.get("h2", Label.LabelStyle.class).font.getLineHeight();
                for (int i = 0; i < knownIds.size(); ++i) {
                    ImageButton.ImageButtonStyle imgbs;
                    imgbs = new ImageButton.ImageButtonStyle();
                    imgbs.imageUp = new TextureRegionDrawable(new TextureRegion(renderManager.getTextureFor(knownIds.get(i))));
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
                uiInfo.lbl[0].setText("TileID: " + levelBuilder.getCurrentTileID());
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

        FreeMode() {
            optionsMenu = new OptionsMenu();
            setPreferencesRoot(optionsMenu);
        }


        @Override
        public boolean touchDown(float x, float y, int count, int button) {
            return isMovingOffset;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (isMovingOffset) {
                levelBuilder.moveOffsetBy(deltaX, -deltaY);
                optionsMenu.mapSettings.updatePositionValues();
            } else {
                camera.position.add(-deltaX * currentZoom, deltaY * currentZoom, 0);
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

                TextButton btnLevelSettings = new TextButton("Level", skin, "levelBuilderButton");
                btnLevelSettings.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        prefsStack.push(gameSettings);
                    }
                });

                TextButton btnMapSettings = new TextButton("Layer", skin, "levelBuilderButton");
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
            TextField tfMoves, tfLives, tfTime;

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
                            textField.getOnscreenKeyboard().show(false);
                            stage.setKeyboardFocus(null);
                        }
                    }
                };

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

                sldrLauncherCooldown = new Slider(0f, 4.8f, .16f, false, skin);
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
                root.add(grpBallSpeed).growX();

                updateValues();
                updateMovesPercent(levelBuilder.getMoves());
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
                float totalTiles = levelBuilder.getTotalTileCount();
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
            public Group getRoot() {
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
            TextField tfOriginX, tfOriginY, tfOffsetX, tfOffsetY;
            Slider sldrMinRot, sldrMaxRot, sldrColorCount, sldrOriginMinRot, sldrOriginMaxRot;
            Label lblMinRot, lblMaxRot, lblColorCount, lblOriginMinRot, lblOriginMaxRot;
            CheckBox cbRotateCCW, cbIsChained;
            int activeLayer;

            UIMapSettings() {
                uiLayer = new UILayer() {
                    @Override
                    public void onLayerChange(int layer) {
                        updateValues(layer);
                    }
                };

                InputListener stopTouchDown = new InputListener() {
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        event.stop();
                        return false;
                    }
                };

                activeLayer = levelBuilder.getLayer();
                sldrMinRot = new Slider(0, 120, 1, false, skin);
                lblMinRot = new Label(String.format(Locale.ROOT, "Min:%3d", (int) sldrMinRot.getValue()), skin, "h5");
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
                sldrMinRot.addListener(stopTouchDown);

                sldrMaxRot = new Slider(0, 120, 1, false, skin);
                lblMaxRot = new Label(String.format(Locale.ROOT, "Max:%3d", (int) sldrMaxRot.getValue()), skin, "h5");
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
                sldrMaxRot.addListener(stopTouchDown);

                sldrOriginMinRot = new Slider(0, 120, 1, false, skin);
                lblOriginMinRot = new Label(String.format(Locale.ROOT, "Min:%3d", (int) sldrOriginMinRot.getValue()), skin, "h5");
                sldrOriginMinRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int min = (int) sldrOriginMinRot.getValue();
                        lblOriginMinRot.setText(String.format(Locale.ROOT, "Min:%3d", min));
                        levelBuilder.setOriginMinSpeed(min);
                    }
                });
                sldrOriginMinRot.addListener(new DragListener() {
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
                        int min = (int) sldrOriginMinRot.getValue();
                        int max = (int) sldrOriginMaxRot.getValue();
                        if (Integer.compare(min, max) == 1) {
                            sldrOriginMaxRot.setValue(min);
                        }
                        lblOriginMinRot.setText(String.format(Locale.ROOT, "Min:%3d", min));
                        levelBuilder.setOriginMinSpeed(min);

                    }
                });
                sldrOriginMinRot.addListener(stopTouchDown);

                sldrOriginMaxRot = new Slider(0, 120, 1, false, skin);
                lblOriginMaxRot = new Label(String.format(Locale.ROOT, "Max:%3d", (int) sldrOriginMaxRot.getValue()), skin, "h5");
                sldrOriginMaxRot.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int max = (int) sldrOriginMaxRot.getValue();
                        lblOriginMaxRot.setText(String.format(Locale.ROOT, "Max:%3d", max));
                        levelBuilder.setOriginMaxSpeed(max);
                    }
                });
                sldrOriginMaxRot.addListener(new DragListener() {
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
                        int min = (int) sldrOriginMinRot.getValue();
                        int max = (int) sldrOriginMaxRot.getValue();
                        if (Integer.compare(min, max) == 1) {
                            sldrOriginMinRot.setValue(max);
                        }
                        lblOriginMaxRot.setText(String.format(Locale.ROOT, "Max:%3d", max));
                        levelBuilder.setOriginMaxSpeed(max);
                    }
                });
                sldrOriginMaxRot.addListener(stopTouchDown);

                sldrColorCount = new Slider(levelBuilder.getColorCount(), 8, 1, false, skin);
                lblColorCount = new Label(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()), skin, "h5");
                sldrColorCount.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lblColorCount.setText(String.format(Locale.ROOT, "Colors:%2d", (int) sldrColorCount.getValue()));
                        levelBuilder.setColorCount((int) sldrColorCount.getValue());
                    }
                });
                sldrColorCount.addListener(stopTouchDown);

                cbRotateCCW = ActorFactory.createCheckBox("Rotate CCW", skin);
                cbRotateCCW.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setCCWRotation(cbRotateCCW.isChecked());
                    }
                });

                cbIsChained = ActorFactory.createCheckBox("Chained", skin);
                cbIsChained.setChecked(true);
                cbIsChained.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelBuilder.setChained(cbIsChained.isChecked());
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

                Table positionSettings = createPositionGroup();

                float labelPercent = .15f;

                Table rotLabels = new Table();
                rotLabels.defaults().expandX().padBottom(Value.percentHeight(.5f, sldrMinRot));
                rotLabels.columnDefaults(0).padRight(Value.percentHeight(1f, sldrMinRot));
                rotLabels.add(new Label(":Position:", skin, "h5")).colspan(2).padRight(0).padBottom(Value.percentHeight(.5f, sldrMinRot)).row();
                rotLabels.add(positionSettings).colspan(2).padBottom(Value.percentHeight(.5f, sldrMinRot)).padRight(0).row();
                rotLabels.add(new Label(":Rotation:", skin, "h5")).colspan(2).padRight(0).padBottom(0).row();
                rotLabels.row().padBottom(Value.percentHeight(labelPercent, sldrMinRot));
                rotLabels.add(lblMinRot);
                rotLabels.add(lblMaxRot).row();
                rotLabels.add(sldrMinRot).fill();
                rotLabels.add(sldrMaxRot).fill().row();

                rotLabels.add(new Label(":Origin Rotation:", skin, "h5")).colspan(2).padRight(0).padBottom(0).row();
                rotLabels.row().padBottom(Value.percentHeight(labelPercent, sldrOriginMinRot));
                rotLabels.add(lblOriginMinRot);
                rotLabels.add(lblOriginMaxRot).row();
                rotLabels.add(sldrOriginMinRot).fill();
                rotLabels.add(sldrOriginMaxRot).fill().row();

                rotLabels.add(cbRotateCCW).left().padBottom(0);
                rotLabels.add(cbIsChained).right().padRight(0).padBottom(0).row();

                rotLabels.add(lblColorCount).colspan(2).padRight(0).padBottom(Value.percentHeight(labelPercent, sldrMinRot)).row();
                rotLabels.add(sldrColorCount).colspan(2).padRight(0).fill().row();

                VerticalGroup vgroup = new VerticalGroup();
                vgroup.grow();
                vgroup.addActor(rotLabels);

                ScrollPane scrollPane = new ScrollPane(vgroup);
                scrollPane.setOverscroll(false, false);
                scrollPane.setScrollingDisabled(true, false);
                scrollPane.setCancelTouchFocus(false);

                Table main = new Table();
                main.add(uiLayer.root);
                main.add(scrollPane).grow()
                        .maxHeight(Value.percentHeight(.35f, UIUtils.getScreenActor()))
                        .padLeft(Value.percentHeight(.5f, lblMinRot))
                        .padRight(Value.percentHeight(.5f, lblMinRot));

                root = new Container<>(main);
                root.fill();
            }

            private void updateValues(int layer) {
                activeLayer = layer;
                uiLayer.updateLayer();
                sldrMaxRot.setValue(levelBuilder.getMaxSpeed());
                sldrMinRot.setValue(levelBuilder.getMinSpeed());
                sldrOriginMaxRot.setValue(levelBuilder.getOriginMaxSpeed());
                sldrOriginMinRot.setValue(levelBuilder.getOriginMinSpeed());
                sldrColorCount.setValue(levelBuilder.getColorCount());
                cbRotateCCW.setChecked(levelBuilder.isCCWRotationEnabled());
                cbIsChained.setChecked(levelBuilder.isChained());
                tfOffsetX.setText(String.valueOf((int) levelBuilder.getOffsetX()));
                tfOffsetY.setText(String.valueOf((int) levelBuilder.getOffsetY()));
                tfOriginX.setText(String.valueOf((int) levelBuilder.getOriginX()));
                tfOriginY.setText(String.valueOf((int) levelBuilder.getOriginY()));
                uiInfo.lbl[1].setText(String.format(Locale.ROOT, "Origin<->Offset Dist: %.3f", Math.hypot(levelBuilder.getOffsetX(), levelBuilder.getOffsetY())));
            }

            private void updatePositionValues() {
                tfOffsetX.setText(String.valueOf((int) levelBuilder.getOffsetX()));
                tfOffsetY.setText(String.valueOf((int) levelBuilder.getOffsetY()));
                tfOriginX.setText(String.valueOf((int) levelBuilder.getOriginX()));
                tfOriginY.setText(String.valueOf((int) levelBuilder.getOriginY()));
                uiInfo.lbl[1].setText(String.format(Locale.ROOT, "Origin<->Offset Dist: %.3f", Math.hypot(levelBuilder.getOffsetX(), levelBuilder.getOffsetY())));
            }

            private Table createPositionGroup() {
                final TextField.TextFieldListener returnOnNewLineListener = new TextField.TextFieldListener() {
                    public void keyTyped(TextField textField, char key) {
                        if (key == '\n' || key == '\r') {
                            textField.getOnscreenKeyboard().show(false);
                            stage.setKeyboardFocus(null);
                        }
                    }
                };

                tfOriginX = new TextField("0", skin);
                tfOriginX.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfOriginX.setAlignment(Align.center);
                tfOriginX.setMaxLength(6);
                tfOriginX.setTextFieldListener(returnOnNewLineListener);
                tfOriginX.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        tfOriginX.setCursorPosition(tfOriginX.getText().length());
                    }
                });

                tfOriginY = new TextField("0", skin);
                tfOriginY.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfOriginY.setAlignment(Align.center);
                tfOriginY.setMaxLength(6);
                tfOriginY.setTextFieldListener(returnOnNewLineListener);
                tfOriginY.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        tfOriginY.setCursorPosition(tfOriginY.getText().length());
                    }
                });

                tfOffsetX = new TextField("0", skin);
                tfOffsetX.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfOffsetX.setAlignment(Align.center);
                tfOffsetX.setMaxLength(6);
                tfOffsetX.setTextFieldListener(returnOnNewLineListener);
                tfOffsetX.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        tfOffsetX.setCursorPosition(tfOffsetX.getText().length());
                    }
                });

                tfOffsetY = new TextField("0", skin);
                tfOffsetY.setTextFieldFilter(UIUtils.getNumbersOnlyFilter());
                tfOffsetY.setAlignment(Align.center);
                tfOffsetY.setMaxLength(6);
                tfOffsetY.setTextFieldListener(returnOnNewLineListener);
                tfOffsetY.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        tfOffsetY.setCursorPosition(tfOffsetY.getText().length());
                    }
                });

                TextField.TextFieldListener offsetListener = new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField textField, char c) {
                        try {
                            String txt = tfOffsetX.getText();
                            int x = txt.equals("") ? 0 : Integer.valueOf(txt);
                            txt = tfOffsetY.getText();
                            int y = txt.equals("") ? 0 : Integer.valueOf(txt);
                            levelBuilder.setOffset(x, y);
                            uiInfo.lbl[1].setText(String.format(Locale.ROOT, "Origin<->Offset Dist: %.3f", Math.hypot(x, y)));
                        } catch (NumberFormatException ignore) {
                            Components.showToast("[Error] " + ignore.getLocalizedMessage(), stage);
                        }
                    }
                };

                TextField.TextFieldListener originListener = new TextField.TextFieldListener() {
                    @Override
                    public void keyTyped(TextField textField, char c) {
                        String txt = tfOriginX.getText();
                        try {
                            int x = txt.equals("") ? 0 : Integer.valueOf(txt);
                            txt = tfOriginY.getText();
                            int y = txt.equals("") ? 0 : Integer.valueOf(txt);
                            levelBuilder.setOrigin(x, y);
                        } catch (NumberFormatException ignore) {
                            Components.showToast("[Error] " + ignore.getLocalizedMessage(), stage);
                        }
                    }
                };

//                TextButton offsetPlus = new TextButton("+", skin);
//                TextButton offsetMinus = new TextButton("-", skin);
//                TextButton originPlus = new TextButton("+", skin);
//                TextButton originMinus = new TextButton("-", skin);

                final TextButton freeMove = new TextButton("Move", skin, "levelBuilderButton");
                freeMove.getLabelCell().pad(Value.percentHeight(.3f, freeMove.getLabel()));
                freeMove.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (freeMode.isMovingOffset) {
                            freeMode.isMovingOffset = false;
                            Components.showToast("Stopped: Moving offset", stage);
                        } else {
                            freeMode.isMovingOffset = true;
                            Components.showToast("Press Back Button to stop", stage);
                        }
                    }
                });

                tfOriginX.setTextFieldListener(originListener);
                tfOriginY.setTextFieldListener(originListener);
                tfOffsetX.setTextFieldListener(offsetListener);
                tfOffsetY.setTextFieldListener(offsetListener);

                GlyphLayout glayout = new GlyphLayout(skin.getFont("h4"), " 666666 ");

                Table positionSettings = new Table(skin);
                positionSettings.defaults().padRight(3 * Gdx.graphics.getDensity());
                positionSettings.columnDefaults(1).width(glayout.width).padRight(Value.percentHeight(1, tfOffsetX));
                positionSettings.columnDefaults(3).width(glayout.width);
                positionSettings.row().padBottom(Value.percentHeight(.5f, tfOriginX));

                positionSettings.add("Origin: X", "h5");
                positionSettings.add(tfOriginX);

                positionSettings.add("Y", "h5");
                positionSettings.add(tfOriginY).row();

                positionSettings.add("Offset: X", "h5");
                positionSettings.add(tfOffsetX);

                positionSettings.add("Y", "h5");
                positionSettings.add(tfOffsetY);
                positionSettings.add(freeMove).height(Value.percentHeight(1f, tfOffsetX));

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

    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                if (freeMode.isMovingOffset) {
                    freeMode.isMovingOffset = false;
                    Components.showToast("Stopped: Moving offset", stage);
                    uiInfo.lbl[2].setText("");

                } else {
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
