package com.breakthecore.screens;

import com.badlogic.gdx.InputProcessor;
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

    private Slider m_slider1, m_slider2, m_slider3;
    private Label m_label1, m_label2, m_label3;

    public GameSettingsScreen(BreakTheCoreGame game) {
        m_game = game;
        m_skin = game.getSkin();
        m_stage = new Stage(game.getWorldViewport());
        m_classicTable = createClassicTable();
        m_spinTheCoreTable = createSpinTheCoreTable();
        m_pickGameModeTable = createPickGameModeTable();
        m_gameSettings = new GameScreen.GameSettings();
        m_gameScreen = new GameScreen(m_game);

        m_stage.addActor(m_pickGameModeTable);
    }

    @Override
    public InputProcessor getScreenInputProcessor() {
        return m_stage;
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

        Label dummy = new Label(": Game Setup :", m_skin, "comic1_96b");

        m_slider1 = new Slider(1, 8, 1, false, m_skin);
        m_label1 = new Label(String.valueOf((int) m_slider1.getValue()), m_skin, "comic1_48");
        m_slider1.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_label1.setText(String.valueOf((int) m_slider1.getValue()));
            }
        });

        mt.top().pad(100);
        mt.add(dummy).pad(100).padBottom(400).colspan(2).row();

        dummy = new Label("Circle Radius:", m_skin, "comic1_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_label1).width(m_label1.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_slider1).colspan(2).fill().expandX().padBottom(100).row();

        m_slider2 = new Slider(20, 150, 1, false, m_skin);
        m_label2 = new Label(String.valueOf((int) m_slider2.getValue()), m_skin, "comic1_48");
        m_slider2.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int slider2 = (int) m_slider2.getValue();
                int slider3 = (int) m_slider3.getValue();

                if (slider2 > slider3) {
                    m_slider3.setValue(slider2);
                }

                m_label2.setText(String.valueOf(slider2));
            }
        });

        dummy = new Label("Min Rotation Speed:", m_skin, "comic1_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_label2).width(m_label2.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_slider2).colspan(2).fill().expandX().padBottom(100).row();


        m_slider3 = new Slider(20, 150, 1, false, m_skin);
        m_label3 = new Label(String.valueOf((int) m_slider3.getValue()), m_skin, "comic1_48");
        m_slider3.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int slider2 = (int) m_slider2.getValue();
                int slider3 = (int) m_slider3.getValue();

                if (slider2 > slider3) {
                    m_slider2.setValue(slider3);
                }

                m_label3.setText(String.valueOf((int) m_slider3.getValue()));
            }
        });

        dummy = new Label("Max Rotation Speed:", m_skin, "comic1_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_label3).width(m_label3.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_slider3).colspan(2).fill().expandX().padBottom(100).height(100).fill().row();


        TextButton playBtn = new TextButton("Play", m_skin);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_gameSettings.initRadius = (int) m_slider1.getValue();
                m_gameSettings.minRotationSpeed = (int) m_slider2.getValue();
                m_gameSettings.maxRotationSpeed = (int) m_slider3.getValue();

                m_gameScreen.initializeGameScreen(m_gameSettings);
                m_game.setScreen(m_gameScreen);
            }
        });
        mt.add(playBtn).colspan(2).width(200).height(150).align(Align.right);

        return mt;
    }

    private Table createSpinTheCoreTable() {
        Table mt = new Table();
        mt.setFillParent(true);
        mt.setDebug(false);

        Label dummy = new Label(": Game Setup :", m_skin, "comic1_96b");

        mt.top().pad(100);
        mt.add(dummy).pad(100).padBottom(400).colspan(2).row();


        m_slider1 = new Slider(1, 8, 1, false, m_skin);
        m_label1 = new Label(String.valueOf((int) m_slider1.getValue()), m_skin, "comic1_48");
        m_slider1.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_label1.setText(String.valueOf((int) m_slider1.getValue()));
            }
        });
        dummy = new Label("Circle Radius:", m_skin, "comic1_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_label1).width(m_label1.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_slider1).colspan(2).fill().expandX().padBottom(100).row();


        m_slider2 = new Slider(3, 16, 1, false, m_skin);
        m_label2 = new Label(String.valueOf((int) m_slider2.getValue()), m_skin, "comic1_48");
        m_slider2.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_label2.setText(String.valueOf((int) m_slider2.getValue()));
            }
        });
        dummy = new Label("Ball Speed:", m_skin, "comic1_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_label2).width(m_label2.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_slider2).colspan(2).fill().expandX().padBottom(100).row();


        m_slider3 = new Slider(.4f, 3, .2f, false, m_skin);
        m_label3 = new Label(String.format("%.2f sec", m_slider3.getValue()), m_skin, "comic1_48");
        m_slider3.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_label3.setText(String.format("%.2f sec", m_slider3.getValue()));
            }
        });
        dummy = new Label("Launcher Cooldown:", m_skin, "comic1_48b");
        mt.add(dummy).padRight(30).align(Align.right).padBottom(10).uniformX();
        mt.add(m_label3).width(m_label3.getPrefWidth()).align(Align.left).padBottom(10).uniformX().row();
        mt.add(m_slider3).colspan(2).fill().expandX().padBottom(100).height(100).fill().row();


        TextButton playBtn = new TextButton("Play", m_skin);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_gameSettings.initRadius = (int) m_slider1.getValue();
                m_gameSettings.movingTileSpeed = (int) m_slider2.getValue();
                m_gameSettings.launcherCooldown = m_slider3.getValue();

                m_gameScreen.initializeGameScreen(m_gameSettings);
                m_game.setScreen(m_gameScreen);
            }
        });
        mt.add(playBtn).colspan(2).width(200).height(150).align(Align.right);

        return mt;
    }

    private Table createPickGameModeTable() {
        Table tbl = new Table();
        tbl.setFillParent(true);
        tbl.pad(100);

        String txt = "The core is spinning and your goal is to shoot balls of the right color in order to match-3" +
                " and beat it.";
        Label dummy = new Label(":Classic Mode:", m_skin, "comic1_48b");
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


        txt = "You spin the core while balls get launched at you. Your goal is to match-3 the right colors " +
                "in order to beat it.";
        dummy = new Label(":Spin The Core Mode:", m_skin, "comic1_48b");
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


        txt = "Shoot the core as fast as you can while you keep matching balls of the same color.";
        dummy = new Label(":Shoot'Em Up Mode:", m_skin, "comic1_48b");
        tb = new TextButton(txt, m_skin, "modeButton");
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

        return tbl;
    }
}
