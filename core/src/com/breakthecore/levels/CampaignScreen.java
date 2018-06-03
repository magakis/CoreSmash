package com.breakthecore.levels;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.breakthecore.CoreSmash;
import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
import com.breakthecore.WorldSettings;
import com.breakthecore.levelbuilder.XmlManager;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.ScreenBase;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.ui.UIComponent;

public class CampaignScreen extends ScreenBase implements RoundEndListener {
    private GameScreen gameScreen;
    private UIOverlay uiOverlay;
    private GestureDetector gd;
    private Skin skin;
    private Stage stage;
    private LevelButton[] levelButtons;

    public CampaignScreen(CoreSmash game) {
        super(game);
        skin = game.getSkin();
        stage = new Stage(game.getUIViewport());
        gd = new CustomGestureDetector(new InputListener());

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(gd);
        gameScreen = new GameScreen(gameInstance);

        levelButtons = new LevelButton[20];

        WidgetGroup buttonsGroup = createButtonGroup();
        ScrollPane scrollPane = new ScrollPane(buttonsGroup);
        scrollPane.setFillParent(true);
        scrollPane.setOverscroll(false, false);
        scrollPane.validate();
        scrollPane.setSmoothScrolling(false);
        scrollPane.setScrollPercentY(100);

        int levelsUnlocked = gameInstance.getUserAccount().getUnlockedLevels();
        for (int i = 0; i < levelsUnlocked && i < levelButtons.length; ++i) {
            levelButtons[i].enable();
        }

        uiOverlay = new UIOverlay();

        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        rootStack.addActor(scrollPane);
        rootStack.addActor(uiOverlay.show());


        stage.addActor(rootStack);
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
        container.prefSize(WorldSettings.getWorldWidth(), 200 + 20 * 190);

        return container;
    }

    private void startCampaignLevel(int lvl) {
        if (!XmlManager.fileExists("level" + lvl)) return;

        gameScreen.deployLevel(new CampaignLevel(lvl, gameInstance.getUserAccount(), this) {
            @Override
            public void initialize(GameScreen.GameScreenController gameScreenController) {
                gameScreenController.loadLevel(getLevelNumber());
            }

            @Override
            public void update(float delta, TilemapManager tilemapManager) {

            }
        });
    }

    public void updateInfo() {
        uiOverlay.updateValues();
    }

    @Override
    public void onRoundEnded(StatsManager statsManager) {
        gameInstance.getUserAccount().saveStats(statsManager);
        uiOverlay.updateValues();
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

    /**
     * Why do I have a GestureDetector in Campaign Screen?!?
     */
    private class CustomGestureDetector extends GestureDetector {
        public CustomGestureDetector(GestureListener listener) {
            super(listener);
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
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
            super(String.valueOf(lvl), skin, "levelBtnDisabled");
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
            setStyle(skin.get("levelBtnEnabled", TextButtonStyle.class));
        }
    }

    private class UIOverlay implements UIComponent {
        private Table root;
        private ProgressBar pbAccountExp;
        private Label lblLevel, lblExp, lblExpForLevel;

        public UIOverlay() {

            ImageButton.ImageButtonStyle userButtonStyle = new ImageButton.ImageButtonStyle();
            userButtonStyle.up = skin.getDrawable("box_white_5");
            userButtonStyle.down = skin.newDrawable("box_white_5", Color.GRAY);
            userButtonStyle.imageUp = skin.newDrawable("userDefIcon");

            ImageButton btnUser = new ImageButton(userButtonStyle);
            btnUser.getImage().setScaling(Scaling.fit);
            btnUser.getImageCell().pad(10);

            ProgressBar.ProgressBarStyle pbStyle = new ProgressBar.ProgressBarStyle();
            pbStyle.background = skin.newDrawable("progressbar_inner", Color.DARK_GRAY);
            pbStyle.knobBefore = skin.newDrawable("progressbar_inner", Color.GREEN);

            pbStyle.background.setLeftWidth(0);
            pbStyle.background.setRightWidth(0);

            pbStyle.knobBefore.setLeftWidth(0);
            pbStyle.knobBefore.setRightWidth(0);

            pbAccountExp = new ProgressBar(0, 1, 1, false, pbStyle);

            lblLevel = new Label("", skin, "h5");
            lblExp = new Label("", skin, "h5");
            lblExpForLevel = new Label("", skin, "h6", Color.GRAY);

            HorizontalGroup hgExp = new HorizontalGroup();
            hgExp.wrap(false);
            hgExp.addActor(lblExp);
            hgExp.addActor(new Label("/", skin, "h6", Color.GRAY));
            hgExp.addActor(lblExpForLevel);

            HorizontalGroup hgLevel = new HorizontalGroup();
            hgLevel.wrap(false);
            hgLevel.addActor(new Label("Level: ", skin, "h5"));
            hgLevel.addActor(lblLevel);

            Table tblInfo = new Table();

            Table tblAccount = new Table();
            tblAccount.background(skin.newDrawable("box_white_5", 30 / 255f, 30 / 255f, 30 / 255f, 1));
            tblAccount.pad(15);
            tblAccount.add(btnUser).width(130).height(130).padBottom(15).padRight(15).left();
            tblAccount.add(tblInfo).fill().padBottom(15).row();
            tblAccount.add(hgLevel).padBottom(5).left();
            tblAccount.add(hgExp).padBottom(5).right().row();
            tblAccount.add(pbAccountExp).growX().width(350).colspan(tblAccount.getColumns());

            root = new Table();
            root.top().left().pad(25);
            root.add(tblAccount);

            updateValues();
        }

        public void updateValues() {
            UserAccount user = gameInstance.getUserAccount();
            lblExp.setText(user.getXPProgress());
            lblLevel.setText(user.getLevel());
            lblExpForLevel.setText(user.getExpForNextLevel());
            pbAccountExp.setRange(0, user.getExpForNextLevel());
            pbAccountExp.setValue(user.getXPProgress());
            if (levelButtons[user.getUnlockedLevels()].isDisabled()) {
                levelButtons[user.getUnlockedLevels()].enable();
            }
        }

        @Override
        public Group show() {
            return root;
        }
    }
}