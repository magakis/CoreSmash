package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.breakthecore.BreakTheCoreGame;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.breakthecore.RoundEndListener;
import com.breakthecore.WorldSettings;
import com.breakthecore.levels.Level1;
import com.breakthecore.levels.Level2;

public class CampaignScreen extends ScreenBase implements RoundEndListener {
    private GameScreen gameScreen;
    private GestureDetector gd;
    private Skin m_skin;
    private Stage stage;
    private Table tblCampaignMap;
    private LevelButton[] levelButtons;
    private int currentLevel;
    private int activeLevel;

    public CampaignScreen(BreakTheCoreGame game) {
        super(game);
        m_skin = game.getSkin();
        stage = new Stage(game.getWorldViewport());
        gd = new CustomGestureDetector(new InputListener());

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(gd);
        tblCampaignMap = new Table();
        gameScreen = new GameScreen(gameInstance);

        levelButtons = new LevelButton[20];

        WidgetGroup buttonsGroup = createButtonGroup();
        ScrollPane scrollPane = new ScrollPane(buttonsGroup);
        scrollPane.setFillParent(true);
        scrollPane.setOverscroll(false, false);
        scrollPane.validate();
        scrollPane.setSmoothScrolling(false);
        scrollPane.setScrollPercentY(100);

        currentLevel = Gdx.app.getPreferences("highscores").getInteger("campaign_level", 1);
        for (int i = 0; i < currentLevel; ++i) {
            levelButtons[i].enable();
        }

        stage.addActor(scrollPane);
    }

    @Override
    public void render(float delta) {
        stage.act();
        stage.draw();
    }

    private Container<WidgetGroup> createButtonGroup() {
        WidgetGroup grp = new WidgetGroup();

        float x;
        float y;
        for (int i = 0; i < 20; ++i) {
            x = WorldSettings.getWorldWidth() / 2 + (WorldSettings.getWorldWidth() / 3) * (float) Math.cos(i * Math.PI / 2);
            y = 200 + i * 190;
            levelButtons[i] = new LevelButton(i + 1, (int) x, (int) y);
            grp.addActor(levelButtons[i]);
        }

        Container<WidgetGroup> container = new Container<WidgetGroup>(grp);
        container.prefSize(WorldSettings.getWorldWidth(), 200+20*190);

        return container;
    }

    private void startCampaignLevel(int lvl) {
        activeLevel = lvl;
        switch (lvl) {
            case 1:
                gameScreen.deployLevel(new Level1(this));
                break;
            case 2:
                gameScreen.deployLevel(new Level2(this));
            default:
                return;
        }

    }

    @Override
    public void onRoundEnded(boolean result) {
        // Round WON
        if (result) {
            Preferences prefs = Gdx.app.getPreferences("highscores");
            if (currentLevel == activeLevel) {
                prefs.putInteger("campaign_level", currentLevel+1);
                prefs.flush();
                levelButtons[currentLevel].enable();
                ++currentLevel;
            }
        }
        //Round LOST
    }

    private class InputListener implements GestureDetector.GestureListener {

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return false;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }

        @Override
        public void pinchStop() {

        }
    }

    /** Why do I have a GestureDetector in Campaign Screen?!? */
    private class CustomGestureDetector extends GestureDetector {
        public CustomGestureDetector(GestureListener listener) {
            super(listener);
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                gameInstance.setPrevScreen();
                return false;
            }
            return false;
        }
    }

    private class LevelLauncher extends ChangeListener {
        private int m_lvl;

        public LevelLauncher(int lvl) {
            m_lvl = lvl;
        }

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            startCampaignLevel(m_lvl);
        }
    }

    private class LevelButton extends TextButton {
        private int m_level;

        public LevelButton(int lvl, int x, int y) {
            super(String.valueOf(lvl), m_skin, "levelBtnDisabled");
            setSize(160, 160);
            setPosition(x - 80, y - 80);
            addListener(new LevelLauncher(lvl));
            m_level = lvl;
            setDisabled(true);
        }

        public int getLevel() {
            return m_level;
        }

        public void enable() {
            setDisabled(false);
            setStyle(m_skin.get("levelBtnEnabled", TextButtonStyle.class));
        }
    }
}
