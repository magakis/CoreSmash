package com.archapp.coresmash.levels;

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
import com.archapp.coresmash.managers.RoundManager;
import com.archapp.coresmash.managers.RoundManager.GameStats;
import com.archapp.coresmash.platform.AdManager;
import com.archapp.coresmash.platform.FeedbackMailHandler;
import com.archapp.coresmash.platform.GoogleGames;
import com.archapp.coresmash.platform.PlayerInfo;
import com.archapp.coresmash.screens.GameScreen;
import com.archapp.coresmash.screens.ScreenBase;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tiles.TileType.PowerupType;
import com.archapp.coresmash.ui.Components;
import com.archapp.coresmash.ui.HeartReplenishDialog;
import com.archapp.coresmash.ui.LotteryDialog;
import com.archapp.coresmash.ui.UIComponent;
import com.archapp.coresmash.ui.UIFactory;
import com.archapp.coresmash.ui.UIUtils;
import com.archapp.coresmash.utlis.FileUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
    private HeartReplenishDialog heartReplenishDialog;
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
        heartReplenishDialog = new HeartReplenishDialog(skin, heartManager, gameInstance.getAdManager());

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
                if (heartManager.isHeartAvailable())
                    powerupPickDialog.show(stage, Integer.valueOf(actor.getName()));
                else
                    heartReplenishDialog.show(stage);
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

        uiRightBar = new UIRightBar(stage, skin, gameInstance.getUserAccount(), gameInstance.getAdManager(), gameInstance.getFeedbackMailHandler());
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
        searchRegisteredLevel.num = lvl;
        int index = Arrays.binarySearch(levels.toArray(), searchRegisteredLevel, LevelListParser.compLevel);
        if (index < 0) return;

        final RegisteredLevel level = levels.get(index);
        gameScreen.deployLevel(new CampaignLevel(lvl, gameInstance.getUserAccount(), this) {
            @Override
            public void initialize(GameController controller) {
                controller.loadLevelMap(level.name, LevelListParser.Source.INTERNAL);
                RoundManager roundManager = controller.getBehaviourPack().roundManager;
                roundManager.setLevel(level.num, gameInstance.getUserAccount().getUnlockedLevels());
                for (Powerup powerup : powerups) {
                    roundManager.enablePowerup(powerup.type, powerup.count);
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
                if ((nextLevel - 1) < levelButtons.size())
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
        private final float BUTTON_SIZE;
        private Container<Table> root;
        private final LotteryButton lotteryButton;
        private final HowToPlayDialog howToPlayDialog;
        private final HeartButton heartButton;

        UIRightBar(final Stage stage, final Skin skin, final UserAccount user, final AdManager adManager, final FeedbackMailHandler feedbackMailHandler) {
            BUTTON_SIZE = skin.getFont("h4").getLineHeight() * 3;

            lotteryButton = new LotteryButton(stage, skin, user, adManager);
            heartButton = new HeartButton(skin, user, adManager);
            howToPlayDialog = new HowToPlayDialog(skin);

            ImageButton helpButton = UIFactory.createImageButton(skin, "ButtonHowToPlay");
            helpButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    howToPlayDialog.show(stage);
                }
            });

            ImageButton feedbackButton = UIFactory.createImageButton(skin, "ButtonFeedback");
            feedbackButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    feedbackMailHandler.createFeedbackMail();
                }
            });

            Table barGroup = new Table();
            barGroup.defaults().space(BUTTON_SIZE * .4f);
            barGroup.add(helpButton)
                    .expandY()
                    .top()
                    .size(BUTTON_SIZE * .5f)
                    .padTop(BUTTON_SIZE * .4f)
                    .row();
            barGroup.add(heartButton.root)
                    .row();
            barGroup.add(lotteryButton.root)
                    .top()
                    .row();
            barGroup.add(feedbackButton)
                    .size(BUTTON_SIZE * .8f)
                    .padBottom(BUTTON_SIZE * .4f)
                    .expandY()
                    .bottom();

            root = new Container<>(barGroup);
            root.center().right().padRight(3 * Gdx.graphics.getDensity()).fillY();
        }

        @Override
        public Group getRoot() {
            return root;
        }

        private class LotteryButton {
            Container<Stack> root;
            Container<Image> indicator;
            Container<ImageButton> button;
            LotteryDialog lotteryDialog;

            public LotteryButton(final Stage stage, Skin skin, final UserAccount user, AdManager adManager) {
                lotteryDialog = new LotteryDialog(skin, user.getCurrencyManager(), adManager) {
                    @Override
                    protected void result(Object object) {
                        Reward reward = ((Reward) object);
                        if (reward.getAmount() > 0) {
                            user.addPowerup(reward.getType(), reward.getAmount());
                        }
                    }
                };

                ImageButton btnSlotMachine = UIFactory.createImageButton(skin, "ButtonLottery");
                btnSlotMachine.getImageCell().grow();
                btnSlotMachine.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        lotteryDialog.show(stage);
                    }
                });


                button = new Container<>(btnSlotMachine);
                button.size(BUTTON_SIZE, UIUtils.getHeightFor(btnSlotMachine.getImage().getDrawable(), BUTTON_SIZE));

                Image img = new Image(skin, "NotificationIndicator");
                indicator = new Container<>(img);
                indicator.top().right().size(BUTTON_SIZE * .3f);

                Stack rootstack = new Stack();
                rootstack.addActor(button);
                rootstack.addActor(indicator);

                root = new Container<>(rootstack);
                root.size(BUTTON_SIZE, UIUtils.getHeightFor(btnSlotMachine.getImage().getDrawable(), BUTTON_SIZE));

                indicator.setTransform(true);
                indicator.setOrigin(root.getPrefWidth() - (BUTTON_SIZE * .25f) / 2, root.getPrefHeight() - (BUTTON_SIZE * .25f) / 2f);
                indicator.addAction(Actions.forever(Actions.sequence(
                        Actions.scaleBy(-.15f, -.15f, .4f),
                        Actions.scaleBy(.15f, .15f, .4f)
                )));
                indicator.setVisible(user.getCurrencyManager().isCurrencyAvailable(CurrencyType.LOTTERY_COIN));

                user.setPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void onChange(String name, Object newValue) {
                        if (name.equals(CurrencyType.LOTTERY_COIN.name())) {
                            indicator.setVisible((int) newValue > 0);
                        }
                    }
                });
            }
        }

        private class HeartButton {
            private VerticalGroup root;
            private UserAccount.HeartManager heartManager;
            private AdManager.AdRewardListener listener;
            private Label lblLivesLeft;
            private Label lblTimeForLife;
            private Container<Image> redCross;

            public HeartButton(Skin skin, final UserAccount user, final AdManager adManager) {
                heartManager = user.getHeartManager();

                listener = new AdManager.AdRewardListener() {
                    @Override
                    public void reward(String type, int amount) {
                        heartManager.restoreHeart();
                    }
                };

                lblLivesLeft = new Label("null", skin, "h3o");
                lblLivesLeft.setAlignment(Align.center);
                lblTimeForLife = new Label("null", skin, "h5");
                lblTimeForLife.setAlignment(Align.center);

                Image imgRedCross = new Image(skin, "RedCross");
                redCross = new Container<>(imgRedCross);
                redCross.size(BUTTON_SIZE * .3f, UIUtils.getHeightFor(imgRedCross.getDrawable(), BUTTON_SIZE * .3f));
                redCross.setTransform(true);
                redCross.addAction(Actions.forever(Actions.forever(Actions.sequence(
                        Actions.scaleBy(.15f, .15f, .5f),
                        Actions.scaleBy(-.15f, -.15f, .5f)
                ))));

                Container<Container<Image>> redCrossWrapper = new Container<>(redCross);
                redCrossWrapper.top().right();

                Image backgroundImage = new Image(skin, "Heart");

                Stack heartStack = new Stack();
                heartStack.add(backgroundImage);
                heartStack.add(lblLivesLeft);
                heartStack.add(redCrossWrapper);

                redCross.setOrigin(Align.center);

                float buttonSize = BUTTON_SIZE * .8f;
                Container<Stack> livesLeftWrapper = new Container<>(heartStack);
                livesLeftWrapper.size(buttonSize, UIUtils.getHeightFor(backgroundImage.getDrawable(), buttonSize));

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
                            SoundManager.get().play(SoundManager.SoundTrack.BUTTON_CLICK);
                            adManager.showAdForReward(listener, AdManager.VideoAdRewardType.HEART);
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
                if (heartManager.isFull())
                    redCross.setVisible(false);
                else
                    redCross.setVisible(true);
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
        private ImageButton btnUser;
        private Label lblUserName,
                lblLevel, lblExp, lblExpForLevel, lblTotalXP;

        public UIUserPanel() {
            ImageButton.ImageButtonStyle userButtonStyle = new ImageButton.ImageButtonStyle();
            userButtonStyle.up = skin.getDrawable("invisible");

            lblUserName = UIFactory.createLabel("", skin, "h5", Align.center);
            btnUser = new ImageButton(userButtonStyle);

            updateUserInfo();

            ProgressBar.ProgressBarStyle pbStyle = new ProgressBar.ProgressBarStyle();
            pbStyle.background = skin.newDrawable("progressbar_inner", Color.DARK_GRAY);
            pbStyle.knobBefore = skin.newDrawable("progressbar_inner", Color.GREEN);

            pbStyle.background.setLeftWidth(0);
            pbStyle.background.setRightWidth(0);
            pbStyle.knobBefore.setLeftWidth(0);
            pbStyle.knobBefore.setRightWidth(0);

            pbAccountExp = new ProgressBar(0, 1, 1, false, pbStyle);

            lblLevel = new Label("", skin, "h4", new Color(0x793642ff));
            lblExp = new Label("", skin, "h6");
            lblExpForLevel = new Label("", skin, "h6", Color.GRAY);
            lblTotalXP = new Label("XP:", skin, "h5");

            HorizontalGroup hgExp = new HorizontalGroup();
            hgExp.wrap(false);
            hgExp.addActor(lblExp);
            hgExp.addActor(new Label("/", skin, "h6", Color.GRAY));
            hgExp.addActor(lblExpForLevel);

//            Table tblInfo = new Table();
//            tblInfo.top().left().defaults().left().padBottom(lblLotteryCoins.getMinHeight() / 4);

            Container<Label> userLevel = new Container<>(lblLevel);
            userLevel.setBackground(skin.getDrawable("UserLevelBackground"));
            Container<Container<Label>> userLevelWrapper = new Container<>(userLevel);
            userLevelWrapper.size(lblLevel.getPrefHeight() * 1.4f);
            userLevelWrapper.padLeft(lblLevel.getPrefHeight() * -.3f);
            userLevelWrapper.padBottom(lblLevel.getPrefHeight() * -.3f);
            userLevelWrapper.bottom().left();

            Stack userIconGroup = new Stack();
            userIconGroup.addActor(btnUser);
            userIconGroup.addActor(userLevelWrapper);

            contentSize = WorldSettings.getSmallestScreenDimension() * .5f;
            Table tblAccount = new Table();
            tblAccount.background(skin.getDrawable("UserAccountFrame"));
            tblAccount.add(lblUserName).center()
                    .padTop(-7 * Gdx.graphics.getDensity())
                    .padBottom(lblLevel.getPrefHeight() * .1f).row();
            tblAccount.row().padBottom(lblLevel.getPrefHeight() / 3);
            tblAccount.add(userIconGroup)
                    .size(contentSize * .4f).grow().row();
            tblAccount.add(lblTotalXP).padBottom(5).left().row();
            tblAccount.add(pbAccountExp).growX().colspan(tblAccount.getColumns()).padRight(0);

            Container<Table> wrapper = new Container<>(tblAccount);
            wrapper.maxWidth(contentSize);
            wrapper.pad(lblLevel.getPrefHeight() * .3f);

            root = new Container<>(wrapper);
            root.top().left();

            gameInstance.getPlatformSpecificManager().googleGames.addListener(
                    new PropertyChangeListener() {
                        @Override
                        public void onChange(String name, Object newValue) {
                            updateUserInfo();
                        }
                    });

            updateValues();
        }

        private void updateUserInfo() {
            GoogleGames service = gameInstance.getPlatformSpecificManager().googleGames;
            if (service.isSignedIn()) {
                PlayerInfo playerInfo = service.getAccountInfo();
                if (playerInfo.avatar != null)
                    btnUser.getStyle().imageUp = new TextureRegionDrawable(playerInfo.avatar);
                lblUserName.setText(playerInfo.displayName);
            } else {
                lblUserName.setText("Singed Out");
                btnUser.getStyle().imageUp = skin.getDrawable("DefaultUserIcon");
            }
        }

        public void updateValues() {
            UserAccount user = gameInstance.getUserAccount();
            lblExp.setText(String.valueOf(user.getXPProgress()));
            lblLevel.setText(String.valueOf(user.getLevel()));
            lblExpForLevel.setText(String.valueOf(user.getExpForNextLevel()));
            lblTotalXP.setText("XP: " + UIUtils.formatNumber(user.getTotalProgress(), '.'));
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

            ImageButton btnStart = UIFactory.createImageButton(skin, "ButtonPlay");
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
            buttons.row().expandX();
            buttons.add(btnStart).height(buttonSize).width(UIUtils.getWidthFor(btnStart.getImage().getDrawable(), buttonSize));
            buttons.add(btnClose).height(buttonSize).width(UIUtils.getWidthFor(btnClose.getImage().getDrawable(), buttonSize));

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

    private static class HowToPlayDialog extends Dialog {
        private Label lblMessage;
        private Label lblTitle;
        private ScrollPane scrollPane;

        HowToPlayDialog(Skin skin) {
            super("", skin, "PopupMessage");

            float width = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();

            lblMessage = new Label(FileUtils.fileToString(Gdx.files.internal("docs/how_to_play").reader(512)), skin, "h5");
            lblTitle = new Label("How to play?", skin, "h2");
            lblMessage.setWrap(true);
            scrollPane = new ScrollPane(lblMessage);
            scrollPane.setScrollingDisabled(true, false);
            scrollPane.setOverscroll(false, false);

            getContentTable().defaults().space(0);
            getContentTable().add(lblTitle).row();
            getContentTable().add(scrollPane).padLeft(width * .025f).padRight(width * .025f).grow();
            getCell(getContentTable()).width(width).padBottom(width * .025f);
            getCell(getButtonTable()).width(width);
            setModal(true);
            setMovable(false);

            ImageButton btn = UIFactory.createImageButton(skin, "ButtonGotIt");
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide(null);
                }
            });

            float buttonSize = WorldSettings.getDefaultButtonHeight();
            getButtonTable().add(btn).height(buttonSize).width(UIUtils.getWidthFor(btn.getImage().getDrawable(), buttonSize));
        }

        @Override
        public Dialog show(Stage stage) {
            return show(stage, null);
        }

        public Dialog show(Stage stage, Action action) {
            getCell(getContentTable()).maxHeight(stage.getHeight() * .7f);
            super.show(stage, action);
            setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
            return this;
        }
    }
}