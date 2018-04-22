package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
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
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.levels.CampaignLevel;
import com.breakthecore.levels.Level;
import com.breakthecore.managers.MovingTileManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.managers.TilemapManager;
import com.breakthecore.screens.GameScreen.GameMode;
import com.breakthecore.ui.UIComponent;

import java.util.Locale;

/**
 * Created by Michail on 16/3/2018.
 */

public class MainMenuScreen extends ScreenBase {
    private GameScreen gameScreen;
    private CampaignScreen campaignScreen;
    private Stage stage;
    private Skin skin;
    private Stack rootStack;
    private UIComponent uiMainMenu, uiMenuOverlay, uiGameSettings;

    public MainMenuScreen(BreakTheCoreGame game) {
        super(game);
        stage = new Stage(game.getWorldViewport());
        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(stage);
        setupMainMenuStage(stage);
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

        gameScreen = new GameScreen(gameInstance);
        campaignScreen = new CampaignScreen(gameInstance);

        uiMainMenu = new UIMainMenu();
        uiMenuOverlay = new UIOverlay();
        uiGameSettings = new UIGameSettings();

        rootStack = new Stack();
        rootStack.setFillParent(true);
        rootStack.add(uiMainMenu.getRoot());
        rootStack.add(uiMenuOverlay.getRoot());

        stage.addActor(rootStack);
    }

    private boolean checkForLocalAccount() {
        Preferences prefs = Gdx.app.getPreferences("account");

        if (!prefs.getBoolean("valid", false)) {
            Gdx.input.getTextInput(new Input.TextInputListener() {
                @Override
                public void input(String text) {
                    if (text.length() < 3) {
                        return;
                    }
                    Preferences prefs = Gdx.app.getPreferences("account");
                    prefs.putString("username", text);
                    prefs.putBoolean("valid", true);
                    prefs.flush();
                }

                @Override
                public void canceled() {
                    return;
                }
            }, "Setup Username:", "", "Minimum 3 chars");

        } else {
            return true;
        }

        return prefs.getBoolean("valid");
    }

    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
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

    private class UIMainMenu extends UIComponent {
        public UIMainMenu() {
            Table tblMain = new Table();

            Container btnPlay = newMenuButton("Play", "btnPlay", new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (checkForLocalAccount()) {
                        gameInstance.setScreen(campaignScreen);
                    }
                }
            });

            Container btnAccount = newMenuButton("Account", "btnAccount", new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {

                }
            });

            tblMain.defaults()
                    .width(WorldSettings.getWorldWidth() * 3 / 5)
                    .height(WorldSettings.getWorldHeight() * 2 / 16)
                    .fill();

            Label versInfo = new Label("v.1.0.0 - Michail Angelos Gakis", skin, "comic_24b", Color.DARK_GRAY);
            versInfo.setAlignment(Align.bottom);

            tblMain.bottom();
            tblMain.add(btnPlay).padBottom(Value.percentHeight(1 / 16f, tblMain)).row();
            tblMain.add(btnAccount).padBottom(Value.percentHeight(3 / 16f, tblMain)).row();
            tblMain.add(versInfo).align(Align.center).height(versInfo.getPrefHeight());

            setRoot(tblMain);
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
    }

    private class UIOverlay extends UIComponent {
        public UIOverlay() {
            Table tblRoot = new Table();
            Table tblSettings = new Table();

            tblSettings.setBackground(skin.getDrawable("box_white_5"));
            tblRoot.bottom().left();
            tblRoot.add(tblSettings).padBottom(80).padLeft(-10);

            ImageButton.ImageButtonStyle imgbsSound = new ImageButton.ImageButtonStyle();
            imgbsSound.imageUp = skin.getDrawable("speaker");
            imgbsSound.imageDown = skin.newDrawable("speaker", Color.RED);
            ImageButton imgbSound = new ImageButton(imgbsSound);
            imgbSound.getImageCell().width(70).height(70);
            tblSettings.add(imgbSound).height(80).width(80).padLeft(20);

            ImageButton.ImageButtonStyle imgbsCog = new ImageButton.ImageButtonStyle();
            imgbsCog.imageUp = skin.getDrawable("cog");
            imgbsCog.imageDown = skin.newDrawable("cog", Color.RED);
            ImageButton imgbCog = new ImageButton(imgbsCog);
            imgbCog.getImageCell().width(70).height(70);
            tblSettings.add(imgbCog).height(80).width(80).padLeft(20);
            imgbCog.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rootStack.clear();
                    rootStack.addActor(uiGameSettings.getRoot());
                }
            });

            setRoot(tblRoot);
        }
    }

    private class UIGameSettings extends UIComponent {
        Slider radiusSlider, minRotSlider, maxRotSlider, sldrBallSpeed, sldrLauncherCooldown, sldrColorCount;
        Label radiusLbl, minRotLbl, maxRotLbl, ballSpeedLbl, lblLauncherCooldown, lblColorount;
        CheckBox cbUseMoves, cbUseLives, cbUseTime, cbSpinTheCoreMode;
        TextField tfMoves, tfLives, tfTime;
        Table tblCheckboxesWithValues;

        final int settingsPadding = 50;

        // XXX(14/4/2018): *TOO* many magic values
        public UIGameSettings() {
            Table mainTable = new Table();
            mainTable.setFillParent(true);

            Label dummy = new Label
                    ("Game Setup", skin, "comic_96b");
            mainTable.top().pad(50);
            mainTable.add(dummy).padBottom(50).colspan(2).row();

            Table settingsTbl = new Table();
            settingsTbl.defaults().padBottom(settingsPadding);

            final ScrollPane scrollPane = new ScrollPane(settingsTbl);
            scrollPane.setScrollingDisabled(true, false);
            scrollPane.setCancelTouchFocus(false);
            scrollPane.setOverscroll(false,false);
            mainTable.add(scrollPane).colspan(2).expand().fill().row();

            Preferences prefs = Gdx.app.getPreferences("game_settings");
            InputListener stopTouchDown = new InputListener() {
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    event.stop();
                    return false;
                }
            };


            radiusSlider = new Slider(1, 8, 1, false, skin);
            radiusSlider.setValue(prefs.getInteger("init_radius", 4));
            radiusLbl = new Label(String.valueOf((int) radiusSlider.getValue()), skin, "comic_48");
            radiusSlider.addListener(stopTouchDown);
            radiusSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    radiusLbl.setText(String.valueOf((int) radiusSlider.getValue()));
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
            ballSpeedLbl = new Label(String.valueOf((int) sldrBallSpeed.getValue()), skin, "comic_48");
            sldrBallSpeed.addListener(stopTouchDown);
            sldrBallSpeed.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    ballSpeedLbl.setText(String.valueOf((int) sldrBallSpeed.getValue()));
                }
            });
            attachSliderToTable("Ball Speed", sldrBallSpeed, ballSpeedLbl, settingsTbl);

            sldrLauncherCooldown = new Slider(0f, 4.8f, .16f, false, skin);
            sldrLauncherCooldown.setValue(prefs.getFloat("launcher_cooldown", 0.16f));
            lblLauncherCooldown = new Label(String.format(Locale.ENGLISH,"%.2f",sldrLauncherCooldown.getValue()), skin, "comic_48");
            sldrLauncherCooldown.addListener(stopTouchDown);
            sldrLauncherCooldown.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    lblLauncherCooldown.setText(String.format(Locale.ENGLISH,"%.2f",sldrLauncherCooldown.getValue()));
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
                }
            });
            attachSliderToTable("Color Count", sldrColorCount, lblColorount, settingsTbl);


            tblCheckboxesWithValues = new Table();
            tblCheckboxesWithValues.defaults().padBottom(settingsPadding);

            TextField.TextFieldListener returnOnNewLineListener = new TextField.TextFieldListener() {
                public void keyTyped(TextField textField, char key) {
                    if (key == '\n' || key == '\r') {
                        textField.getOnscreenKeyboard().show(false);
                        stage.setKeyboardFocus(null);
                    }
                }
            };

            cbUseLives = new CheckBox("Use Lives", skin);
            cbUseLives.getImageCell().width(cbUseLives.getLabel().getPrefHeight()).height(cbUseLives.getLabel().getPrefHeight()).padRight(15);
            cbUseLives.getImage().setScaling(Scaling.fill);
            cbUseLives.setChecked(prefs.getBoolean("lives_enabled", true));

            tfLives = new TextField("", skin);
            tfLives.setAlignment(Align.center);
            tfLives.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
            tfLives.setTextFieldListener(returnOnNewLineListener);
            tfLives.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tfLives.setCursorPosition(tfLives.getText().length());
                }
            });
            tfLives.setMaxLength(3);
            tfLives.setText(String.valueOf(prefs.getInteger("lives_amount", 3)));

            cbUseMoves = new CheckBox("Use Moves", skin);
            cbUseMoves.getImageCell().width(cbUseMoves.getLabel().getPrefHeight()).height(cbUseMoves.getLabel().getPrefHeight()).padRight(15);
            cbUseMoves.getImage().setScaling(Scaling.fill);
            cbUseMoves.setChecked(prefs.getBoolean("moves_enabled", false));

            tfMoves = new TextField("", skin);
            tfMoves.setAlignment(Align.center);
            tfMoves.setMaxLength(3);
            tfMoves.setText(String.valueOf(prefs.getInteger("move_count", 40)));
            tfMoves.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
            tfMoves.setTextFieldListener(returnOnNewLineListener);
            tfMoves.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tfMoves.setCursorPosition(tfMoves.getText().length());
                }
            });

            cbUseTime = new CheckBox("Use Time", skin);
            cbUseTime.getImageCell().width(cbUseTime.getLabel().getPrefHeight()).height(cbUseTime.getLabel().getPrefHeight()).padRight(15);
            cbUseTime.getImage().setScaling(Scaling.fill);
            cbUseTime.setChecked(prefs.getBoolean("time_enabled", false));

            tfTime = new TextField("", skin);
            tfTime.setAlignment(Align.center);
            tfTime.setMaxLength(3);
            tfTime.setText(String.valueOf(prefs.getInteger("time_amount", 180)));
            tfTime.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
            tfTime.setTextFieldListener(returnOnNewLineListener);
            tfTime.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tfTime.setCursorPosition(tfTime.getText().length());
                }
            });

            cbSpinTheCoreMode = new CheckBox("Spin The Core Mode", skin);
            cbSpinTheCoreMode.getImageCell().width(cbSpinTheCoreMode.getLabel().getPrefHeight()).height(cbSpinTheCoreMode.getLabel().getPrefHeight()).padRight(15);
            cbSpinTheCoreMode.getImage().setScaling(Scaling.fill);
            cbSpinTheCoreMode.setChecked(prefs.getBoolean("spinthecore_enabled", false));

            tblCheckboxesWithValues.add(cbUseLives).left().padRight(15);
            tblCheckboxesWithValues.add(tfLives).center().width(101).padRight(60);
            tblCheckboxesWithValues.add(cbSpinTheCoreMode);
            tblCheckboxesWithValues.add().row();

            tblCheckboxesWithValues.add(cbUseMoves).left().padRight(15);
            tblCheckboxesWithValues.add(tfMoves).center().width(101).padRight(60);
            tblCheckboxesWithValues.add();
            tblCheckboxesWithValues.add().row();

            tblCheckboxesWithValues.add(cbUseTime).left().padRight(15);
            tblCheckboxesWithValues.add(tfTime).center().width(101).padRight(60);
            tblCheckboxesWithValues.add();
            tblCheckboxesWithValues.add().row();

            settingsTbl.add(tblCheckboxesWithValues).colspan(settingsTbl.getColumns()).expandX().left();

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
            mainTable.add(tbtn).width(250).height(200).padTop(50).align(Align.left);

            tbtn = new TextButton("Play", skin);
            tbtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (tfMoves.getText().isEmpty()) tfMoves.setText("0");
                    if (tfLives.getText().isEmpty()) tfLives.setText("0");
                    if (tfTime.getText().isEmpty()) tfTime.setText("0");

                    Level dbLevel = new CampaignLevel(999,null) {
                        @Override
                        public void initialize(StatsManager statsManager, TilemapManager tilemapManager, MovingTileManager movingTileManager) {
                            Preferences prefs = Gdx.app.getPreferences("game_settings");

                            boolean spinTheCoreEnabled = cbSpinTheCoreMode.isChecked();
                            int initRadius = (int) radiusSlider.getValue();
                            float minRotationSpeed = minRotSlider.getValue();
                            float maxRotationSpeed = maxRotSlider.getValue();
                            int colorCount = (int)sldrColorCount.getValue();

                            TilemapManager.TilemapGenerator tilemapGenerator = tilemapManager.getTilemapGenerator();
                            tilemapGenerator.setColorCount(colorCount);

                            tilemapManager.init(1);
                            Tilemap tm = tilemapManager.getTilemap(0);
                            tilemapGenerator.generateRadius(tm, initRadius);

                            tilemapGenerator.reduceColorMatches(tm, 3, 2);
                            tilemapGenerator.balanceColorAmounts(tm);
                            tilemapGenerator.reduceCenterTileColorMatch(tm, 2);

                            tm.setMinMaxSpeed(minRotationSpeed, maxRotationSpeed);
                            tm.setAutoRotation(!spinTheCoreEnabled);
                            tm.initialized();

                            movingTileManager.setLauncherCooldown(sldrLauncherCooldown.getValue());
                            movingTileManager.setColorCount(colorCount);
                            // movingTileManager.setAutoEject(spinTheCoreEnabled);
                            movingTileManager.setDefaultBallSpeed((int) sldrBallSpeed.getValue());
                            movingTileManager.initLauncher(3);


                            statsManager.setGameMode(spinTheCoreEnabled ? GameMode.SPIN_THE_CORE : GameMode.CLASSIC);
                            statsManager.setLives(cbUseLives.isChecked(), Integer.parseInt(tfLives.getText()));
                            statsManager.setMoves(cbUseMoves.isChecked(), Integer.parseInt(tfMoves.getText()));
                            statsManager.setTime(cbUseTime.isChecked(), Integer.parseInt(tfTime.getText()));
                            statsManager.setSpecialBallCount(0);

                            prefs.putBoolean("spinthecore_enabled", spinTheCoreEnabled);
                            prefs.putInteger("init_radius", initRadius);
                            prefs.putFloat("min_rotation_speed", minRotationSpeed);
                            prefs.putFloat("max_rotation_speed", maxRotationSpeed);
                            prefs.putFloat("launcher_cooldown", sldrLauncherCooldown.getValue());
                            prefs.putFloat("ball_speed", sldrBallSpeed.getValue());
                            prefs.putInteger("color_count", colorCount);
                            prefs.putBoolean("moves_enabled", cbUseMoves.isChecked());
                            prefs.putInteger("move_count", Integer.parseInt(tfMoves.getText()));
                            prefs.putBoolean("time_enabled", cbUseTime.isChecked());
                            prefs.putInteger("time_amount",Integer.parseInt(tfTime.getText()));
                            prefs.putBoolean("lives_enabled", cbUseLives.isChecked());
                            prefs.putInteger("lives_amount", Integer.parseInt(tfLives.getText()));
                            prefs.flush();
                        }

                        @Override
                        public void update(float delta, TilemapManager tilemapManager) {

                        }

                        @Override
                        public void end(boolean roundWon, StatsManager statsManager) {}
                    };


                    gameScreen.deployLevel(dbLevel);
                }
            });
            mainTable.add(tbtn).width(250).height(200).padTop(50).align(Align.right);

            setRoot(mainTable);
        }

        private void attachSliderToTable(String name, Slider slider, Label amountLbl, Table tbl) {
            Label dummy = new Label(name + ":", skin, "comic_48b");
            tbl.add(dummy).padRight(30).align(Align.right).padBottom(5);
            tbl.add(amountLbl).align(Align.left).padBottom(5).expandX().row();
            tbl.add(slider).colspan(2).fill().expandX().row();
        }
    }

//    private class PickGameModeUI extends UIComponent {
//        int gameModeCount = 3;
//        Value blockHeight;
//        Value blockPad;
//
//        public PickGameModeUI() {
//            Table tbl = new Table();
//            Label dummy;
//            tbl.setFillParent(true);
//            tbl.padLeft(Value.percentWidth(.05f));
//            tbl.padRight(Value.percentWidth(.05f));
//
//            blockHeight = Value.percentHeight(.6f / (gameModeCount + 1), tbl);
//            blockPad = Value.percentHeight(.3f / (gameModeCount + 2), tbl);
//
//            dummy = new Label("Game Mode", skin, "comic_96b");
//            tbl.add(dummy).padTop(blockPad).padBottom(blockPad).row();
//
//            String txt = "The core is spinning and your goal is to match balls of the same color " +
//                    "in order to beat it.";
//            dummy = new Label(":Classic Mode:", skin, "comic_48b");
//            TextButton tb = new TextButton(txt, skin, "modeButton");
//            tb.getLabel().setWrap(true);
//            tb.getLabelCell().pad(10);
//            tb.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    stage.clear();
//                    stage.addActor(m_classicSettingsUI.getRoot());
//                    roundSettings.gameMode = CLASSIC;
//                }
//            });
//
//            tbl.add(dummy).row();
//            tbl.add(tb).expandX().height(blockHeight).fill().padBottom(blockPad).row();
//
//            txt = "Expiremental campaign mode..";
//            dummy = new Label(":Campaign:", skin, "comic_48b");
//            tb = new TextButton(txt, skin, "modeButton");
//            tb.getLabel().setWrap(true);
//            tb.getLabelCell().pad(10);
//            tb.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    gameInstance.setScreen(m_campaignScreen);
//                }
//            });
//
//            tbl.add(dummy).row();
//            tbl.add(tb).expandX().height(blockHeight).fill().padBottom(blockPad).row();
//
//            root = tbl;
//        }
//    }

}
