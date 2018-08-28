package com.archapp.coresmash.levels;

import com.archapp.coresmash.AdManager;
import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.CurrencyType;
import com.archapp.coresmash.GameController;
import com.archapp.coresmash.PropertyChangeListener;
import com.archapp.coresmash.RoundEndListener;
import com.archapp.coresmash.UserAccount;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.levelbuilder.LevelListParser;
import com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel;
import com.archapp.coresmash.levels.CampaignArea.LevelButton;
import com.archapp.coresmash.managers.StatsManager;
import com.archapp.coresmash.managers.StatsManager.GameStats;
import com.archapp.coresmash.screens.GameScreen;
import com.archapp.coresmash.screens.ScreenBase;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tiles.TileType.PowerupType;
import com.archapp.coresmash.ui.Components;
import com.archapp.coresmash.ui.LotteryDialog;
import com.archapp.coresmash.ui.UIComponent;
import com.archapp.coresmash.ui.UIFactory;
import com.archapp.coresmash.ui.UIUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.archapp.coresmash.CurrencyType.LOTTERY_COIN;

public class CampaignScreen extends ScreenBase implements RoundEndListener {
    private GameScreen gameScreen;
    private PickPowerUpsDialog powerupPickDialog;
    private UserAccount.HeartManager heartManager;
    private UIRightBar uiRightBar;
    private UIUserPanel uiUserPanel;
    private Skin skin;
    private Stage stage;
    private List<LevelButton> levelButtons;
    private LevelListParser levelListParser;
    private Array<RegisteredLevel> levels;
    private RewardsPerLevelManager rewardsManager;
    private Stack rootStack;

    private RegisteredLevel searchRegisteredLevel;

    public CampaignScreen(CoreSmash game) {
        super(game);
        skin = game.getSkin();
        heartManager = game.getUserAccount().getHeartManager();

        rewardsManager = new RewardsPerLevelManager();
        stage = new Stage(game.getUIViewport());

        levelListParser = new LevelListParser();
        levels = new Array<>();

        powerupPickDialog = new PickPowerUpsDialog(skin, gameInstance.getUserAccount().getSpecialBallsAvailable());

        searchRegisteredLevel = new RegisteredLevel(0, "");

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(new InputAdapter() {
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

        Container<Image> container = new Container<>(area1.getBackground());
        container.maxWidth(stage.getWidth());
        container.maxHeight(UIUtils.getHeightFor(area1.getBackground().getDrawable(), stage.getWidth()));

        Table testTable = new Table();
        testTable.stack(container, area1.getLevelsGroup()).grow();

        ScrollPane scrollPane = new ScrollPane(testTable);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.validate();
        scrollPane.setSmoothScrolling(false);
        scrollPane.setScrollPercentY(100);

        rootStack.addActor(scrollPane);

        uiUserPanel = new UIUserPanel();
        rootStack.addActor(uiUserPanel.root);

        uiRightBar = new UIRightBar(stage, skin, gameInstance.getUserAccount(), gameInstance.getAdManager());
        rootStack.addActor(uiRightBar.root);

        levelButtons = area1.getLevels();
        int levelsUnlocked = gameInstance.getUserAccount().getUnlockedLevels();
        for (int i = levelsUnlocked; i < levelButtons.size(); ++i) {
            levelButtons.get(i).setDisabled(true);
        }
    }

    @Override
    public void render(float delta) {
        heartManager.checkTimeForHeart();

        if (!heartManager.isFull())
            uiRightBar.heartButton.updateTimeTillNextHeart();

        stage.act();
        stage.draw();
    }

    private void startCampaignLevel(int lvl, final List<Powerup> powerups) {
        if (heartManager.getHearts() == 0) {
            Components.showToast("You have no Hearts left. Either wait for a heart to replenish or try watching a video", stage, 3);
            return;
        }

        searchRegisteredLevel.num = lvl;
        int index = Arrays.binarySearch(levels.toArray(), searchRegisteredLevel, LevelListParser.compLevel);
        if (index < 0) return;

        final RegisteredLevel level = levels.get(index);
        gameScreen.deployLevel(new CampaignLevel(lvl, gameInstance.getUserAccount(), this) {
            @Override
            public void initialize(GameController controller) {
                controller.loadLevelMap(level.name, LevelListParser.Source.INTERNAL);
                StatsManager statsManager = controller.getBehaviourPack().statsManager;
                statsManager.setLevel(level.num, gameInstance.getUserAccount().getUnlockedLevels());
                for (Powerup powerup : powerups) {
                    statsManager.enablePowerup(powerup.type, powerup.count);
                }
            }

            @Override
            public void update(float delta, TilemapManager tilemapManager) {

            }
        });
        heartManager.consumeHeart();
    }

    public void updateInfo() {
        uiUserPanel.updateValues();
    }

    @Override
    public void show() {
        levels.clear();
        levelListParser.parseAssignedLevels(levels, LevelListParser.Source.INTERNAL);
        levels.sort(LevelListParser.compLevel);
        super.show();
    }

    @Override
    public void hide() {
        Components.clearToasts();
    }

    @Override
    public void onRoundEnded(GameStats stats) {
        gameInstance.getUserAccount().saveStats(stats);
        uiUserPanel.updateValues();
        if (stats.isRoundWon()) {
            if (stats.isLevelUnlocked()) {
                int nextLevel = stats.getUnlockedLevel() + 1;
                rewardsManager.giveRewardForLevel(nextLevel, gameInstance.getUserAccount(), stage);
                levelButtons.get(nextLevel - 1).setDisabled(false);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        powerupPickDialog.hide();
        super.resize(width, height);
    }

    private static class UIRightBar implements UIComponent {
        public final float BUTTON_SIZE;
        private Container<Container<VerticalGroup>> root;
        private final LotteryButton lotteryButton;
        private final HeartButton heartButton;

        UIRightBar(final Stage stage, final Skin skin, final UserAccount user, final AdManager adManager) {
            BUTTON_SIZE = skin.getFont("h4").getLineHeight() * 3;

            lotteryButton = new LotteryButton(stage, skin, user);
            heartButton = new HeartButton(skin, user, adManager);

            VerticalGroup barGroup = new VerticalGroup();
            barGroup.addActor(heartButton.root);
            barGroup.addActor(lotteryButton.root);

            Container<VerticalGroup> bar = new Container<>(barGroup);

            root = new Container<>(bar);
            root.center().right().padRight(3 * Gdx.graphics.getDensity());
        }

        @Override
        public Group getRoot() {
            return root;
        }

        private class LotteryButton {
            Container<ImageButton> root;
            LotteryDialog lotteryDialog;

            public LotteryButton(final Stage stage, Skin skin, final UserAccount user) {
                lotteryDialog = new LotteryDialog(skin, user.getCurrencyManager()) {
                    @Override
                    protected void result(Object object) {
                        Reward reward = ((Reward) object);
                        if (reward.getAmount() > 0) {
                            user.addPowerup(reward.getType(), reward.getAmount());
                            Components.showToast("You have claimed " + reward.getAmount() + "x " + reward.getType() + "!", getStage());
                        }
                    }
                };

                ImageButton btnSlotMachine = new ImageButton(skin, "ButtonLottery");
                btnSlotMachine.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lotteryDialog.show(stage);
                    }
                });

                root = new Container<>(btnSlotMachine);
                root.size(BUTTON_SIZE, UIUtils.getWidthFor(btnSlotMachine.getImage().getDrawable(), BUTTON_SIZE));
            }
        }

        private class HeartButton {
            private VerticalGroup root;
            private UserAccount.HeartManager heartManager;
            private Label lblLivesLeft;
            private Label lblTimeForLife;

            public HeartButton(Skin skin, final UserAccount user, final AdManager adManager) {
                heartManager = user.getHeartManager();

                lblLivesLeft = new Label("null", skin, "h3o");
                lblLivesLeft.setAlignment(Align.center);
                lblTimeForLife = new Label("null", skin, "h5");
                lblTimeForLife.setAlignment(Align.center);

                Container<Label> livesLeft = new Container<>(lblLivesLeft);
                livesLeft.center();
                livesLeft.setBackground(skin.getDrawable("Heart"));

                float buttonSize = BUTTON_SIZE * .8f;
                Container<Container<Label>> livesLeftWrapper = new Container<>(livesLeft);
                livesLeftWrapper.size(buttonSize, UIUtils.getHeightFor(livesLeft.getBackground(), buttonSize));

                Container<Label> timeForLife = new Container<>(lblTimeForLife);
                timeForLife.width(Value.percentWidth(1, livesLeftWrapper));

                root = new VerticalGroup();
                root.addActor(livesLeftWrapper);
                root.addActor(timeForLife);
                root.setTouchable(Touchable.enabled);
                root.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (!heartManager.isFull()) {
                            adManager.showAdForReward(new AdManager.AdRewardListener() {
                                @Override
                                public void reward(String type, int amount) {
                                    user.getCurrencyManager().giveCurrency(LOTTERY_COIN);
                                    heartManager.restoreHeart();
                                }
                            });
                        }
                    }
                });

                heartManager.setChangeListener(new PropertyChangeListener() {
                    @Override
                    public void onChange(String name, Object newValue) {
                        updateHearts();
                        updateTimeTillNextHeart();
                    }
                });

                updateHearts();
                updateTimeTillNextHeart();
            }

            public void updateTimeTillNextHeart() {
                if (heartManager.isFull())
                    lblTimeForLife.setText("Full");
                else
                    lblTimeForLife.setText(String.format(Locale.ROOT, "%1$tM:%1$tS", heartManager.getTimeForNextHeart()));
            }


            public void updateHearts() {
                lblLivesLeft.setText(String.valueOf(heartManager.getHearts()));
            }
        }
    }

    private static class RewardsPerLevelManager {
        private Random rand;

        public RewardsPerLevelManager() {
            rand = new Random();
        }

        public void giveRewardForLevel(int level, UserAccount account, Stage stage) {
            if (rand.nextBoolean()) {
                account.getCurrencyManager().giveCurrency(LOTTERY_COIN);
                Components.showToast("You were rewarded 1x Lottery Key!", stage);
            }
        }
    }

    private class UIUserPanel implements UIComponent {
        private float contentSize;

        private Container<Container<Table>> root;
        private ProgressBar pbAccountExp;
        private Label lblLevel, lblExp, lblExpForLevel, lblLotteryCoins;

        public UIUserPanel() {
            ImageButton.ImageButtonStyle userButtonStyle = new ImageButton.ImageButtonStyle();
            userButtonStyle.up = skin.getDrawable("invisible");
            userButtonStyle.imageUp = skin.getDrawable("DefaultUserIcon");
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

            Image imgLotteryCoin = new Image(skin.getDrawable("LotteryCoin"));
            lblLotteryCoins = new Label(String.valueOf(gameInstance.getUserAccount().getCurrencyManager().getAmountOf(LOTTERY_COIN)), skin, "h5");

            Table tblInfo = new Table();
            tblInfo.top().defaults().left().padBottom(lblLotteryCoins.getMinHeight() / 4);
            tblInfo.add(imgLotteryCoin).height(lblLotteryCoins.getMinHeight() * 1.3f).width(UIUtils.getWidthFor(imgLotteryCoin.getDrawable(), lblLotteryCoins.getMinHeight() * 1.3f)).padRight(Value.percentHeight(.2f, lblLotteryCoins));
            tblInfo.add(lblLotteryCoins).row();


            Table tblAccount = new Table();
            tblAccount.background(skin.getDrawable("UserAccountFrame"));

            contentSize = WorldSettings.getSmallestScreenDimension() * .4f;

            tblAccount.columnDefaults(0).padRight(5);
            tblAccount.row().padBottom(5);
            tblAccount.add(btnUser)
                    .size(contentSize * .4f)
                    .padRight(lblLotteryCoins.getMinHeight() / 2);
            tblAccount.add(tblInfo).fill().row();
            tblAccount.add(hgLevel).padBottom(5).left();
            tblAccount.add(hgExp).padBottom(5).right().row();
            tblAccount.add(pbAccountExp).growX().colspan(tblAccount.getColumns()).padRight(0);


            Container<Table> wrapper = new Container<>(tblAccount);
            wrapper.maxWidth(contentSize);
            wrapper.pad(lblLevel.getPrefHeight() / 2);

            root = new Container<>(wrapper);
            root.top().left();

            gameInstance.getUserAccount().setPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void onChange(String name, Object newValue) {
                    if (name.equals(CurrencyType.LOTTERY_COIN.name())) {
                        lblLotteryCoins.setText(String.valueOf(newValue));
                    }
                }
            });

            updateValues();
        }

        public void updateValues() {
            UserAccount user = gameInstance.getUserAccount();
            lblExp.setText(String.valueOf(user.getXPProgress()));
            lblLevel.setText(String.valueOf(user.getLevel()));
            lblExpForLevel.setText(String.valueOf(user.getExpForNextLevel()));
            pbAccountExp.setRange(0, user.getExpForNextLevel());
            pbAccountExp.setValue(user.getXPProgress());
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class PickPowerUpsDialog extends Dialog {
        private float contentSize;

        private UserAccount.PowerupManager powerUpsAvailable;
        private List<Powerup> choosenPowerups;
        private int levelToLaunch = -1;
        private ButtonGroup<Button> buttonGroup;
        private Button[] powerupButtons;

        PickPowerUpsDialog(Skin skin, final UserAccount.PowerupManager powerUps) {
            super("", skin, "PickPowerUpDialog");
            powerUpsAvailable = powerUps;
            choosenPowerups = new ArrayList<>(3);

            contentSize = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();

            buttonGroup = new ButtonGroup<>();
            buttonGroup.setMaxCheckCount(3);
            buttonGroup.setMinCheckCount(0);
            powerupButtons = new Button[PowerupType.values().length];

            HorizontalGroup powerupsGroup = new HorizontalGroup();
            powerupsGroup.space(contentSize * .05f);
            powerupsGroup.wrap(true);
            powerupsGroup.wrapSpace(8 * Gdx.graphics.getDensity());
            powerupsGroup.align(Align.center);

            int counter = 0;
            for (PowerupType type : PowerupType.values()) {
                powerupButtons[counter] = createPowerUpButton(type);
                powerupsGroup.addActor(powerupButtons[counter]);
                buttonGroup.add(powerupButtons[counter]);
                ++counter;
            }


            Table content = getContentTable();
            getCell(content).width(contentSize);
            content.padBottom(contentSize * .025f);
            content.add(new Label("Choose your POWERUPS!", skin, "h4")).row();
            content.add(powerupsGroup)
                    .width(contentSize);


            ImageButton btnClose = UIFactory.createImageButton(skin, "ButtonCancel");
            btnClose.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    levelToLaunch = -1;
                    hide();
                }
            });

            ImageButton btnStart = UIFactory.createImageButton(skin, "ButtonStart");
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

            float buttonSize = contentSize * WorldSettings.DefaultRatio.dialogButtonToContent();
            Table buttons = getButtonTable();
            buttons.defaults().space((contentSize - (2 * buttonSize)) / 4);
            buttons.add(btnStart).width(buttonSize).height(UIUtils.getHeightFor(btnStart.getImage().getDrawable(), buttonSize));
            buttons.add(btnClose).width(buttonSize).height(UIUtils.getHeightFor(btnClose.getImage().getDrawable(), buttonSize));

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
            float buttonSize = contentSize * .15f;
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
            tb.getImageCell().size(buttonSize, buttonSize - lbl.getPrefHeight()).row();
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