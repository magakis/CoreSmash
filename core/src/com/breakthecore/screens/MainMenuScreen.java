package com.breakthecore.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by Michail on 16/3/2018.
 */

public class MainMenuScreen extends ScreenAdapter{
    private Game m_game;
    private Stage m_stage;

    private Label lbl;

    public MainMenuScreen(Game game) {
        m_game = game;
        m_stage = new Stage(new ScreenViewport());
        setupMainMenuStage(m_stage);
        Gdx.input.setInputProcessor(m_stage);

    }
    @Override
    public void render(float delta) {
        m_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        m_stage.getViewport().update(width, height);
    }

    private void setupMainMenuStage(Stage stage) {
        stage.clear(); // start with empty stage

        Skin skin = new Skin();
        skin.add("default", new BitmapFont());

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        skin.add("white", new Texture(pix));

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.newDrawable("white", Color.CORAL);
        btnStyle.down = skin.newDrawable("white", Color.WHITE);
        btnStyle.checked = skin.newDrawable("white", Color.GREEN);
        btnStyle.font = skin.getFont("default");
        skin.add("default", btnStyle);

        Table debugTable = new Table();
        debugTable.setDebug(false);
        debugTable.top().left();

        lbl = new Label("null", skin, "default", Color.GOLD);
        debugTable.add(lbl).row();

        Table menuTable = new Table();
        menuTable.setDebug(false);

        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        rootStack.add(menuTable);
        rootStack.add(debugTable);

        stage.addActor(rootStack);

        TextButton button = new TextButton("Play",skin);
        button.setName("playBtn");
        button.getLabel().setFontScale(6);
        button.getLabel().setColor(Color.FIREBRICK);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                m_game.setScreen(new GameScreen(m_game));
            }
        });

        menuTable.bottom();
        menuTable.add(button)
                .size(Value.percentWidth(.60f,menuTable), Value.percentHeight(1/8f,menuTable))
                .padBottom(Value.percentHeight(1/8f,menuTable));

        menuTable.pad(30);
    }
}
