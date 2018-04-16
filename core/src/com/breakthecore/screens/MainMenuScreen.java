package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.WorldSettings;

/**
 * Created by Michail on 16/3/2018.
 */

public class MainMenuScreen extends ScreenBase {
    private GameSettingsScreen m_gameSettingsScreen;
    private Stage stage;
    private Table menuTable;
    private Skin skin;


    public MainMenuScreen(BreakTheCoreGame game) {
        super(game);
        stage = new Stage(game.getWorldViewport());
        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(stage);
        setupMainMenuStage(stage);
        m_gameSettingsScreen = new GameSettingsScreen(game);
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
        stage.clear(); // start with empty stage

        skin = m_game.getSkin();

        setupMenuTable();

        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        rootStack.add(menuTable);

        stage.addActor(rootStack);
    }

    private void setupMenuTable() {
        menuTable = new Table();

        Container btnPlay = newMenuButton("Play", "btnPlay", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (checkForLocalAccount()) {
                    m_gameSettingsScreen.tmpReset();
                    m_game.setScreen(m_gameSettingsScreen);
                }
            }
        });

        Container btnAccount = newMenuButton("Account", "btnAccount", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        menuTable.defaults()
                .width(WorldSettings.getWorldWidth() * 3 / 5)
                .height(WorldSettings.getWorldHeight() * 2 / 16)
                .fill();

        Label versInfo = new Label("v.1.0.0 - Michail Angelos Gakis", skin, "comic_24b", Color.DARK_GRAY);
        versInfo.setAlignment(Align.bottom);
        menuTable.bottom();
        menuTable.add(btnPlay).padBottom(Value.percentHeight(1 / 16f, menuTable)).row();
        menuTable.add(btnAccount).padBottom(Value.percentHeight(3 / 16f, menuTable)).row();
        menuTable.add(versInfo).align(Align.center).height(versInfo.getPrefHeight());
        //        menuTable.add(scoresBtn);
    }

    public boolean checkForLocalAccount() {
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

    public Container newMenuButton(String text, String name, EventListener el) {
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

    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                Gdx.app.exit();
                return false;
            }
            return false;
        }
    }
}
