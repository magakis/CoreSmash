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

public class GameSettingsScreen extends ScreenBase {
    private BreakTheCoreGame m_game;
    private Stage m_stage;
    private Skin m_skin;
    private Table m_pickGameModeTable;
    private Table m_classicTable;
    private Table m_spinTheCoreTable;
    private GameScreen.GameSettings m_gameSettings;
    private GameScreen m_gameScreen;
    private CampaignScreen m_campaignScreen;

    private Slider m_cRadiusSlider, m_cMinRotSlider, m_cMaxRotSlider;
    private Label m_cRadiusLabel, m_cMinRotLabel, m_cMaxRotLabel;

    private Slider m_stcRadiusSlider, m_stcBallSpeedSlider, m_stcLauncherCDSlider;
    private Label m_stcRadiusLabel, m_stcBallSpeedLabel, m_stcLauncherCDLabel;

    public GameSettingsScreen(BreakTheCoreGame game) {
        m_game = game;
        m_skin = game.getSkin();
        m_stage = new Stage(game.getWorldViewport());
        m_classicTable = createClassicTable();
        m_spinTheCoreTable = createSpinTheCoreTable();
        m_pickGameModeTable = createPickGameModeTable();
        m_gameSettings = new GameScreen.GameSettings();
        m_gameScreen = new GameScreen(m_game);
        m_campaignScreen = new CampaignScreen(m_game);

        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(m_stage);

        m_stage.addActor(m_pickGameModeTable);
    }

    public void tmpReset() {
        m_stage.clear();
        m_stage.addActor(m_pickGameModeTable);
    }

    @Override
    public void render(float delta) {
        m_stage.act();
        m_stage.draw();
    }

    private Table createClassicTable() {
        Table mt = new Table();

        mt.setFillParent(true);
        mt.setDebug(false);

        Label dummy = new Label(": Game Setup :", m_skin, "comic_96b");
        mt.top().pad(100);
        mt.add(dummy).pad(100).padBottom(400).colspan(2).row();


        m_cRadiusSlider = new Slider(1, 8, 1, false, m_skin);
        m_cRadiusSlider.setValue(4);
        m_cRadiusLabel = new Label(String.valueOf((int) m_cRadiusSlider.getValue()), m_skin, "comic_48");
        m_cRadiusSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_cRadiusLabel.setText(String.valueOf((int) m_cRadiusSlider.getValue()));
            }
        });
        dummy = new Label("Circle Radius:", m_skin, "comic_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_cRadiusLabel).width(m_cRadiusLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_cRadiusSlider).colspan(2).fill().expandX().padBottom(100).row();

        m_cMinRotSlider = new Slider(10, 120, 1, false, m_skin);
        m_cMinRotSlider.setValue(20);
        m_cMinRotLabel = new Label(String.valueOf((int) m_cMinRotSlider.getValue()), m_skin, "comic_48");
        m_cMinRotSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int slider2 = (int) m_cMinRotSlider.getValue();
                int slider3 = (int) m_cMaxRotSlider.getValue();

                if (slider2 > slider3) {
                    m_cMaxRotSlider.setValue(slider2);
                }

                m_cMinRotLabel.setText(String.valueOf(slider2));
            }
        });
        dummy = new Label("Min Rotation Speed:", m_skin, "comic_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_cMinRotLabel).width(m_cMinRotLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_cMinRotSlider).colspan(2).fill().expandX().padBottom(100).row();


        m_cMaxRotSlider = new Slider(10, 120, 1, false, m_skin);
        m_cMaxRotSlider.setValue(70);
        m_cMaxRotLabel = new Label(String.valueOf((int) m_cMaxRotSlider.getValue()), m_skin, "comic_48");
        m_cMaxRotSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int slider2 = (int) m_cMinRotSlider.getValue();
                int slider3 = (int) m_cMaxRotSlider.getValue();

                if (slider2 > slider3) {
                    m_cMinRotSlider.setValue(slider3);
                }

                m_cMaxRotLabel.setText(String.valueOf((int) m_cMaxRotSlider.getValue()));
            }
        });
        dummy = new Label("Max Rotation Speed:", m_skin, "comic_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_cMaxRotLabel).width(m_cMaxRotLabel.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_cMaxRotSlider).colspan(2).fill().expandX().padBottom(100).fill().row();


        TextButton tbtn = new TextButton("Back", m_skin);
        tbtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_stage.clear();
                m_stage.addActor(m_pickGameModeTable);
            }
        });
        mt.add(tbtn).width(250).height(200).align(Align.left);

        tbtn = new TextButton("Play", m_skin);
        tbtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_gameSettings.initRadius = (int) m_cRadiusSlider.getValue();
                m_gameSettings.minRotationSpeed = (int) m_cMinRotSlider.getValue();
                m_gameSettings.maxRotationSpeed = (int) m_cMaxRotSlider.getValue();

                m_gameScreen.initialize(m_gameSettings);
                m_game.setScreen(m_gameScreen);
            }
        });
        mt.add(tbtn).width(250).height(200).align(Align.right);

        return mt;
    }

    private Table createSpinTheCoreTable() {
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


        m_stcLauncherCDSlider = new Slider(.4f, 3, .2f, false, m_skin);
        m_stcLauncherCDSlider.setValue(1.6f);
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
                m_stage.addActor(m_pickGameModeTable);
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

        return mt;
    }

    private Table createPickGameModeTable() {
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
                m_stage.addActor(m_classicTable);
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
                m_stage.addActor(m_spinTheCoreTable);
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

        return tbl;
    }

    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                m_game.setMainMenuScreen();
                return false;
            }
            return false;
        }
    }
}
