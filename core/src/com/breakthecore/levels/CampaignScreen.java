package com.breakthecore.levels;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.breakthecore.CoreSmash;
import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
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
    private LevelWidget[] levelButtons;
    private Stack rootStack;

    public CampaignScreen(CoreSmash game) {
        super(game);
        skin = game.getSkin();
        stage = new Stage(game.getUIViewport());
        gd = new CustomGestureDetector(new InputListener());

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(gd);
        gameScreen = new GameScreen(gameInstance);

        rootStack = new Stack();
        rootStack.setFillParent(true);
        stage.addActor(rootStack);

        levelButtons = new LevelWidget[20];

        WidgetGroup buttonsGroup = createButtonGroup();
        ScrollPane scrollPane = new ScrollPane(buttonsGroup);
        scrollPane.setOverscroll(false, false);
        scrollPane.validate();
        scrollPane.setSmoothScrolling(false);
        scrollPane.setScrollPercentY(100);
        rootStack.addActor(scrollPane);

        Table uiOverlayRoot = new Table();
        rootStack.addActor(uiOverlayRoot);

        int levelsUnlocked = gameInstance.getUserAccount().getUnlockedLevels();
        for (int i = 0; i < levelsUnlocked && i < levelButtons.length; ++i) {
            levelButtons[i].enable();
        }

        uiOverlay = new UIOverlay(uiOverlayRoot);
    }

    @Override
    public void render(float delta) {
        stage.act();
        stage.draw();
    }

    private Container<WidgetGroup> createButtonGroup() {
        WidgetGroup grp = new WidgetGroup();

        final int WIDTH = Gdx.graphics.getWidth();
        final int HEIGHT = Gdx.graphics.getHeight();

        float ySpace = (HEIGHT > WIDTH ? HEIGHT : WIDTH) / 8f;

        float x;
        float y;
        for (int i = 0; i < levelButtons.length; ++i) {
            x = WIDTH/2  + ySpace * (float) Math.cos(i * Math.PI / 2);
            y = ySpace + i * ySpace;
            levelButtons[i] = new LevelWidget(i + 1, (int) x, (int) y);
            grp.addActor(levelButtons[i]);
        }

        Container<WidgetGroup> container = new Container<>(grp);
        container.prefSize(WIDTH, ySpace + levelButtons.length * ySpace);

        container.debug();
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

//    private void invalidateAll() {
//        SnapshotArray<Actor> actors = ((Container<WidgetGroup>)((ScrollPane)rootStack.getChildren().get(0)).getActor()).getActor().getChildren();
//
//        for (Actor actor : actors){
//            ((WidgetGroup)actor).invalidate();
//        }
//
//        ((WidgetGroup)actors.get(0)).invalidateHierarchy();
//    }

    private class LevelWidget extends Container<TextButton>{
        private int level;
        private TextButton button;

        public LevelWidget(int lvl, int x, int y) {
            button = new TextButton(String.valueOf(lvl), skin, "levelButton");

            setActor(button);
            if (CoreSmash.DEBUG_TABLET) {
                size(Value.percentWidth(.1f, rootStack));
            } else {
                size(Value.percentHeight(.1f, rootStack));
            }

            setPosition(x - getWidth()/2, y - getHeight()/2);

            addListener(new LevelLauncher(lvl));
            level = lvl;
        }

        public int getLevel() {
            return level;
        }

        public boolean isDisabled() {
            return button.isDisabled();
        }

        public void enable() {
            button.setDisabled(false);
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
    }

    private class UIOverlay implements UIComponent {
        private Table root;
        private ProgressBar pbAccountExp;
        private Label lblLevel, lblExp, lblExpForLevel;

        public UIOverlay(Table root) {
            this.root = root;
            ImageButton.ImageButtonStyle userButtonStyle = new ImageButton.ImageButtonStyle();
            userButtonStyle.up = skin.getDrawable("box_white_5");
            userButtonStyle.down = skin.newDrawable("box_white_5", Color.GRAY);
            userButtonStyle.imageUp = skin.newDrawable("userDefIcon");

            ImageButton btnUser = new ImageButton(userButtonStyle);

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

            tblAccount.columnDefaults(0).padRight(5);
            tblAccount.row().padBottom(5);
            tblAccount.background(skin.newDrawable("box_white_5", 30 / 255f, 30 / 255f, 30 / 255f, 1));
            tblAccount.add(btnUser)
                    .size(60);
            tblAccount.add(tblInfo).fill().row();
            tblAccount.add(hgLevel).padBottom(5).left();
            tblAccount.add(hgExp).padBottom(5).right().row();
            tblAccount.add(pbAccountExp).grow().colspan(tblAccount.getColumns());

            root.top().left().pad(5);
            root.add(tblAccount);
            tblAccount.pack();
            btnUser.invalidateHierarchy();
            tblAccount.validate();

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