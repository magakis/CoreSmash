package com.archapp.coresmash.levels;

import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.CurrencyType;
import com.archapp.coresmash.GameController;
import com.archapp.coresmash.GameTarget;
import com.archapp.coresmash.Lottery;
import com.archapp.coresmash.PropertyChangeListener;
import com.archapp.coresmash.RoundEndListener;
import com.archapp.coresmash.UserAccount;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.levelbuilder.LevelListParser;
import com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel;
import com.archapp.coresmash.levelbuilder.LevelParser;
import com.archapp.coresmash.levels.CampaignArea.LevelButton;
import com.archapp.coresmash.managers.RoundManager.GameStats;
import com.archapp.coresmash.platform.AdManager;
import com.archapp.coresmash.platform.FeedbackMailHandler;
import com.archapp.coresmash.platform.GoogleGames;
import com.archapp.coresmash.platform.PlayerInfo;
import com.archapp.coresmash.screens.GameScreen;
import com.archapp.coresmash.screens.ScreenBase;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.tiles.TileType.PowerupType;
import com.archapp.coresmash.ui.Annotator;
import com.archapp.coresmash.ui.Components;
import com.archapp.coresmash.ui.HeartReplenishDialog;
import com.archapp.coresmash.ui.LotteryDialog;
import com.archapp.coresmash.ui.UIComponent;
import com.archapp.coresmash.ui.UIEffects;
import com.archapp.coresmash.ui.UIFactory;
import com.archapp.coresmash.ui.UIUtils;
import com.archapp.coresmash.utlis.FileUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
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
import com.badlogic.gdx.utils.IntMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.archapp.coresmash.CurrencyType.GOLD_BAR;
import static com.archapp.coresmash.CurrencyType.LOTTERY_TICKET;

public class CampaignScreen extends ScreenBase implements RoundEndListener {
    private GameScreen gameScreen;
    private PickPowerUpsDialog powerupPickDialog;
    private HeartReplenishDialog heartReplenishDialog;
    private UserAccount.HeartManager heartManager;
    private UIRightBar uiRightBar;
    private UIUserPanel uiUserPanel;
    private Skin skin;
    private Stage stage;
    private IntMap<LevelButton> levelButtons;
    private LevelListParser levelListParser;
    private IntMap<String> levels;
    private RewardsPerLevelManager rewardsManager;
    private Annotator annotator;
    private Stack rootStack;

    public CampaignScreen(CoreSmash game) {
        super(game);
        skin = game.getSkin();
        heartManager = game.getUserAccount().getHeartManager();

        rewardsManager = new RewardsPerLevelManager();
        stage = new Stage(game.getUIViewport());
        annotator = new Annotator(skin);

        levelListParser = new LevelListParser();
        levels = new IntMap<>();

        powerupPickDialog = new PickPowerUpsDialog(skin, gameInstance.getUserAccount().getSpecialBallsAvailable());
        heartReplenishDialog = new HeartReplenishDialog(skin, heartManager, gameInstance.getAdManager());

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

        uiRightBar = new UIRightBar(stage, skin,
                gameInstance.getUserAccount(),
                gameInstance.getAdManager(),
                gameInstance.getFeedbackMailHandler());

        rootStack.addActor(uiRightBar.root);

        levelButtons = area1.getLevelButtonList();
        for (int i = gameInstance.getUserAccount().getUnlockedLevels() + 1;
             i <= levelButtons.size;
             ++i) {
            levelButtons.get(i).setDisabled(true);
        }

        Array<RegisteredLevel> foundLevels = Array.of(RegisteredLevel.class);
        levelListParser.parseAssignedLevels(foundLevels, LevelListParser.Source.INTERNAL);

        for (RegisteredLevel lvl : foundLevels) {
            levels.put(lvl.num, lvl.name);
        }

        area1.updateLevelStars(levels, gameInstance.getUserAccount());
    }

    @Override
    public void render(float delta) {
        heartManager.checkTimeForHeart();

        if (!heartManager.isFull())
            uiRightBar.heartButton.updateTimeTillNextHeart();

        stage.act();
        stage.draw();
    }

    private void startCampaignLevel(final int lvl, final List<Powerup> powerups) {
        final String fileName = levels.get(lvl);
        if (fileName == null || fileName.isEmpty()) return;

        switch (lvl) {
            case 1:
                gameScreen.deployLevel(new TutorialLevel(lvl, gameInstance.getUserAccount(), this, annotator));
                break;
            default:
                gameScreen.deployLevel(new CampaignLevel(lvl, gameInstance.getUserAccount(), this) {
                    @Override
                    public void initialize(GameController controller) {
                        super.initialize(controller);
                        controller.loadLevelMap(fileName, LevelListParser.Source.INTERNAL);
                        for (Powerup powerup : powerups) {
                            controller.getBehaviourPack().roundManager.enablePowerup(powerup.type, powerup.count);
                        }
                    }

                    @Override
                    public void update(float delta, GameController.BehaviourPack behaviourPack, GameScreen.GameUI gameUI) {

                    }
                });
        }
        heartManager.consumeHeart();
    }

    public void updateInfo() {
        uiUserPanel.updateValues();
    }

    @Override
    public void show() {
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
            LevelButton button = levelButtons.get(stats.getActiveLevel());
            if (button.getStarsUnlocked() < stats.getStarsUnlocked())
                button.setStars(stats.getStarsUnlocked());

            if (stats.isLevelUnlocked()) {
                rewardsManager.giveRewardForLevel(stats.getActiveLevel(), gameInstance.getUserAccount(), stage);
                LevelButton levelButton = levelButtons.get(stats.getNextLevel());
                if (levelButton != null)
                    levelButton.setDisabled(false);
                else
                    new EndGameDialog(skin).show(stage,
                            Actions.sequence(
                                    Actions.alpha(0),
                                    Actions.delay(2f, Actions.fadeIn(.5f, Interpolation.smooth)
                                    )));
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
        private final HeartButton heartButton;
//        private final GoldBarButton goldBarButton;

        UIRightBar(final Stage stage, final Skin skin, final UserAccount user, final AdManager adManager, final FeedbackMailHandler feedbackMailHandler) {
            BUTTON_SIZE = skin.getFont("h4").getLineHeight() * 2.5f;

            lotteryButton = new LotteryButton(stage, skin, user, adManager);
            heartButton = new HeartButton(skin, user, adManager);
//            goldBarButton = new GoldBarButton(skin, user);

            ImageButton feedbackButton = UIFactory.createImageButton(skin, "ButtonFeedback");
            feedbackButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    feedbackMailHandler.createFeedbackMail();
                }
            });

            Table barGroup = new Table();
            barGroup.defaults().space(BUTTON_SIZE * .4f);
//            barGroup.add(goldBarButton.root)
//                    .center()
//                    .expandY()
//                    .row();
            barGroup.add(heartButton.root)
                    .expandY()
                    .bottom()
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
                        Lottery.Item<LotteryRewardSimple> reward = ((Lottery.Item<LotteryRewardSimple>) object);
                        switch (reward.getType()) {
                            case FIREBALL:
                                user.givePowerup(PowerupType.FIREBALL, reward.getAmount());
                                break;
                            case COLORBOMB:
                                user.givePowerup(PowerupType.COLORBOMB, reward.getAmount());
                                break;
                            case GOLD_BAR:
                                user.getCurrencyManager().giveCurrency(GOLD_BAR, reward.getAmount());
                                break;
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
                indicator.setVisible(user.getCurrencyManager().isCurrencyAvailable(CurrencyType.LOTTERY_TICKET));

                user.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void onChange(String name, Object newValue) {
                        if (name.equals(CurrencyType.LOTTERY_TICKET.name())) {
                            indicator.setVisible((int) newValue > 0);
                        }
                    }
                });
            }
        }

        private class HeartButton {
            private VerticalGroup root;
            private Stack heartStack;
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

                    @Override
                    public void canceled() {

                    }
                };

                lblLivesLeft = new Label("null", skin, "h3o");
                lblLivesLeft.setAlignment(Align.center);
                lblTimeForLife = new Label("null", skin, "h5");
                lblTimeForLife.setAlignment(Align.center);

                float buttonSize = BUTTON_SIZE * .8f;

                Image imgRedCross = new Image(skin, "RedCross");
                redCross = new Container<>(imgRedCross);
                redCross.size(buttonSize * .35f, UIUtils.getHeightFor(imgRedCross.getDrawable(), buttonSize * .35f));
//                redCross.setTransform(true);
//                redCross.addAction(Actions.forever(Actions.forever(Actions.sequence(
//                        Actions.scaleBy(.05f, .05f, .35f, Interpolation.smooth),
//                        Actions.scaleBy(-.05f, -.05f, .4f, Interpolation.smooth)
//                ))));

                Container<Container<Image>> redCrossWrapper = new Container<>(redCross);
                redCrossWrapper.top().right();

                Image backgroundImage = new Image(skin, "Heart");

                heartStack = new Stack();
                heartStack.setTransform(true);
                heartStack.add(backgroundImage);
                heartStack.add(lblLivesLeft);
                heartStack.add(redCrossWrapper);

//                redCross.setOrigin(Align.center);


                final Container<Stack> livesLeftWrapper = new Container<>(heartStack);
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
                            if (CoreSmash.DEV_MODE)
                                listener.reward("Not used parameters", 99999);
                            else
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

                heartStack.setTransform(true);
                heartStack.setOrigin(buttonSize / 2, buttonSize / 2);

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
                if (heartManager.isFull()) {
                    redCross.setVisible(false);
                    heartStack.clearActions();
                    heartStack.setScale(1);
                } else {
                    redCross.setVisible(true);
                    if (!heartStack.hasActions())
                        heartStack.addAction(Actions.forever(UIEffects.getEffect(UIEffects.Effect.bubbly_x, .4f)));
                }
            }
        }

        private class GoldBarButton {
            private Table root;
            private Label goldBarLabel;

            GoldBarButton(Skin skin, UserAccount userAccount) {

                root = new Table();
                Container<Image> goldBarWrapper = new Container<>(new Image(skin, "GoldBarWrapper"));
//                goldBarWrapper.addAction(Actions.forever(UIEffects.getEffect(UIEffects.Effect.bubbly_x)));
//                goldBarWrapper.setTransform(true);

                goldBarLabel = new Label(String.valueOf(userAccount.getCurrencyManager().getAmountOf(CurrencyType.GOLD_BAR)), skin, "h4s");
                userAccount.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void onChange(String name, Object newValue) {
                        if (name.equals(CurrencyType.GOLD_BAR.name()))
                            goldBarLabel.setText(String.valueOf(newValue));
                    }
                });
                Container<Label> goldBarTextWrapper = new Container<>(goldBarLabel);
                goldBarTextWrapper.setBackground(skin.getDrawable("GoldBarTextWrapper"));

                float labelWrapperHeight = goldBarLabel.getPrefHeight() * .9f;
                float goldBarWrapperHeight = BUTTON_SIZE * .9f;

                root.add(goldBarWrapper)
                        .size(goldBarWrapperHeight)
                        .padBottom(-labelWrapperHeight * .4f)
                        .row();
                root.add(goldBarTextWrapper)
                        .prefWidth(UIUtils.getWidthFor(goldBarTextWrapper.getBackground(), labelWrapperHeight))
                        .maxWidth(BUTTON_SIZE)
                        .height(labelWrapperHeight)
                        .row();

                goldBarWrapper.setOrigin(goldBarWrapperHeight / 2, goldBarWrapperHeight / 2);
            }

        }
    }

    private static class RewardsPerLevelManager {
        private Random rand;

        public RewardsPerLevelManager() {
            rand = new Random();
        }

        public void giveRewardForLevel(int level, UserAccount account, Stage stage) {
            if (rand.nextInt(100) < 25) {
                account.getCurrencyManager().giveCurrency(LOTTERY_TICKET);
                Components.showToast("You were rewarded 1x Lottery Key!", stage, 3);
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

            contentSize = WorldSettings.getSmallestScreenDimension() / 3;

            Table tblAccount = new Table();
            tblAccount.background(skin.getDrawable("UserAccountFrame"));
//            tblAccount.add(lblUserName).center()
//                    .padTop(-7 * Gdx.graphics.getDensity())
//                    .padBottom(1 * Gdx.graphics.getDensity()).row();
//            tblAccount.row().padBottom(lblLevel.getPrefHeight() / 3);
            tblAccount.add(userIconGroup)
                    .size(contentSize * .65f)
                    .padBottom(2 * Gdx.graphics.getDensity())
                    .grow().row();
            tblAccount.add(lblTotalXP).padBottom(0).left().row();
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
                if (playerInfo.avatar != null) {
                    Gdx.app.log("DB", String.valueOf(playerInfo.avatar.getHeight()));
                    Gdx.app.log("DB", String.valueOf(playerInfo.avatar.getWidth()));
                    btnUser.getStyle().imageUp = new TextureRegionDrawable(playerInfo.avatar);
                } else {
                    btnUser.getStyle().imageUp = skin.getDrawable("DefaultUserIcon");
                }
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
        private Label levelLabel, targetLabel;

        PickPowerUpsDialog(Skin skin, final UserAccount.PowerupManager powerUps) {
            super("", skin, "BlackBackOnly");

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

            levelLabel = UIFactory.createLabel("", skin, "h3", Align.center);
            levelLabel.setColor(new Color(153f / 255f, 46f / 255f, 103f / 255f, 1));
            targetLabel = new Label("", skin, "h4", Color.DARK_GRAY);

            Image background = new Image(skin, "DialogSelectPowerups");

            Table main = new Table(skin);
            main.top()
                    .padBottom(contentSize * .025f);
            main.add(levelLabel)
                    .expandX()
                    .center()
                    .padTop(contentSize * .04f)
                    .row();
            main.add(targetLabel)
                    .padTop(contentSize * .25f)
                    .center()
                    .row();
            main.add(new Label("Select powerups:", skin, "h4"))
                    .expand()
                    .bottom()
                    .row();
            main.add(powerupsGroup)
                    .width(contentSize)
                    .padBottom(Value.percentHeight(.15f, this))
                    .row();

            Stack contentStack = new Stack(background, main);


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

            float buttonHeight = WorldSettings.getDefaultButtonHeight() * 1.4f;
            Table buttons = getButtonTable();
            buttons.row().expandX();
            buttons.add(btnStart).height(buttonHeight).width(UIUtils.getWidthFor(btnStart.getImage().getDrawable(), buttonHeight));
//            buttons.add(btnClose).height(buttonHeight).width(UIUtils.getWidthFor(btnClose.getImage().getDrawable(), buttonHeight));

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

            getContentTable()
                    .add(contentStack)
                    .width(contentSize)
                    .height(UIUtils.getHeightFor(background.getDrawable(), contentSize));
//            getCell(getContentTable())
//                    .height(UIUtils.getHeightFor(getBackground(), contentSize) - buttons.getPrefHeight())
//                    .width(contentSize);
            getCell(buttons)
                    .padTop(Value.percentHeight(-.85f))
                    .maxWidth(contentSize);

            setClip(false);
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
            levelLabel.setText("Level " + lvl);

            LevelParser.LevelInfo levelInfo = LevelParser.getLevelInfo(levels.get(lvl), LevelListParser.Source.INTERNAL);
            targetLabel.setText("");
            if (levelInfo.gameTargets.contains(GameTarget.SCORE))
                targetLabel.setText("Collect " + levelInfo.targetScores.one + " points\n");
            if (levelInfo.gameTargets.contains(GameTarget.ASTRONAUTS))
                targetLabel.setText(targetLabel.getText() + "Save all Astronauts");

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

    private static class EndGameDialog extends Dialog {

        public EndGameDialog(Skin skin) {
            super("", skin, "PickPowerUpDialog");
            float width = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();

            Label lblTitle = new Label("To be continued", skin, "h3");
            Label lblMessage = new Label(FileUtils.fileToString(Gdx.files.internal("docs/end_message").reader(512)), skin, "h5");
            lblMessage.setWrap(true);
            ScrollPane scrollPane = new ScrollPane(lblMessage);
            scrollPane.setScrollingDisabled(true, false);
            scrollPane.setOverscroll(false, false);

            getContentTable().defaults().space(0);
            getContentTable().add(lblTitle)
                    .padBottom(lblTitle.getPrefHeight() / 2)
                    .row();
            getContentTable().add(scrollPane).padLeft(width * .025f).padRight(width * .025f).grow();
            getCell(getContentTable()).width(width).padBottom(width * .025f);
            getCell(getButtonTable()).width(width);
            setModal(true);
            setMovable(false);

            ImageButton btn = UIFactory.createImageButton(skin, "ButtonCloseGreen");
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