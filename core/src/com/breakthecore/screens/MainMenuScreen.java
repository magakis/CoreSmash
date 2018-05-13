package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.breakthecore.CoreSmash;
import com.breakthecore.Launcher;
import com.breakthecore.WorldSettings;
import com.breakthecore.levelbuilder.LevelBuilderScreen;
import com.breakthecore.levels.CampaignLevel;
import com.breakthecore.levels.CampaignScreen;
import com.breakthecore.levels.Level;
import com.breakthecore.managers.MovingBallManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.screens.GameScreen.GameMode;
import com.breakthecore.ui.UIComponent;

import java.util.Locale;

/**
 * Created by Michail on 16/3/2018.
 */

public class MainMenuScreen extends ScreenBase {
    private CampaignScreen campaignScreen;
    private ScoresScreen scoresScreen;
    private LevelBuilderScreen levelBuilderScreen;
    private Stage stage;
    private Skin skin;
    private Stack rootStack;
    private UIComponent uiMainMenu, uiMenuOverlay;

    public MainMenuScreen(CoreSmash game) {
        super(game);
        stage = new Stage(game.getUIViewport());
        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(stage);
        setupMainMenuStage(stage);

        campaignScreen = new CampaignScreen(gameInstance);
        scoresScreen = new ScoresScreen(gameInstance);
        levelBuilderScreen = new LevelBuilderScreen(gameInstance);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    private void setupMainMenuStage(Stage stage) {
        skin = gameInstance.getSkin();

        uiMainMenu = new UIMainMenu();
        uiMenuOverlay = new UIOverlay();

        rootStack = new Stack();
        rootStack.setFillParent(true);
        rootStack.add(uiMainMenu.getRoot());
        rootStack.add(uiMenuOverlay.getRoot());

        stage.addActor(rootStack);
    }

    private void checkForLocalAccount() {
        Preferences prefs = Gdx.app.getPreferences("account");

        if (!prefs.getBoolean("valid", false)) {
            Gdx.input.getTextInput(new Input.TextInputListener() {
                @Override
                public void input(String text) {
                    if (text.length() < 3 || text.length() > 20) {
                        checkForLocalAccount();// XXX(26/4/2018): Recursive...
                        return;
                    }
                    Preferences prefs = Gdx.app.getPreferences("account");
                    prefs.putString("username", text);
                    prefs.putBoolean("valid", true);
                    prefs.flush();
                    gameInstance.getUserAccount().setUsername(text);
                }

                @Override
                public void canceled() {
                    checkForLocalAccount();
                }
            }, "Setup Username:", "", "Min 3 - Max 20 Characters");
        }
    }

    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                if (uiMainMenu.getRoot().getParent() == rootStack) {
                    Gdx.app.exit();
                    return true;
                } else {
                    rootStack.clear();
                    rootStack.addActor(uiMainMenu.getRoot());
                    rootStack.addActor(uiMenuOverlay.getRoot());
                }
            }
            return false;
        }
    }

    private class UIMainMenu implements UIComponent {
        Table root;

        public UIMainMenu() {
            root = new Table();

            Container btnPlay = newMenuButton("Play", "btnPlay", new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    gameInstance.setScreen(campaignScreen);
                }
            });

            Container btnAccount = newMenuButton("Scores", "btnAccount", new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    gameInstance.setScreen(scoresScreen);
                }
            });

            root.defaults()
                    .width(WorldSettings.getWorldWidth() * 3 / 5)
                    .height(WorldSettings.getWorldHeight() * 2 / 16)
                    .fill();

            Label versInfo = new Label("v.1.0.0 - Michail Angelos Gakis", skin, "comic_24b", Color.DARK_GRAY);
            versInfo.setAlignment(Align.bottom);

            root.bottom();
            root.add(btnPlay).padBottom(Value.percentHeight(1 / 16f, root)).row();
            root.add(btnAccount).padBottom(Value.percentHeight(3 / 16f, root)).row();
            root.add(versInfo).align(Align.center).height(versInfo.getPrefHeight());
        }

        private Container newMenuButton(String text, String name, EventListener el) {
            TextButton bt = new TextButton(text, skin.get("menuButton", TextButton.TextButtonStyle.class));
            bt.setName(name);
            bt.addListener(el);

            Container result = new Container(bt);
            result.setTransform(true);
            result.setOrigin(bt.getWidth() / 2, bt.getHeight() / 2);
            result.fill();
            result.setRotation(-.25f);
            result.addAction(Actions.forever(Actions.sequence(Actions.rotateBy(.5f, 1.5f, Interpolation.smoother), Actions.rotateBy(-.5f, 1.5f, Interpolation.smoother))));
            result.addAction(Actions.forever(Actions.sequence(Actions.scaleBy(.02f, .02f, 0.75f), Actions.scaleBy(-.02f, -.02f, 0.75f))));
            return result;
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIOverlay implements UIComponent {
        Table root;
        public UIOverlay() {
            root = new Table();
            Table tblSettings = new Table();

            tblSettings.setBackground(skin.getDrawable("box_white_5"));
            root.bottom().left();
            root.add(tblSettings).padBottom(80).padLeft(-10);

            ImageButton.ImageButtonStyle imgbsSound = new ImageButton.ImageButtonStyle();
            imgbsSound.imageUp = skin.getDrawable("map");
            imgbsSound.imageDown = skin.newDrawable("map", Color.RED);
            ImageButton imgbSound = new ImageButton(imgbsSound);
            imgbSound.getImageCell().width(70).height(70);
            tblSettings.add(imgbSound).height(80).width(80).padLeft(20);
            imgbSound.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    gameInstance.setScreen(levelBuilderScreen);
                }
            });

//            ImageButton.ImageButtonStyle imgbsCog = new ImageButton.ImageButtonStyle();
//            imgbsCog.imageUp = skin.getDrawable("cog");
//            imgbsCog.imageDown = skin.newDrawable("cog", Color.RED);
//            ImageButton imgbCog = new ImageButton(imgbsCog);
//            imgbCog.getImageCell().width(70).height(70);
//            tblSettings.add(imgbCog).height(80).width(80).padLeft(20);
//            imgbCog.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    rootStack.clear();
//                    rootStack.addActor(uiGameSettings.getRoot());
//                }
//            });
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIGameSettings implements UIComponent {
        Table root;
        Slider radiusSlider, minRotSlider, maxRotSlider, sldrBallSpeed, sldrLauncherCooldown, sldrColorCount;
        Label radiusLbl, minRotLbl, maxRotLbl, lblBallSpeed, lblLauncherCooldown, lblColorount, lblDifficulty;
        CheckBox cbUseMoves, cbUseLives, cbUseTime, cbSpinTheCoreMode, cbUseCustomMap;//, cbDrawCircle, cbDrawDiamond, cbDrawStar;
        TextField tfMoves, tfLives, tfTime;
        Table tblCheckboxesWithValues;
        StatsManager.ScoreMultiplier scoreMultiplier;

        final int settingsPadding = 50;

        // XXX(14/4/2018): *TOO* many magic values
        public UIGameSettings() {
            scoreMultiplier = new StatsManager.ScoreMultiplier();
            root = new Table();
            root.setFillParent(true);

            Label dummy = new Label
                    ("Game Setup", skin, "comic_96b");
            root.top().pad(50);
            root.add(dummy).padBottom(50).colspan(2).row();

            Table settingsTbl = new Table();
            settingsTbl.defaults().padBottom(settingsPadding);

            final ScrollPane scrollPane = new ScrollPane(settingsTbl);
            scrollPane.setScrollingDisabled(true, false);
            scrollPane.setCancelTouchFocus(false);
            scrollPane.setOverscroll(false, false);
            root.add(scrollPane).colspan(3).expand().fill().row();

            Preferences prefs = Gdx.app.getPreferences("game_settings");
            InputListener stopTouchDown = new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    event.stop();
                    return false;
                }
            };

            ChangeListener updateDifficultyListener = new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    updateDifficulty();
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

            cbUseLives = createCheckBox("Use Lives", prefs, "lives_enabled", false);
            cbUseLives.addListener(updateDifficultyListener);
            tfLives = createTextField(returnOnNewLineListener, prefs, "lives_amount", 3);
            tfLives.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tfLives.setCursorPosition(tfLives.getText().length());
                }
            });
            tfLives.addListener(updateDifficultyListener);

            cbUseMoves = createCheckBox("Use Moves", prefs, "moves_enabled", true);
            cbUseMoves.addListener(updateDifficultyListener);
            tfMoves = createTextField(returnOnNewLineListener, prefs, "move_count", 36);
            tfMoves.addListener(updateDifficultyListener);
            tfMoves.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tfMoves.setCursorPosition(tfMoves.getText().length());
                }
            });

            cbUseTime = createCheckBox("Use Time", prefs, "time_enabled", false);
            cbUseTime.addListener(updateDifficultyListener);
            tfTime = createTextField(returnOnNewLineListener, prefs, "time_amount", 180);
            tfTime.addListener(updateDifficultyListener);
            tfTime.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tfTime.setCursorPosition(tfTime.getText().length());
                }
            });

            cbUseCustomMap = createCheckBox("Use Custom Map", prefs, "custom_map_enabled", false);
            cbSpinTheCoreMode = createCheckBox("Spin The Core Mode", prefs, "spinthecore_enabled", false);

            tblCheckboxesWithValues = new Table();
            tblCheckboxesWithValues.defaults().padBottom(settingsPadding);

            tblCheckboxesWithValues.add(cbUseLives).left().padRight(15);
            tblCheckboxesWithValues.add(tfLives).center().width(101).padRight(60);
            tblCheckboxesWithValues.add(cbSpinTheCoreMode).left();
            tblCheckboxesWithValues.add().row();

            tblCheckboxesWithValues.add(cbUseMoves).left().padRight(15);
            tblCheckboxesWithValues.add(tfMoves).center().width(101).padRight(60);
            tblCheckboxesWithValues.add(cbUseCustomMap).left();
            tblCheckboxesWithValues.add().row();

            tblCheckboxesWithValues.add(cbUseTime).left().padRight(15).padBottom(0);
            tblCheckboxesWithValues.add(tfTime).center().width(101).padRight(60).padBottom(0);
            tblCheckboxesWithValues.add().padBottom(0);
            tblCheckboxesWithValues.add().padBottom(0).row();

            settingsTbl.add(tblCheckboxesWithValues).colspan(3).expandX().left().row();

            radiusSlider = new Slider(1, 8, 1, false, skin);
            radiusSlider.setValue(prefs.getInteger("init_radius", 4));
            radiusLbl = new Label(String.valueOf((int) radiusSlider.getValue()), skin, "comic_48");
            radiusSlider.addListener(stopTouchDown);
            radiusSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    radiusLbl.setText(String.valueOf((int) radiusSlider.getValue()));
                    updateDifficulty();
                }
            });
            attachSliderToTable("Circle Radius", radiusSlider, radiusLbl, settingsTbl);

            minRotSlider = new Slider(10, 120, 1, false, skin);
            minRotSlider.setValue(prefs.getFloat("min_rotation_speed", 40));
            minRotLbl = new Label(String.valueOf((int) minRotSlider.getValue()), skin, "comic_48");
            minRotSlider.addListener(stopTouchDown);
            minRotSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    int slider2 = (int) minRotSlider.getValue();
                    int slider3 = (int) maxRotSlider.getValue();

                    if (slider2 > slider3) {
                        maxRotSlider.setValue(slider2);
                    }

                    minRotLbl.setText(String.valueOf(slider2));
                }
            });
            attachSliderToTable("Min Rotation Speed", minRotSlider, minRotLbl, settingsTbl);

            maxRotSlider = new Slider(10, 120, 1, false, skin);
            maxRotSlider.setValue(prefs.getFloat("max_rotation_speed", 70));
            maxRotLbl = new Label(String.valueOf((int) maxRotSlider.getValue()), skin, "comic_48");
            maxRotSlider.addListener(stopTouchDown);
            maxRotSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    int slider2 = (int) minRotSlider.getValue();
                    int slider3 = (int) maxRotSlider.getValue();

                    if (slider2 > slider3) {
                        minRotSlider.setValue(slider3);
                    }

                    maxRotLbl.setText(String.valueOf((int) maxRotSlider.getValue()));
                }
            });
            attachSliderToTable("Max Rotation Speed", maxRotSlider, maxRotLbl, settingsTbl);

            sldrBallSpeed = new Slider(5, 20, 1, false, skin);
            sldrBallSpeed.setValue(prefs.getFloat("ball_speed", 15));
            lblBallSpeed = new Label(String.valueOf((int) sldrBallSpeed.getValue()), skin, "comic_48");
            sldrBallSpeed.addListener(stopTouchDown);
            sldrBallSpeed.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    lblBallSpeed.setText(String.valueOf((int) sldrBallSpeed.getValue()));
                }
            });
            attachSliderToTable("Ball Speed", sldrBallSpeed, lblBallSpeed, settingsTbl);

            sldrLauncherCooldown = new Slider(0f, 4.8f, .16f, false, skin);
            sldrLauncherCooldown.setValue(prefs.getFloat("launcher_cooldown", 0.16f));
            lblLauncherCooldown = new Label(String.format(Locale.ENGLISH, "%.2f", sldrLauncherCooldown.getValue()), skin, "comic_48");
            sldrLauncherCooldown.addListener(stopTouchDown);
            sldrLauncherCooldown.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    lblLauncherCooldown.setText(String.format(Locale.ENGLISH, "%.2f", sldrLauncherCooldown.getValue()));
                }
            });
            attachSliderToTable("Launcher Cooldown", sldrLauncherCooldown, lblLauncherCooldown, settingsTbl);

            sldrColorCount = new Slider(1, 8, 1, false, skin);
            sldrColorCount.setValue(prefs.getInteger("color_count", 7));
            lblColorount = new Label(String.valueOf((int) sldrColorCount.getValue()), skin, "comic_48");
            sldrColorCount.addListener(stopTouchDown);
            sldrColorCount.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    lblColorount.setText(String.valueOf((int) sldrColorCount.getValue()));
                    updateDifficulty();
                }
            });
            attachSliderToTable("Color Count", sldrColorCount, lblColorount, settingsTbl);

//
//            cbDrawCircle = createCheckBox("Draw Circle", prefs, "draw_circle", true);
//            cbDrawDiamond = createCheckBox("Draw Diamond", prefs, "draw_diamond", false);
//            cbDrawStar = createCheckBox("Draw Star", prefs, "draw_star", false);
//
//            Table tmGeneration = new Table();
//            tmGeneration.defaults().padBottom(50);
//            tmGeneration.add(new Label("~: Map Generation :~",skin, "comic_32"))
//                    .expandX().center().padBottom(25).row();
//
//            tmGeneration.add(cbDrawCircle).left().row();
//            tmGeneration.add(cbDrawDiamond).left().row();
//            tmGeneration.add(cbDrawStar).left().row();
//
//            settingsTbl.add(tmGeneration).growX().colspan(settingsTbl.getColumns()).left();

            //======= Play & Back Buttons =======
            TextButton tbtn = new TextButton("Back", skin);
            tbtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rootStack.clear();
                    rootStack.addActor(uiMainMenu.getRoot());
                    rootStack.addActor(uiMenuOverlay.getRoot());
                }
            });
            root.add(tbtn).width(250).height(200).padTop(50).align(Align.left);

            lblDifficulty = new Label("null", skin, "comic_48b");
            lblDifficulty.setAlignment(Align.center);
            updateDifficulty();
            root.add(lblDifficulty).expandX();

            tbtn = new TextButton("Play", skin);
            tbtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tfMoves.getText().isEmpty()) tfMoves.setText("0");
                    if (tfLives.getText().isEmpty()) tfLives.setText("0");
                    if (tfTime.getText().isEmpty()) tfTime.setText("0");

//                    Level dbLevel = new CampaignLevel(999, gameInstance.getUserAccount(), null) {
//                        @Override
//                        public void initialize(GameScreen.LevelTools levelTools) {
//                            Preferences prefs = Gdx.app.getPreferences("game_settings");
//
//                            TilemapManager tilemapManager = levelTools.tilemapManager;
//                            MovingBallManager movingBallManager = levelTools.movingBallManager;
//                            StatsManager statsManager = levelTools.statsManager;
//                            Launcher launcher = levelTools.launcher;
//
//
//                            boolean spinTheCoreEnabled = cbSpinTheCoreMode.isChecked();
//                            int minRotationSpeed = (int) minRotSlider.getValue();
//                            int maxRotationSpeed = (int) maxRotSlider.getValue();
//                            int initRadius = (int) radiusSlider.getValue();
//                            int colorCount = (int) sldrColorCount.getValue();
//                            int moves = Integer.parseInt(tfMoves.getText());
//                            int lives = Integer.parseInt(tfLives.getText());
//                            int time = Integer.parseInt(tfTime.getText());
//
//                            TilemapBuilder builder = tilemapManager.newLayer();
//                            builder.setColorCount(colorCount);
//                            if (cbUseCustomMap.isChecked()) {
//                                builder.loadMapFromFile("mainmenumap");
//                            } else {
//                                builder.generateRadius(initRadius);
//                            }
//                            if (colorCount > 1) {
//                                builder.reduceColorMatches(2)
//                                        .balanceColorAmounts()
//                                        .forceEachColorOnEveryRadius()
//                                        .reduceCenterTileColorMatch(2, false);
//                            }
//                            if (!spinTheCoreEnabled) {
//                                builder.setMinMaxRotationSpeed(minRotationSpeed, maxRotationSpeed);
//                            }
//                            builder.build();
//
//                            launcher.setLauncherCooldown(sldrLauncherCooldown.getValue());
////                            launcher.setAutoEject(spinTheCoreEnabled);
//                            movingBallManager.setDefaultBallSpeed((int) sldrBallSpeed.getValue());
//                            launcher.setLauncherSize(3);
//
//                            statsManager.setUserAccount(getUser());
//                            statsManager.setGameMode(spinTheCoreEnabled ? GameMode.SPIN_THE_CORE : GameMode.CLASSIC);
//                            statsManager.setLives(cbUseLives.isChecked(), lives);
//                            statsManager.setMoves(cbUseMoves.isChecked(), moves);
//                            statsManager.setTime(cbUseTime.isChecked(), time);
//                            statsManager.setSpecialBallCount(2);
//                            statsManager.getScoreMultiplier().setup(
//                                    colorCount,
//                                    cbUseLives.isChecked(), lives,
//                                    cbUseMoves.isChecked(), moves,
//                                    cbUseTime.isChecked(), time,
//                                    tilemapManager.getTotalTileCount());
//
//                            prefs.putBoolean("spinthecore_enabled", spinTheCoreEnabled);
//                            prefs.putBoolean("custom_map_enabled", cbUseCustomMap.isChecked());
//                            prefs.putInteger("init_radius", initRadius);
//                            prefs.putFloat("min_rotation_speed", minRotationSpeed);
//                            prefs.putFloat("max_rotation_speed", maxRotationSpeed);
//                            prefs.putFloat("launcher_cooldown", sldrLauncherCooldown.getValue());
//                            prefs.putFloat("ball_speed", sldrBallSpeed.getValue());
//                            prefs.putInteger("color_count", colorCount);
//                            prefs.putBoolean("moves_enabled", cbUseMoves.isChecked());
//                            prefs.putInteger("move_count", moves);
//                            prefs.putBoolean("time_enabled", cbUseTime.isChecked());
//                            prefs.putInteger("time_amount", time);
//                            prefs.putBoolean("lives_enabled", cbUseLives.isChecked());
//                            prefs.putInteger("lives_amount", lives);
//                            prefs.flush();
//                        }
//
//                        @Override
//                        public void update(float delta, TilemapManager tilemapManager) {
//
//                        }
//
//                        @Override
//                        public void end(StatsManager statsManager) {
//                            if (statsManager.getRoundOutcome()) {
//                                statsManager.getUser().saveScore(statsManager.getScore(), statsManager.getDifficultyMultiplier());
//                            }
//                        }
//                    };
//                    gameScreen.deployLevel(dbLevel);
                }
            });
            root.add(tbtn).

                    width(250).

                    height(200).

                    padTop(50).

                    align(Align.right);

        }

        private void updateDifficulty() {
            int lives = !tfLives.getText().equals("") ? Integer.parseInt(tfLives.getText()) : 0;
            int moves = !tfMoves.getText().equals("") ? Integer.parseInt(tfMoves.getText()) : 0;
            int time = !tfTime.getText().equals("") ? Integer.parseInt(tfTime.getText()) : 0;

            scoreMultiplier.setup(
                    (int) sldrColorCount.getValue(),
                    cbUseLives.isChecked(), lives,
                    cbUseMoves.isChecked(), moves,
                    cbUseTime.isChecked(), time,
                    scoreMultiplier.getTotalTilesFromRadius((int) radiusSlider.getValue()));
            lblDifficulty.setText(String.format(Locale.ENGLISH, "Difficulty:\n %.2f", scoreMultiplier.get()));
        }

        private TextField createTextField(TextField.TextFieldListener backOnNewLineListener, Preferences prefs, String prefName, int defVal) {
            TextField tf = new TextField("", skin);
            tf.setAlignment(Align.center);
            tf.setMaxLength(3);
            tf.setText(String.valueOf(prefs.getInteger(prefName, defVal)));
            tf.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
            tf.setTextFieldListener(backOnNewLineListener);
            return tf;
        }

        private CheckBox createCheckBox(String name, Preferences prefs, String prefName, boolean defVal) {
            CheckBox cb = new CheckBox(name, skin);
            cb.getImageCell().width(cb.getLabel().getPrefHeight()).height(cb.getLabel().getPrefHeight()).padRight(15);
            cb.getImage().setScaling(Scaling.fill);
            cb.setChecked(prefs.getBoolean(prefName, defVal));
            return cb;
        }

        private void attachSliderToTable(String name, Slider slider, Label amountLbl, Table tbl) {
            Label dummy = new Label(name + ":", skin, "comic_48b");
            tbl.add(dummy).padRight(30).align(Align.right).padBottom(5);
            tbl.add(amountLbl).align(Align.left).padBottom(5).expandX().row();
            tbl.add(slider).colspan(tbl.getColumns()).fill().expandX().row();
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

}
