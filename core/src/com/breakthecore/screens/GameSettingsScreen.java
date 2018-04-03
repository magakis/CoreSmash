package com.breakthecore.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.ui.UIBase;

public class GameSettingsScreen extends ScreenBase {
    private Stage m_stage;
    private Skin m_skin;
    private PickGameModeUI m_pickGameModeUI;
    private ClassicSettingsUI m_classicSettingsUI;
    private SpinTheCoreSettingsUI m_spinTheCoreSettingsUI;
    private GameScreen.GameSettings m_gameSettings;
    private GameScreen m_gameScreen;
    private CampaignScreen m_campaignScreen;

    public GameSettingsScreen(BreakTheCoreGame game) {
        super(game);
        m_skin = game.getSkin();
        m_stage = new Stage(game.getWorldViewport());
        m_classicSettingsUI = new ClassicSettingsUI();
        m_spinTheCoreSettingsUI = new SpinTheCoreSettingsUI();
        m_pickGameModeUI = new PickGameModeUI();
        m_gameSettings = new GameScreen.GameSettings();

        m_gameScreen = new GameScreen(m_game);
        m_campaignScreen = new CampaignScreen(m_game);

        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(m_stage);

        m_stage.addActor(m_pickGameModeUI.getRoot());
    }

    public void tmpReset() {
        m_stage.clear();
        m_stage.addActor(m_pickGameModeUI.getRoot());
    }

    @Override
    public void render(float delta) {
        m_stage.act();
        m_stage.draw();
    }

    private class ClassicSettingsUI extends UIBase {
        Slider radiusSlider, minRotSlider, maxRotSlider, ballSpeedSlider;
        Label radiusLbl, minRotLbl, maxRotLbl, ballSpeedLbl;

        public ClassicSettingsUI() {
            Table mt = new Table();

            mt.setFillParent(true);
            mt.setDebug(false);

            Label dummy = new Label(": Game Setup :", m_skin, "comic_96b");
            mt.top().pad(100);
            mt.add(dummy).pad(100).padBottom(200).colspan(2).row();


            radiusSlider = new Slider(1, 8, 1, false, m_skin);
            radiusSlider.setValue(4);
            radiusLbl = new Label(String.valueOf((int) radiusSlider.getValue()), m_skin, "comic_48");
            radiusSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    radiusLbl.setText(String.valueOf((int) radiusSlider.getValue()));
                }
            });
            dummy = new Label("Circle Radius:", m_skin, "comic_48b");
            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
            mt.add(radiusLbl).width(radiusLbl.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
            mt.add(radiusSlider).colspan(2).fill().expandX().padBottom(100).row();

            minRotSlider = new Slider(10, 120, 1, false, m_skin);
            minRotSlider.setValue(30);
            minRotLbl = new Label(String.valueOf((int) minRotSlider.getValue()), m_skin, "comic_48");
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
            dummy = new Label("Min Rotation Speed:", m_skin, "comic_48b");
            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
            mt.add(minRotLbl).width(minRotLbl.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
            mt.add(minRotSlider).colspan(2).fill().expandX().padBottom(100).row();


            maxRotSlider = new Slider(10, 120, 1, false, m_skin);
            maxRotSlider.setValue(70);
            maxRotLbl = new Label(String.valueOf((int) maxRotSlider.getValue()), m_skin, "comic_48");
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
            dummy = new Label("Max Rotation Speed:", m_skin, "comic_48b");
            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
            mt.add(maxRotLbl).width(maxRotLbl.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
            mt.add(maxRotSlider).colspan(2).fill().expandX().padBottom(100).fill().row();

            ballSpeedSlider = new Slider(5, 20, 1, false, m_skin);
            ballSpeedSlider.setValue(15);
            ballSpeedLbl = new Label(String.valueOf((int) ballSpeedSlider.getValue()), m_skin, "comic_48");
            ballSpeedSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    ballSpeedLbl.setText(String.valueOf((int) ballSpeedSlider.getValue()));
                }
            });
            dummy = new Label("Ball Speed:", m_skin, "comic_48b");
            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
            mt.add(ballSpeedLbl).width(ballSpeedLbl.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
            mt.add(ballSpeedSlider).colspan(2).fill().expandX().padBottom(100).fill().row();

            TextButton tbtn = new TextButton("Back", m_skin);
            tbtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_stage.clear();
                    m_stage.addActor(m_pickGameModeUI.getRoot());
                }
            });
            mt.add(tbtn).width(250).height(200).align(Align.left);

            tbtn = new TextButton("Play", m_skin);
            tbtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_gameSettings.initRadius = (int) radiusSlider.getValue();
                    m_gameSettings.minRotationSpeed = (int) minRotSlider.getValue();
                    m_gameSettings.maxRotationSpeed = (int) maxRotSlider.getValue();
                    m_gameSettings.movingTileSpeed = (int) ballSpeedSlider.getValue();

                    m_gameScreen.initialize(m_gameSettings);
                    m_game.setScreen(m_gameScreen);
                }
            });
            mt.add(tbtn).width(250).height(200).align(Align.right);

            root = mt;
        }
    }

    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                if (m_pickGameModeUI.getRoot().getStage() == null) {
                    m_stage.clear();
                    m_stage.addActor(m_pickGameModeUI.getRoot());
                } else {
                    m_game.setPrevScreen();
                }
                return false;
            }
            return false;
        }
    }

    private class SpinTheCoreSettingsUI extends UIBase {
        Slider m_stcRadiusSlider, m_stcBallSpeedSlider, m_stcLauncherCDSlider;
        Label m_stcRadiusLabel, m_stcBallSpeedLabel, m_stcLauncherCDLabel;

        public SpinTheCoreSettingsUI() {
            Table mt = new Table();

            mt.setFillParent(true);
            mt.setDebug(false);

            Label dummy = new Label(": Game Setup :", m_skin, "comic_96b");

            mt.top().pad(100);
            mt.add(dummy).pad(100).padBottom(400).colspan(2).row();


            m_stcRadiusSlider = new Slider(1, 8, 1, false, m_skin);
            m_stcRadiusSlider.setValue(4);
            m_stcRadiusLabel = new Label(String.valueOf((int) m_stcRadiusSlider.getValue()), m_skin, "comic_48");
            m_stcRadiusSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_stcRadiusLabel.setText(String.valueOf((int) m_stcRadiusSlider.getValue()));
                }
            });
            dummy = new Label("Circle Radius:", m_skin, "comic_48b");
            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
            mt.add(m_stcRadiusLabel).width(m_stcRadiusLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
            mt.add(m_stcRadiusSlider).colspan(2).fill().expandX().padBottom(100).row();


            m_stcBallSpeedSlider = new Slider(2, 15, 1, false, m_skin);
            m_stcBallSpeedSlider.setValue(4);
            m_stcBallSpeedLabel = new Label(String.valueOf((int) m_stcBallSpeedSlider.getValue()), m_skin, "comic_48");
            m_stcBallSpeedSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_stcBallSpeedLabel.setText(String.valueOf((int) m_stcBallSpeedSlider.getValue()));
                }
            });
            dummy = new Label("Ball Speed:", m_skin, "comic_48b");
            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
            mt.add(m_stcBallSpeedLabel).width(m_stcBallSpeedLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
            mt.add(m_stcBallSpeedSlider).colspan(2).fill().expandX().padBottom(100).row();


            m_stcLauncherCDSlider = new Slider(1f, 4, .2f, false, m_skin);
            m_stcLauncherCDSlider.setValue(1.8f);
            m_stcLauncherCDLabel = new Label(String.format("%.2f sec", m_stcLauncherCDSlider.getValue()), m_skin, "comic_48");
            m_stcLauncherCDSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_stcLauncherCDLabel.setText(String.format("%.2f sec", m_stcLauncherCDSlider.getValue()));
                }
            });
            dummy = new Label("Launcher Cooldown:", m_skin, "comic_48b");
            mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
            mt.add(m_stcLauncherCDLabel).width(m_stcLauncherCDLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
            mt.add(m_stcLauncherCDSlider).colspan(2).fill().expandX().padBottom(100).fill().row();

            TextButton tbtn = new TextButton("Back", m_skin);
            tbtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_stage.clear();
                    m_stage.addActor(m_pickGameModeUI.getRoot());
                }
            });
            mt.add(tbtn).width(250).height(200).align(Align.left);

            tbtn = new TextButton("Play", m_skin);
            tbtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_gameSettings.initRadius = (int) m_stcRadiusSlider.getValue();
                    m_gameSettings.movingTileSpeed = (int) m_stcBallSpeedSlider.getValue();
                    m_gameSettings.launcherCooldown = m_stcLauncherCDSlider.getValue();

                    m_gameScreen.initialize(m_gameSettings);
                    m_game.setScreen(m_gameScreen);
                }
            });
            mt.add(tbtn).colspan(2).width(250).height(200).align(Align.right);

            root = mt;
        }
    }

    private class PickGameModeUI extends UIBase {
        public PickGameModeUI() {
            Table tbl = new Table();
            Label dummy;
            tbl.setFillParent(true);
            tbl.pad(100);

            dummy = new Label("Game Mode", m_skin, "comic_96b");
            tbl.add(dummy).padBottom(100).row();

            String txt = "The core is spinning and your goal is to match balls of the same color " +
                    "in order to beat it.";
            dummy = new Label(":Classic Mode:", m_skin, "comic_48b");
            TextButton tb = new TextButton(txt, m_skin, "modeButton");
            tb.getLabel().setWrap(true);
            tb.getLabelCell().pad(10);
            tb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_stage.clear();
                    m_stage.addActor(m_classicSettingsUI.getRoot());
                    m_gameSettings.gameMode = GameScreen.GameMode.CLASSIC;
                }
            });

            tbl.add(dummy).row();
            tbl.add(tb).expandX().height(250).fill().padBottom(200).row();


            txt = "You spin the core while balls get launched at you. Your goal is to match-3 of the right colors " +
                    "in order to beat it.";
            dummy = new Label(":Spin The Core Mode:", m_skin, "comic_48b");
            tb = new TextButton(txt, m_skin, "modeButton");
            tb.getLabel().setWrap(true);
            tb.getLabelCell().pad(10);
            tb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_stage.clear();
                    m_stage.addActor(m_spinTheCoreSettingsUI.getRoot());
                    m_gameSettings.gameMode = GameScreen.GameMode.SPIN_THE_CORE;
                }
            });

            tbl.add(dummy).row();
            tbl.add(tb).expandX().height(250).fill().padBottom(200).row();


            txt = "Expiremental campaign mode..";
            dummy = new Label(":Campaign:", m_skin, "comic_48b");
            tb = new TextButton(txt, m_skin, "modeButton");
            tb.getLabel().setWrap(true);
            tb.getLabelCell().pad(10);
            tb.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    m_game.setScreen(m_campaignScreen);
                }
            });

            tbl.add(dummy).row();
            tbl.add(tb).expandX().height(250).fill().padBottom(200).row();

            root = tbl;
        }
    }
}
