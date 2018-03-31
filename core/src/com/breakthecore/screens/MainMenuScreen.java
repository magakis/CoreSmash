package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.WorldSettings;

/**
 * Created by Michail on 16/3/2018.
 */

public class MainMenuScreen extends ScreenBase {
    private BreakTheCoreGame m_game;
    private GameSettingsScreen m_gameSettingsScreen;
    private InputMultiplexer m_inputMultiplexer;
    private Stage m_stage;
    private Table menuTable;
    private Skin m_skin;
    private Label dblbl;

    public MainMenuScreen(BreakTheCoreGame game) {
        m_game = game;
        m_stage = new Stage(game.getWorldViewport());
        m_inputMultiplexer = new InputMultiplexer(new BackButtonInputHandler(), m_stage);
        setupMainMenuStage(m_stage);
        m_gameSettingsScreen = new GameSettingsScreen(game);
    }

    @Override
    public void render(float delta) {
        m_stage.act(delta);
        m_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        m_stage.getViewport().update(width, height);
    }

    private void setupMainMenuStage(Stage stage) {
        stage.clear(); // start with empty m_stage

        m_skin = m_game.getSkin();

        dblbl = new Label("null", m_skin, "comic1_24b", Color.GOLD);

        setupMenuTable();

        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        rootStack.add(menuTable);

        stage.addActor(rootStack);
    }

    private void setupMenuTable() {
        menuTable = new Table();

        Container playBtn = newMenuButton("Play", "playBtn", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                m_gameSettingsScreen.tmpReset();
                m_game.setScreen(m_gameSettingsScreen);
            }
        });

//        Container scoresBtn = newMenuButton("Scores", "scoresBtn", new ChangeListener() {
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                dblbl.setText("Clicked Scores Button");
//            }
//        });

        menuTable.defaults()
                .width(WorldSettings.getWorldWidth() * 3 / 5)
                .height(WorldSettings.getWorldHeight() * 2 / 16)
                .fill();

        menuTable.bottom();
        menuTable.padBottom(Value.percentHeight(3 / 16f, menuTable));
        menuTable.add(playBtn).padBottom(Value.percentHeight(1 / 32f, menuTable)).row();
//        menuTable.add(scoresBtn);
    }

    public InputProcessor getScreenInputProcessor() {
        return m_inputMultiplexer;
    }

    public Container newMenuButton(String text, String name, EventListener el) {
        TextButton bt = new TextButton(text, m_skin.get("menuButton", TextButton.TextButtonStyle.class));
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
