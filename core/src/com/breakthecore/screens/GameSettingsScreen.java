//package com.breakthecore.screens;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Input;
//import com.badlogic.gdx.InputAdapter;
//import com.badlogic.gdx.scenes.scene2d.Actor;
//import com.badlogic.gdx.scenes.scene2d.InputEvent;
//import com.badlogic.gdx.scenes.scene2d.InputListener;
//import com.badlogic.gdx.scenes.scene2d.Stage;
//import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
//import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
//import com.badlogic.gdx.scenes.scene2d.ui.Label;
//import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
//import com.badlogic.gdx.scenes.scene2d.ui.Skin;
//import com.badlogic.gdx.scenes.scene2d.ui.Slider;
//import com.badlogic.gdx.scenes.scene2d.ui.Table;
//import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
//import com.badlogic.gdx.scenes.scene2d.ui.TextField;
//import com.badlogic.gdx.scenes.scene2d.ui.Value;
//import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
//import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
//import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
//import com.badlogic.gdx.utils.Align;
//import com.badlogic.gdx.utils.Scaling;
//import com.breakthecore.BreakTheCoreGame;
//import com.breakthecore.GameRoundSettings;
//import com.breakthecore.ui.UIBase;
//
//import java.util.Locale;
//
//public class GameSettingsScreen extends ScreenBase {
//    private Stage stage;
//    private Skin skin;
//    private PickGameModeUI m_pickGameModeUI;
//    private ClassicSettingsUI m_classicSettingsUI;
//    private SpinTheCoreSettingsUI m_spinTheCoreSettingsUI;
//    private GameRoundSettings roundSettings;
//    private GameScreen gameScreen;
//    private GameScreen.RoundBuilder roundBuilder;
//    private CampaignScreen m_campaignScreen;
//
//    public GameSettingsScreen(BreakTheCoreGame game) {
//        super(game);
//        skin = game.getSkin();
//        stage = new Stage(game.getWorldViewport());
//        m_classicSettingsUI = new ClassicSettingsUI();
//        m_spinTheCoreSettingsUI = new SpinTheCoreSettingsUI();
//        m_pickGameModeUI = new PickGameModeUI();
//        roundSettings = new GameRoundSettings();
//
//        gameScreen = new GameScreen(m_game);
//        roundBuilder = gameScreen.getRoundBuilder();
//        m_campaignScreen = new CampaignScreen(m_game);
//
//        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
//        screenInputMultiplexer.addProcessor(stage);
//
//        stage.addActor(m_pickGameModeUI.getRoot());
//    }
//
//    public void tmpReset() {
//        stage.clear();
//        stage.addActor(m_pickGameModeUI.getRoot());
//    }
//
//    @Override
//    public void render(float delta) {
//        stage.act();
//        stage.draw();
//    }
//
//    private class ClassicSettingsUI extends UIBase {
//        Slider radiusSlider, minRotSlider, maxRotSlider, ballSpeedSlider, sldrLauncherCooldown;
//        Label radiusLbl, minRotLbl, maxRotLbl, ballSpeedLbl, lblLauncherCooldown;
//        CheckBox cbUseMoves, cbUseLives, cbUseTime, cbSpinTheCoreMode;
//        TextField tfMoves, tfLives, tfTime;
//        Table tblCheckboxesWithValues;
//
//        final int settingsPadding = 50;
//
//        // XXX(14/4/2018): *TOO* many magic values
//        public ClassicSettingsUI() {
//            Table mainTable = new Table();
//            mainTable.setFillParent(true);
//
//            Label dummy = new Label
//                    ("Game Setup", skin, "comic_96b");
//            mainTable.top().pad(50);
//            mainTable.add(dummy).padBottom(100).colspan(2).row();
//
//            Table settingsTbl = new Table();
//            settingsTbl.defaults().padBottom(settingsPadding);
//
//            final ScrollPane scrollPane = new ScrollPane(settingsTbl);
//            mainTable.add(scrollPane).colspan(2).expand().fill().row();
//
//            radiusSlider = new Slider(1, 8, 1, false, skin);
//            radiusSlider.setValue(4);
//            radiusLbl = new Label(String.valueOf((int) radiusSlider.getValue()), skin, "comic_48");
//            radiusSlider.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    radiusLbl.setText(String.valueOf((int) radiusSlider.getValue()));
//                }
//            });
//            attachSliderToTable("Circle Radius", radiusSlider, radiusLbl, settingsTbl);
//
//            minRotSlider = new Slider(10, 120, 1, false, skin);
//            minRotSlider.setValue(30);
//            minRotLbl = new Label(String.valueOf((int) minRotSlider.getValue()), skin, "comic_48");
//            minRotSlider.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    int slider2 = (int) minRotSlider.getValue();
//                    int slider3 = (int) maxRotSlider.getValue();
//
//                    if (slider2 > slider3) {
//                        maxRotSlider.setValue(slider2);
//                    }
//
//                    minRotLbl.setText(String.valueOf(slider2));
//                }
//            });
//            attachSliderToTable("Min Rotation Speed", minRotSlider, minRotLbl, settingsTbl);
//
//            maxRotSlider = new Slider(10, 120, 1, false, skin);
//            maxRotSlider.setValue(70);
//            maxRotLbl = new Label(String.valueOf((int) maxRotSlider.getValue()), skin, "comic_48");
//            maxRotSlider.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    int slider2 = (int) minRotSlider.getValue();
//                    int slider3 = (int) maxRotSlider.getValue();
//
//                    if (slider2 > slider3) {
//                        minRotSlider.setValue(slider3);
//                    }
//
//                    maxRotLbl.setText(String.valueOf((int) maxRotSlider.getValue()));
//                }
//            });
//            attachSliderToTable("Max Rotation Speed", maxRotSlider, maxRotLbl, settingsTbl);
//
//            ballSpeedSlider = new Slider(5, 20, 1, false, skin);
//            ballSpeedSlider.setValue(15);
//            ballSpeedLbl = new Label(String.valueOf((int) ballSpeedSlider.getValue()), skin, "comic_48");
//            ballSpeedSlider.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    ballSpeedLbl.setText(String.valueOf((int) ballSpeedSlider.getValue()));
//                }
//            });
//            attachSliderToTable("Ball Speed", ballSpeedSlider, ballSpeedLbl, settingsTbl);
//
//            sldrLauncherCooldown = new Slider(.16f, 4.8f, .16f, false, skin);
//            sldrLauncherCooldown.setValue(.16f);
//            lblLauncherCooldown = new Label(String.format(Locale.ENGLISH,"%.2f",sldrLauncherCooldown.getValue()), skin, "comic_48");
//            sldrLauncherCooldown.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    lblLauncherCooldown.setText(String.format(Locale.ENGLISH,"%.2f",sldrLauncherCooldown.getValue()));
//                }
//            });
//            attachSliderToTable("Launcher Cooldown", sldrLauncherCooldown, lblLauncherCooldown, settingsTbl);
//
//
//            tblCheckboxesWithValues = new Table();
//            tblCheckboxesWithValues.defaults().padBottom(settingsPadding);
//
//            TextField.TextFieldListener returnOnNewLineListener = new TextField.TextFieldListener() {
//                public void keyTyped(TextField textField, char key) {
//                    if (key == '\n' || key == '\r') {
//                        textField.getOnscreenKeyboard().show(false);
//                        stage.setKeyboardFocus(null);
//                    }
//                }
//            };
//
//            cbUseLives = new CheckBox("Use Lives", skin);
//            cbUseLives.getImageCell().width(cbUseLives.getLabel().getPrefHeight()).height(cbUseLives.getLabel().getPrefHeight()).padRight(15);
//            cbUseLives.getImage().setScaling(Scaling.fill);
//            cbUseLives.setChecked(true);
//
//            tfLives = new TextField("", skin);
//            tfLives.setAlignment(Align.center);
//            tfLives.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
//            tfLives.setTextFieldListener(returnOnNewLineListener);
//            tfLives.addListener(new ClickListener() {
//                @Override
//                public void clicked(InputEvent event, float x, float y) {
//                    tfLives.setCursorPosition(tfLives.getText().length());
//                }
//            });
//            tfLives.setMaxLength(3);
//            tfLives.setText("3");
//
//            cbUseMoves = new CheckBox("Use Moves", skin);
//            cbUseMoves.getImageCell().width(cbUseMoves.getLabel().getPrefHeight()).height(cbUseMoves.getLabel().getPrefHeight()).padRight(15);
//            cbUseMoves.getImage().setScaling(Scaling.fill);
//
//            tfMoves = new TextField("", skin);
//            tfMoves.setAlignment(Align.center);
//            tfMoves.setMaxLength(3);
//            tfMoves.setText("35");
//            tfMoves.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
//            tfMoves.setTextFieldListener(returnOnNewLineListener);
//            tfMoves.addListener(new ClickListener() {
//                @Override
//                public void clicked(InputEvent event, float x, float y) {
//                    tfMoves.setCursorPosition(tfMoves.getText().length());
//                }
//            });
//
//            cbUseTime = new CheckBox("Use Time", skin);
//            cbUseTime.getImageCell().width(cbUseTime.getLabel().getPrefHeight()).height(cbUseTime.getLabel().getPrefHeight()).padRight(15);
//            cbUseTime.getImage().setScaling(Scaling.fill);
//            cbUseTime.setChecked(true);
//
//            tfTime = new TextField("", skin);
//            tfTime.setAlignment(Align.center);
//            tfTime.setMaxLength(3);
//            tfTime.setText("180");
//            tfTime.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
//            tfTime.setTextFieldListener(returnOnNewLineListener);
//            tfTime.addListener(new ClickListener() {
//                @Override
//                public void clicked(InputEvent event, float x, float y) {
//                    tfTime.setCursorPosition(tfTime.getText().length());
//                }
//            });
//
//            cbSpinTheCoreMode = new CheckBox("Spin The Core Mode", skin);
//            cbSpinTheCoreMode.getImageCell().width(cbSpinTheCoreMode.getLabel().getPrefHeight()).height(cbSpinTheCoreMode.getLabel().getPrefHeight()).padRight(15);
//            cbSpinTheCoreMode.getImage().setScaling(Scaling.fill);
//
//            tblCheckboxesWithValues.add(cbUseLives).left().padRight(15);
//            tblCheckboxesWithValues.add(tfLives).center().width(101).padRight(60);
//            tblCheckboxesWithValues.add(cbSpinTheCoreMode);
//            tblCheckboxesWithValues.add().row();
//
//            tblCheckboxesWithValues.add(cbUseMoves).left().padRight(15);
//            tblCheckboxesWithValues.add(tfMoves).center().width(101).padRight(60);
//            tblCheckboxesWithValues.add();
//            tblCheckboxesWithValues.add().row();
//
//            tblCheckboxesWithValues.add(cbUseTime).left().padRight(15);
//            tblCheckboxesWithValues.add(tfTime).center().width(101).padRight(60);
//            tblCheckboxesWithValues.add();
//            tblCheckboxesWithValues.add().row();
//
//            settingsTbl.add(tblCheckboxesWithValues).colspan(settingsTbl.getColumns()).expandX().left();
//
//            //======= Play & Back Buttons =======
//            TextButton tbtn = new TextButton("Back", skin);
//            tbtn.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    stage.clear();
//                    stage.addActor(m_pickGameModeUI.getRoot());
//                }
//            });
//            mainTable.add(tbtn).width(250).height(200).align(Align.left);
//
//            tbtn = new TextButton("Play", skin);
//            tbtn.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    roundSettings.reset();
//
//                    if (tfMoves.getText().isEmpty()) tfMoves.setText("0");
//                    if (tfLives.getText().isEmpty()) tfLives.setText("0");
//                    if (tfTime.getText().isEmpty()) tfTime.setText("0");
//
//                    boolean spinTheCoreEnabled = cbSpinTheCoreMode.isChecked();
//
//                    roundSettings.gameMode = spinTheCoreEnabled ? GameScreen.GameMode.SPIN_THE_CORE : GameScreen.GameMode.CLASSIC;
//                    roundSettings.autoRotationEnabled = spinTheCoreEnabled ? false : true;
//                    roundSettings.autoEjectEnabled = spinTheCoreEnabled ? true : false;
//                    roundSettings.initRadius = (int) radiusSlider.getValue();
//                    roundSettings.minRotationSpeed = minRotSlider.getValue();
//                    roundSettings.maxRotationSpeed = maxRotSlider.getValue();
//                    roundSettings.launcherCooldown = sldrLauncherCooldown.getValue();
//                    roundSettings.ballSpeed = (int) ballSpeedSlider.getValue();
//
//                    roundSettings.isMovesEnabled = cbUseMoves.isChecked();
//                    roundSettings.moveCount = Integer.parseInt(tfMoves.getText());
//                    roundSettings.isTimeEnabled = cbUseTime.isChecked();
//                    roundSettings.timeAmount = Integer.parseInt(tfTime.getText());
//                    roundSettings.isLivesEnabled = cbUseLives.isChecked();
//                    roundSettings.livesAmount = Integer.parseInt(tfLives.getText());
//
//                    roundBuilder.buildRound(roundSettings);
//                    m_game.setScreen(gameScreen);
//                }
//            });
//            mainTable.add(tbtn).width(250).height(200).align(Align.right);
//
//            root = mainTable;
//        }
//
//        private void attachSliderToTable(String name, Slider slider, Label amountLbl, Table tbl) {
//            Label dummy = new Label(name + ":", skin, "comic_48b");
//            tbl.add(dummy).padRight(30).align(Align.right).padBottom(5);
//            tbl.add(amountLbl).align(Align.left).padBottom(5).expandX().row();
//            tbl.add(slider).colspan(2).fill().expandX().row();
//        }
//    }
//
//    private class BackButtonInputHandler extends InputAdapter {
//        @Override
//        public boolean keyDown(int keycode) {
//            if (keycode == Input.Keys.BACK) {
//                if (m_pickGameModeUI.getRoot().getStage() == null) {
//                    stage.clear();
//                    stage.addActor(m_pickGameModeUI.getRoot());
//                } else {
//                    m_game.setPrevScreen();
//                }
//                return false;
//            }
//            return false;
//        }
//    }
//
//    private class SpinTheCoreSettingsUI extends UIBase {
//        Slider m_stcRadiusSlider, m_stcBallSpeedSlider, m_stcLauncherCDSlider;
//        Label m_stcRadiusLabel, m_stcBallSpeedLabel, m_stcLauncherCDLabel;
//
//        public SpinTheCoreSettingsUI() {
//            Table mt = new Table();
//
//            mt.setFillParent(true);
//            mt.setDebug(false);
//
//            Label dummy = new Label(": Game Setup :", skin, "comic_96b");
//
//            mt.top().pad(100);
//            mt.add(dummy).pad(100).padBottom(400).colspan(2).row();
//
//
//            m_stcRadiusSlider = new Slider(1, 8, 1, false, skin);
//            m_stcRadiusSlider.setValue(4);
//            m_stcRadiusLabel = new Label(String.valueOf((int) m_stcRadiusSlider.getValue()), skin, "comic_48");
//            m_stcRadiusSlider.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    m_stcRadiusLabel.setText(String.valueOf((int) m_stcRadiusSlider.getValue()));
//                }
//            });
//            dummy = new Label("Circle Radius:", skin, "comic_48b");
//            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
//            mt.add(m_stcRadiusLabel).width(m_stcRadiusLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
//            mt.add(m_stcRadiusSlider).colspan(2).fill().expandX().padBottom(100).row();
//
//
//            m_stcBallSpeedSlider = new Slider(2, 15, 1, false, skin);
//            m_stcBallSpeedSlider.setValue(4);
//            m_stcBallSpeedLabel = new Label(String.valueOf((int) m_stcBallSpeedSlider.getValue()), skin, "comic_48");
//            m_stcBallSpeedSlider.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    m_stcBallSpeedLabel.setText(String.valueOf((int) m_stcBallSpeedSlider.getValue()));
//                }
//            });
//            dummy = new Label("Ball Speed:", skin, "comic_48b");
//            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
//            mt.add(m_stcBallSpeedLabel).width(m_stcBallSpeedLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
//            mt.add(m_stcBallSpeedSlider).colspan(2).fill().expandX().padBottom(100).row();
//
//
//            m_stcLauncherCDSlider = new Slider(1f, 4, .2f, false, skin);
//            m_stcLauncherCDSlider.setValue(1.8f);
//            m_stcLauncherCDLabel = new Label(String.format("%.2f sec", m_stcLauncherCDSlider.getValue()), skin, "comic_48");
//            m_stcLauncherCDSlider.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    m_stcLauncherCDLabel.setText(String.format("%.2f sec", m_stcLauncherCDSlider.getValue()));
//                }
//            });
//            dummy = new Label("Launcher Cooldown:", skin, "comic_48b");
//            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
//            mt.add(m_stcLauncherCDLabel).width(m_stcLauncherCDLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
//            mt.add(m_stcLauncherCDSlider).colspan(2).fill().expandX().padBottom(100).fill().row();
//
//            TextButton tbtn = new TextButton("Back", skin);
//            tbtn.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    stage.clear();
//                    stage.addActor(m_pickGameModeUI.getRoot());
//                }
//            });
//            mt.add(tbtn).width(250).height(200).align(Align.left);
//
//            tbtn = new TextButton("Play", skin);
//            tbtn.addListener(new ChangeListener() {
//                @Override
//                public void changed(ChangeEvent event, Actor actor) {
//                    roundSettings.initRadius = (int) m_stcRadiusSlider.getValue();
//                    roundSettings.ballSpeed = (int) m_stcBallSpeedSlider.getValue();
//                    roundSettings.launcherCooldown = m_stcLauncherCDSlider.getValue();
//
//                    roundBuilder.buildRound(roundSettings);
//                    m_game.setScreen(gameScreen);
//                }
//            });
//            mt.add(tbtn).colspan(2).width(250).height(200).align(Align.right);
//
//            root = mt;
//        }
//    }
//
//    private class PickGameModeUI extends UIBase {
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
//                    roundSettings.gameMode = GameScreen.GameMode.CLASSIC;
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
//                    m_game.setScreen(m_campaignScreen);
//                }
//            });
//
//            tbl.add(dummy).row();
//            tbl.add(tb).expandX().height(blockHeight).fill().padBottom(blockPad).row();
//
//            root = tbl;
//        }
//    }
//}
