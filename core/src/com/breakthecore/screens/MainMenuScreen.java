package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.breakthecore.BreakTheCoreGame;

/**
 * Created by Michail on 16/3/2018.
 */

public class MainMenuScreen extends ScreenAdapter {
    Pixmap hmm = null;
    private Stage m_stage;
    Texture hmmT = null;
    private BreakTheCoreGame m_game;
    private Table debugTable, menuTable, optionsTable;
    private Skin m_skin;
    private Label dblbl;

    public MainMenuScreen(BreakTheCoreGame game) {
        m_game = game;
        m_stage = new Stage(new ScreenViewport());
        setupMainMenuStage(m_stage);
        game.addInputHandler(m_stage);

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
        stage.clear(); // start with empty stage

        m_skin = createSkin();

        dblbl = new Label("null", m_skin, "default", Color.GOLD);
        setupTables();


        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        rootStack.add(menuTable);
        rootStack.add(debugTable);

        stage.addActor(rootStack);

    }

    private void setupTables() {
        setupMenuTable();
        setupDebugTable();
    }

    private void setupMenuTable() {
        menuTable = new Table();
        menuTable.setDebug(false);

        Container playBtn = newMenuButton("Play", "playBtn", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dblbl.setText("Clicked Play");
                if (true) {
                    m_game.removeInputHandler(m_stage);
                    m_game.setScreen(new GameScreen(m_game));
                }
            }
        });

        Container scoresBtn = newMenuButton("Scores", "scoresBtn", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dblbl.setText("Clicked Scores Button");
            }
        });


        menuTable.bottom();
        menuTable.padBottom(Value.percentHeight(3 / 16f, menuTable));
        playBtn.padBottom(Value.percentHeight(1 / 32f, menuTable));
        menuTable.add(playBtn).row();
        menuTable.add(scoresBtn);

    }

    private void setupDebugTable() {
        debugTable = new Table();
        debugTable.setDebug(false);
        debugTable.top().left();
        debugTable.add(dblbl).row();

    }

    private Skin createSkin() {
        Skin skin = new Skin();

        skin.add("default", new BitmapFont());

        int width = (int) (Gdx.graphics.getWidth() * .60f);
        int height = (int) (Gdx.graphics.getHeight() * 1 / 8f);
        Pixmap buttonSkin = new Pixmap(width, height, Pixmap.Format.RGB888);
        buttonSkin.setColor(Color.WHITE);
        buttonSkin.fill();
        buttonSkin.setColor(Color.BLACK);
        buttonSkin.fillRectangle(10, 10, width - 20, height - 20);
        Texture text = new Texture(buttonSkin);

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        skin.add("white", new Texture(pix));
        skin.add("text", text);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.newDrawable("text", Color.WHITE);
        btnStyle.down = skin.newDrawable("text", Color.GRAY);
        btnStyle.checked = btnStyle.up;
        btnStyle.font = skin.getFont("default");
        skin.add("default", btnStyle);

        return skin;
    }

    public Container newMenuButton(String text, String name, EventListener el) {
        TextButton bt = new TextButton(text, m_skin);
        bt.setName(name);
        bt.getLabel().setFontScale(6);
        bt.getLabel().setColor(Color.WHITE);
        bt.addListener(el);

        Container result = new Container(bt);
        result.setTransform(true);
        result.setOrigin(bt.getWidth() / 2, bt.getHeight() / 2);
//        result.setRotation(-1.25f);
//        result.addAction(Actions.forever(Actions.sequence(Actions.rotateBy(2.5f, 1.5f, Interpolation.smoother), Actions.rotateBy(-2.5f, 1.5f, Interpolation.smoother))));
        result.addAction(Actions.forever(Actions.sequence(Actions.scaleBy(.015f, .015f, 0.75f), Actions.scaleBy(-.015f, -.015f, 0.75f))));
        return result;
    }
}
