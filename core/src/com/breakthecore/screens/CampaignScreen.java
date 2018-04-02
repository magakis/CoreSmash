package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.breakthecore.BreakTheCoreGame;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.breakthecore.RoundEndListener;
import com.breakthecore.WorldSettings;

public class CampaignScreen extends ScreenBase implements RoundEndListener {
    GameScreen m_gameScreen;
    GameScreen.GameSettings m_settings;
    GestureDetector gd;
    Skin m_skin;
    Stage m_stage;

    LevelButton[] m_levelButtons;
    int nextLevel;
    int activeLevel;

    public CampaignScreen(BreakTheCoreGame game) {
        super(game);
        m_skin = game.getSkin();
        m_stage = new Stage(game.getWorldViewport());
        gd = new CustomGestureDetector(new InputListener());
        screenInputMultiplexer.addProcessor(m_stage);
        screenInputMultiplexer.addProcessor(gd);

        m_gameScreen = new GameScreen(m_game);
        m_settings = new GameScreen.GameSettings();

        m_levelButtons = new LevelButton[20];

        Group buttonsGroup = createButtonGroup();

        //Enable levels that can be played
        nextLevel = Gdx.app.getPreferences("highscores").getInteger("campaign_level", 0) + 1;
        for (int i = 0; i < nextLevel; ++i) {
            m_levelButtons[i].enable();
        }


        m_stage.addActor(buttonsGroup);
    }

    @Override
    public void render(float delta) {
        m_stage.act();
        m_stage.draw();
    }

    private Group createButtonGroup() {
        Group grp = new Group();

        float x;
        float y;
        for (int i = 0; i < 10; ++i) {
            x = WorldSettings.getWorldWidth() / 2 + (WorldSettings.getWorldWidth() / 3) * (float) Math.cos(i * Math.PI / 2);
            y = 100 + i * 190;
            m_levelButtons[i] = new LevelButton(i + 1, (int) x, (int) y);
            grp.addActor(m_levelButtons[i]);
        }
        return grp;
    }

    private void startCampaignLevel(int lvl) {
        activeLevel = lvl;
        m_settings.reset();
        switch (lvl) {
            case 1:
                m_settings.gameMode = GameScreen.GameMode.CLASSIC;
                m_settings.initRadius = 2;
                m_settings.movingTileSpeed = 15;
                m_settings.minRotationSpeed = 20;
                m_settings.maxRotationSpeed = 30;
                break;
            case 2:
                m_settings.gameMode = GameScreen.GameMode.CLASSIC;
                m_settings.initRadius = 3;
                m_settings.movingTileSpeed = 15;
                m_settings.minRotationSpeed = 20;
                m_settings.maxRotationSpeed = 30;
                break;
            case 3:
                m_settings.gameMode = GameScreen.GameMode.SPIN_THE_CORE;
                m_settings.initRadius = 2;
                m_settings.movingTileSpeed = 2;
                m_settings.launcherCooldown = 3;
                break;
            case 4:
                m_settings.gameMode = GameScreen.GameMode.SPIN_THE_CORE;
                m_settings.initRadius = 3;
                m_settings.movingTileSpeed = 2;
                m_settings.launcherCooldown = 3;
                break;
            default:
                return;
        }
        m_gameScreen.startRound(m_settings, this);
    }

    @Override
    public void onRoundEnded(boolean result) {
        // Round WON
        if (result) {
            Preferences prefs = Gdx.app.getPreferences("highscores");
            if (prefs.getInteger("campaign_level", 0) < activeLevel) {
                prefs.putInteger("campaign_level", activeLevel);
                prefs.flush();
            }
            m_levelButtons[nextLevel].enable();
            ++nextLevel;
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

    private class CustomGestureDetector extends GestureDetector {
        public CustomGestureDetector(GestureListener listener) {
            super(listener);
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                m_game.setPrevScreen();
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
            setSize(200, 160);
            setPosition(x - 100, y - 80);
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
