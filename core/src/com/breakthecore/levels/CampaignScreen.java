package com.breakthecore.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.breakthecore.CoreSmash;
import com.breakthecore.GameController;
import com.breakthecore.RoundEndListener;
import com.breakthecore.UserAccount;
import com.breakthecore.levelbuilder.LevelListParser;
import com.breakthecore.levelbuilder.LevelListParser.RegisteredLevel;
import com.breakthecore.managers.StatsManager;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.ScreenBase;
import com.breakthecore.tilemap.TilemapManager;
import com.breakthecore.tiles.TileType.PowerupType;
import com.breakthecore.ui.Components;
import com.breakthecore.ui.LotteryDialog;
import com.breakthecore.ui.UIComponent;
import com.breakthecore.ui.UIFactory;
import com.breakthecore.ui.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CampaignScreen extends ScreenBase implements RoundEndListener {
    private GameScreen gameScreen;
    private PickPowerUpsDialog powerupPickDialog;
    private LotteryDialog lotteryDialog;
    private UIOverlay uiOverlay;
    private Skin skin;
    private Stage stage;
    private List<CampaignArea.LevelButton> levelButtons;
    private LevelListParser levelListParser;
    private Array<RegisteredLevel> levels;
    private Stack rootStack;

    private RegisteredLevel searchRegisteredLevel;

    public CampaignScreen(CoreSmash game) {
        super(game);
        skin = game.getSkin();
        stage = new Stage(game.getUIViewport());

        levelListParser = new LevelListParser();
        levels = new Array<>();

        powerupPickDialog = new PickPowerUpsDialog(skin, gameInstance.getUserAccount().getSpecialBallsAvailable());
        lotteryDialog = new LotteryDialog(skin, gameInstance.getUserAccount()) {
            @Override
            protected void result(Object object) {
                Reward reward = ((Reward) object);
                if (reward.getAmount() > 0) {
                    gameInstance.getUserAccount().addPowerup(reward.getType(), reward.getAmount());
                    Components.showToast("You have claimed " + reward.getAmount() + "x " + reward.getType() + "!", stage);
                }
                uiOverlay.lblLotteryCoins.setText(String.valueOf(gameInstance.getUserAccount().getLotteryCoins()));
            }
        };

        searchRegisteredLevel = new RegisteredLevel(0, "");

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    gameInstance.setPrevScreen();
                    return true;
                }
                return false;
            }

        });

        gameScreen = new GameScreen(gameInstance);

        rootStack = new Stack();
        rootStack.setFillParent(true);
        stage.addActor(rootStack);


        Area1 area1 = new Area1(skin, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                powerupPickDialog.show(stage, Integer.valueOf(actor.getName()));
            }
        });
        levelButtons = area1.getLevels();

        Container<Image> container = new Container<>(area1.getBackground());
        container.maxWidth(stage.getWidth());
        container.maxHeight(UIUtils.getHeightFor(area1.getBackground().getDrawable(), stage.getWidth()));

        Table testTable = new Table();
        testTable.stack(container, area1.getLevelsGroup(stage)).grow();

        ScrollPane scrollPane = new ScrollPane(testTable);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.validate();
        scrollPane.setSmoothScrolling(false);
        scrollPane.setScrollPercentY(100);

        rootStack.addActor(scrollPane);

        Table uiOverlayRoot = new Table();
        rootStack.addActor(uiOverlayRoot);

        UILeftBar leftBar = new UILeftBar();
        rootStack.addActor(leftBar.root);

        int levelsUnlocked = gameInstance.getUserAccount().getUnlockedLevels();
        for (int i = 0; i < levelsUnlocked && i < levelButtons.size(); ++i) {
            levelButtons.get(i).setDisabled(false);
        }

        uiOverlay = new UIOverlay(uiOverlayRoot);
    }

    @Override
    public void render(float delta) {
        stage.act();
        stage.draw();
    }

//    private Container<WidgetGroup> createButtonGroup() {
//        WidgetGroup grp = new WidgetGroup();
//        levelButtons = new LevelWidget[15];
//
//        final int WIDTH = Gdx.graphics.getWidth();
//        final int HEIGHT = Gdx.graphics.getHeight();
//
//        float ySpace = (HEIGHT > WIDTH ? HEIGHT : WIDTH) / 8f;
//
//        float x;
//        float y;
//        for (int i = 0; i < levelButtons.length; ++i) {
//            x = WIDTH / 4 + ySpace * (float) Math.cos(i * Math.PI / 2);
//            y = ySpace + i * ySpace;
//            levelButtons[i] = new LevelWidget(i + 1, (int) x, (int) y);
//            grp.addActor(levelButtons[i]);
//        }
//
//        Container<WidgetGroup> container = new Container<>(grp);
//        container.prefSize(WIDTH, ySpace + levelButtons.length * ySpace);
//
//        return container;
//    }

    private void startCampaignLevel(int lvl, final List<Powerup> powerups) {
        searchRegisteredLevel.num = lvl;
        int index = Arrays.binarySearch(levels.toArray(), searchRegisteredLevel, LevelListParser.compLevel);

        if (index < 0) return;

        final RegisteredLevel level = levels.get(index);

        gameScreen.deployLevel(new CampaignLevel(lvl, gameInstance.getUserAccount(), this) {
            @Override
            public void initialize(GameController controller) {
                controller.loadLevelMap(level.name);
                StatsManager statsManager = controller.getBehaviourPack().statsManager;
                statsManager.setLevel(level.num);
                for (Powerup powerup : powerups) {
                    statsManager.enablePowerup(powerup.type, powerup.count);
                }
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
    public void show() {
        levels.clear();
        levelListParser.parseAssignedLevels(levels);
        levels.sort(LevelListParser.compLevel);
        super.show();
    }

    @Override
    public void hide() {
        Components.clearToasts();
    }

    @Override
    public void onRoundEnded(StatsManager.GameStats stats) {
        gameInstance.getUserAccount().saveStats(stats);
        uiOverlay.updateValues();
        if (stats.isRoundWon()) {
            gameInstance.getUserAccount().addLotteryCoins(1);
            Components.showToast("You've been rewarded 1x Lottery Key!", stage);
            uiOverlay.lblLotteryCoins.setText(String.valueOf(gameInstance.getUserAccount().getLotteryCoins()));
        }
    }

    @Override
    public void resize(int width, int height) {
        powerupPickDialog.hide();
        super.resize(width, height);
    }

    private class LevelWidget extends Container<TextButton> {
        private int level;
        private TextButton button;

        public LevelWidget(int lvl, int x, int y) {
            button = UIFactory.createTextButton(String.valueOf(lvl), skin, "levelButton");

            setActor(button);
            if (CoreSmash.DEBUG_TABLET) {
                size(Value.percentWidth(.1f, rootStack));
            } else {
                size(Value.percentHeight(.1f, rootStack));
            }

            setPosition(x - getWidth() / 2, y - getHeight() / 2);

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
                powerupPickDialog.show(stage, m_lvl);
            }
        }
    }

    private class UILeftBar implements UIComponent {
        Container<Table> root;

        UILeftBar() {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = skin.getDrawable("slotMachine");
            style.imageDown = skin.newDrawable("slotMachine", Color.GRAY);
            style.up = skin.getDrawable("boxSmall");
            style.down = skin.newDrawable("boxSmall", Color.GRAY);

            ImageButton btnSlotMachine = new ImageButton(style);
            btnSlotMachine.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    lotteryDialog.show(stage);
                }
            });

            Viewport uiVp = stage.getViewport();
            float btnSize = uiVp.getWorldWidth() * .12f;

            Table bar = new Table();
            bar.setBackground(skin.getDrawable("boxSmall"));
            bar.add(btnSlotMachine).size(btnSize).pad(3 * Gdx.graphics.getDensity());

            root = new Container<>(bar);
            root.center().right();
            root.padRight(-4 * Gdx.graphics.getDensity());
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIOverlay implements UIComponent {
        private Table root;
        private ProgressBar pbAccountExp;
        private Label lblLevel, lblExp, lblExpForLevel, lblLotteryCoins;

        public UIOverlay(Table root) {
            this.root = root;
            ImageButton.ImageButtonStyle userButtonStyle = new ImageButton.ImageButtonStyle();
            userButtonStyle.up = skin.getDrawable("borderTrans");
            userButtonStyle.down = skin.newDrawable("borderTrans", Color.GRAY);
            userButtonStyle.imageUp = skin.newDrawable("userDefIcon");

            ImageButton btnUser = new ImageButton(userButtonStyle);
            btnUser.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
//                    UserAccount acc = gameInstance.getUserAccount();
//                    for (PowerupType pr : PowerupType.values()) {
//                        acc.addPowerup(pr, 5);
//                    }
//                    Components.showToast("Added a 'few' powerups...", stage);
                    //                    lotteryDialog.show(stage);
                }
            });

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


            Image imgLotteryCoin = new Image(skin.getDrawable("lotteryCoin"));
//            imgLotteryCoin.addListener(new ClickListener() {
//                @Override
//                public void clicked(InputEvent event, float x, float y) {
//                    gameInstance.getUserAccount().addLotteryCoins(1);
//                    lblLotteryCoins.setText(gameInstance.getUserAccount().getLotteryCoins());
//                }
//            });
            lblLotteryCoins = new Label(String.valueOf(gameInstance.getUserAccount().getLotteryCoins()), skin, "h5");

            Table tblInfo = new Table();
            tblInfo.top();
            tblInfo.add(imgLotteryCoin).size(Value.percentHeight(1f, lblLotteryCoins)).padRight(Value.percentHeight(.5f, lblLotteryCoins));
            tblInfo.add(lblLotteryCoins);

            Table tblAccount = new Table();

            Viewport uiVp = stage.getViewport();
            float btnUserSize = uiVp.getWorldWidth() * (uiVp.getWorldHeight() > uiVp.getWorldWidth() ? .15f : .1f);
            btnUser.getImageCell().grow().pad(5);

            tblAccount.pad(lblLevel.getPrefHeight() / 3);
            tblAccount.columnDefaults(0).padRight(5);
            tblAccount.row().padBottom(5);
            tblAccount.background(skin.newDrawable("flatColor", 20 / 255f, 20 / 255f, 20 / 255f, 1));
            tblAccount.add(btnUser)
                    .size(btnUserSize);
            tblAccount.add(tblInfo).fill().row();
            tblAccount.add(hgLevel).padBottom(5).left();
            tblAccount.add(hgExp).padBottom(5).right().row();
            tblAccount.add(pbAccountExp).grow().colspan(tblAccount.getColumns());

            root.top().left().pad(lblLevel.getPrefHeight() / 2);
            root.add(tblAccount);
            updateValues();
        }

        public void updateValues() {
            UserAccount user = gameInstance.getUserAccount();
            lblExp.setText(String.valueOf(user.getXPProgress()));
            lblLevel.setText(String.valueOf(user.getLevel()));
            lblExpForLevel.setText(String.valueOf(user.getExpForNextLevel()));
            pbAccountExp.setRange(0, user.getExpForNextLevel());
            pbAccountExp.setValue(user.getXPProgress());
            if (levelButtons.get(user.getUnlockedLevels()).isDisabled()) {
                levelButtons.get(user.getUnlockedLevels()).setDisabled(false);
            }
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class PickPowerUpsDialog extends Dialog {
        UserAccount.PowerupManager powerUpsAvailable;
        List<Powerup> choosenPowerups;
        int levelToLaunch = -1;
        ButtonGroup<Button> buttonGroup;
        Button[] powerupButtons;

        PickPowerUpsDialog(Skin skin, final UserAccount.PowerupManager powerUps) {
            super("", skin, "PickPowerUpDialog");
            powerUpsAvailable = powerUps;
            choosenPowerups = new ArrayList<>(3);

            buttonGroup = new ButtonGroup<>();
            buttonGroup.setMaxCheckCount(3);
            buttonGroup.setMinCheckCount(0);
            powerupButtons = new Button[PowerupType.values().length];

            HorizontalGroup powerupsGroup = new HorizontalGroup();
            powerupsGroup.space(10 * Gdx.graphics.getDensity());
            powerupsGroup.wrap(true);
            powerupsGroup.wrapSpace(10 * Gdx.graphics.getDensity());
            powerupsGroup.align(Align.center);

            int counter = 0;
            for (PowerupType type : PowerupType.values()) {
                powerupButtons[counter] = createPowerUpButton(type);
                powerupsGroup.addActor(powerupButtons[counter]);
                buttonGroup.add(powerupButtons[counter]);
                ++counter;
            }

            Table content = getContentTable();
            content.padBottom(10 * Gdx.graphics.getDensity());
            content.add(new Label("Choose your POWERUPS!", skin, "h4")).row();
            content.add(powerupsGroup)
//                    .width(Value.percentWidth(.8f, UIUtils.getScreenActor(powerupsGroup)));
                    .width(stage.getWidth() * .8f);

            float buttonSize = stage.getWidth() / 3;

            ImageButton btnClose = UIFactory.createImageButton(skin, "ButtonCancel");
            btnClose.getImageCell().grow().size(buttonSize, UIUtils.getHeightFor(btnClose.getImage().getDrawable(), buttonSize));
            btnClose.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelToLaunch = -1;
                    hide();
                }
            });

            ImageButton btnStart = UIFactory.createImageButton(skin, "ButtonStart");
            btnStart.getImageCell().grow().size(buttonSize, UIUtils.getHeightFor(btnClose.getImage().getDrawable(), buttonSize));
            btnStart.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    for (Button btn : buttonGroup.getAllChecked()) {
                        Powerup powerup = null;

                        for (Powerup p : choosenPowerups) {
                            if (p.type == PowerupType.valueOf(btn.getName())) {
                                powerup = p;
                                break;
                            }
                        }

                        if (powerup == null) {
                            choosenPowerups.add(new Powerup(PowerupType.valueOf(btn.getName()), 1));
                        } else {
                            ++powerup.count;
                        }
                    }
                    startCampaignLevel(levelToLaunch, choosenPowerups);
                    hide(null);
                }
            });

            Table buttons = getButtonTable();
            buttons.row().padBottom(4 * Gdx.graphics.getDensity());
            buttons.add(btnStart).expandX();
            buttons.add(btnClose).expandX();

            addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                        hide();
                        return true;
                    }
                    return false;
                }
            });

            setMovable(false);
            setResizable(false);
            setKeepWithinStage(true);
            pad(10 * Gdx.graphics.getDensity());
        }

        @Override
        public void hide() {
            super.hide(null);
        }

        public void show(Stage stage, int lvl) {
            for (Button button : powerupButtons) {
                int amount = powerUpsAvailable.getAmountOf(PowerupType.valueOf(button.getName()));
                ((Label) button.getCells().get(2).getActor()).setText(String.valueOf(amount));
                button.setDisabled(amount == 0);
            }
            choosenPowerups.clear();

            buttonGroup.uncheckAll();
            levelToLaunch = lvl;
            super.show(stage, null);
            setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        }

        private ImageButton createPowerUpButton(PowerupType type) {
            Skin skin = getSkin();
            Label lbl = new Label("null", getSkin(), "h5");
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.up = skin.getDrawable("boxSmall");
            style.disabled = skin.newDrawable("boxSmall", Color.DARK_GRAY);
            style.checked = skin.newDrawable("boxSmall", Color.GREEN);
            style.imageUp = skin.getDrawable(type.name());
            style.imageDisabled = skin.newDrawable(type.name(), Color.DARK_GRAY);

            ImageButton tb = new ImageButton(style);
            tb.setName(type.name());
            tb.add().row();
            tb.add(lbl).row();
            tb.getImageCell().size(50 * Gdx.graphics.getDensity(), 50 * Gdx.graphics.getDensity() - lbl.getPrefHeight()).row();
            return tb;
        }
    }


    private static class Powerup {
        private PowerupType type;
        private int count;

        Powerup(PowerupType type, int count) {
            this.type = type;
            this.count = count;
        }

        void set(PowerupType type, int count) {
            this.type = type;
            this.count = count;
        }

        void reset() {
            type = null;
            count = -1;
        }
    }
}