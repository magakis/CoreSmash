package com.archapp.coresmash.ui;

import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.Lottery;
import com.archapp.coresmash.Lottery.Item;
import com.archapp.coresmash.UserAccount;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.platform.AdManager;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import static com.archapp.coresmash.CurrencyType.LOTTERY_TICKET;

public class LotteryDialog extends Dialog {
    private float contentWidth;
    private float buttonRatio;

    final private Skin skin;
    final private UserAccount.CurrencyManager currencies;

    private CardButton[] cardButtons;
    private ImageButton btnClose, btnOpen, btnClaim, btnRetry, btnFreePick;
    private Lottery<LotteryRewardSimple> lottery;


    public LotteryDialog(Skin sk, UserAccount.CurrencyManager currencyManager, final AdManager adManager) {
        super("", sk, "SimpleDarkPurpleDialog");
        currencies = currencyManager;
        skin = sk;
        lottery = createLottery();

        contentWidth = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();
        buttonRatio = WorldSettings.DefaultRatio.dialogButtonToContent();

        btnClaim = UIFactory.createImageButton(skin, "ButtonClaim");
        btnClaim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        btnOpen = UIFactory.createImageButton(skin, "ButtonPick");
        btnOpen.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currencies.isCurrencyAvailable(LOTTERY_TICKET)) {
                    currencies.consumeCurrency(LOTTERY_TICKET);
                    pickACard();
                } else {
                    throw new RuntimeException("Coins:" + currencies.getAmountOf(LOTTERY_TICKET));
                }
            }
        });

        btnClose = UIFactory.createImageButton(skin, "ButtonClose");
        btnClose.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        btnRetry = UIFactory.createImageButton(skin, "ButtonPickAgain");
        btnRetry.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currencies.isCurrencyAvailable(LOTTERY_TICKET)) {
                    currencies.consumeCurrency(LOTTERY_TICKET);
                    pickACard();
                } else {
                    throw new RuntimeException("Coins:" + currencies.getAmountOf(LOTTERY_TICKET));
                }
            }
        });

        btnFreePick = UIFactory.createImageButton(skin, "ButtonFreePick");
        btnFreePick.addListener(new ChangeListener() {
            AdManager.AdRewardListener listener = new AdManager.AdRewardListener() {
                @Override
                public void reward(String type, int amount) {
                    pickACard();
                }
            };

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (CoreSmash.DEV_MODE)
                    pickACard();
                else
                    adManager.showAdForReward(listener, AdManager.VideoAdRewardType.LOTTERY_COIN);
            }
        });

        cardButtons = new CardButton[3];
        for (int i = 0; i < cardButtons.length; ++i) {
            final CardButton button = new CardButton(skin);
            cardButtons[i] = button;
            button.setTransform(true);
            button.addListener(new ChangeListener() {
                Item<LotteryRewardSimple> reward;

                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    actor.addAction(Actions.sequence(
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    for (CardButton btn : cardButtons) {
                                        btn.setDisabled(true);
                                    }
                                    button.setOrigin(button.getWidth() / 2, button.getHeight() / 2);
                                }
                            })
                            , Actions.scaleBy(-1, 0, 0.5f)
                            , Actions.run(new Runnable() {
                                              @Override
                                              public void run() {
                                                  reward = lottery.draw();
                                                  button.setReward(
                                                          skin.getDrawable(reward.getType().drawableName),
                                                          reward.getAmount());
                                                  button.showReward();
                                              }
                                          }
                            )
                            , Actions.scaleBy(1, 0, 0.5f)
                            , Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    if (currencies.isCurrencyAvailable(LOTTERY_TICKET))
                                        showImageButton(btnRetry);
                                    else
                                        showImageButton(btnFreePick);

                                    showImageButton(btnClaim);
                                    result(reward);
                                    pack();
                                }
                            })
                    ));
                }
            });
        }

        final Table contents = getContentTable();
        getCell(contents).width(contentWidth);

        contents.columnDefaults(0).left().expandX();
        contents.columnDefaults(1).expandX();
        contents.columnDefaults(2).right().expandX();
        contents.padBottom(contentWidth * .025f);

        float cardSize = contentWidth / 3.2f;
        for (CardButton ib : cardButtons) {
            contents.add(ib).width(cardSize).height(UIUtils.getHeightFor(ib.cardBack, cardSize));
        }

        getButtonTable().defaults().expandX();
        setMovable(false);
        setResizable(false);
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
//                    hide(null);
                    return true;
                }
                return false;
            }
        });

    }


    @Override
    public Dialog show(Stage stage) {
        restart();
        return super.show(stage);
    }

    private void pickACard() {
        resetCards();
        enableCards();
        getButtonTable().clearChildren();
        pack();
    }

    private void enableCards() {
        for (CardButton btn : cardButtons) {
            btn.setDisabled(false);
        }
    }

    private void resetCards() {
        for (CardButton btn : cardButtons) {
            btn.setDisabled(true);
            btn.hideReward();
        }
    }

    private void restart() {
        resetCards();
        getButtonTable().clearChildren();
        if (currencies.isCurrencyAvailable(LOTTERY_TICKET))
            showImageButton(btnOpen);
        else
            showImageButton(btnFreePick);

        showImageButton(btnClose);
    }

    private void showImageButton(ImageButton button) {
        float buttonSize = contentWidth * buttonRatio;
        Table table = getButtonTable();
        table.add(button).height(buttonSize).width(UIUtils.getWidthFor(button.getImage().getDrawable(), buttonSize));
    }

    private Lottery<LotteryRewardSimple> createLottery() {
        Lottery<LotteryRewardSimple> lottery = new Lottery<>();

//        lottery.addPossibleItem(LotteryRewardSimple.GOLD_BAR, 1, 15);
//        lottery.addPossibleItem(LotteryRewardSimple.GOLD_BAR, 2, 5);
//        lottery.addPossibleItem(LotteryRewardSimple.GOLD_BAR, 3, 1);
        lottery.addPossibleItem(LotteryRewardSimple.COLORBOMB, 1, 15);
        lottery.addPossibleItem(LotteryRewardSimple.COLORBOMB, 2, 5);
        lottery.addPossibleItem(LotteryRewardSimple.COLORBOMB, 3, 1);
        lottery.addPossibleItem(LotteryRewardSimple.FIREBALL, 1, 15);
        lottery.addPossibleItem(LotteryRewardSimple.FIREBALL, 2, 5);
        lottery.addPossibleItem(LotteryRewardSimple.FIREBALL, 3, 1);

        return lottery;
    }

    private static class CardButton extends Button {
        private Drawable cardBack, disabledCardback, cardFront;

        private Image imgBackground, imgReward;
        private Label lblReward;

        CardButton(Skin skin) {
            super(skin, "TransWithHighlight");
            imgReward = new Image();

            cardBack = skin.getDrawable("cardBack");
            disabledCardback = skin.newDrawable("cardBack", Color.DARK_GRAY);
            cardFront = skin.getDrawable("LotteryCardFront");

            imgBackground = new Image(cardBack);
            imgReward = new Image(null, Scaling.fit);
            lblReward = UIFactory.createLabel("null", skin, "h3", Align.bottom);


            Container<Image> rewardWrapper = new Container<>(imgReward);
            rewardWrapper.pad(Value.percentWidth(.15f, this));

            Stack stack = new Stack(imgBackground, rewardWrapper, lblReward);
            add(stack).grow();
        }

        @Override
        public void setDisabled(boolean isDisabled) {
            imgBackground.setDrawable(isDisabled ? disabledCardback : cardBack);
            super.setDisabled(isDisabled);
        }

        void setReward(Drawable drawable, int amount) {
            imgReward.setDrawable(drawable);
            imgReward.invalidateHierarchy();
            lblReward.setText(amount + "x");
        }

        void showReward() {
            imgBackground.setDrawable(cardFront);
            imgReward.setVisible(true);
            lblReward.setVisible(true);
        }

        void hideReward() {
            imgReward.setVisible(false);
            lblReward.setVisible(false);
        }
    }

    public enum LotteryRewardSimple {
        FIREBALL("FIREBALL"),
        COLORBOMB("COLORBOMB"),
        GOLD_BAR("GoldBar");

        LotteryRewardSimple(String drawableName) {
            this.drawableName = drawableName;
        }

        private String drawableName;

        public String getDrawableName() {
            return drawableName;
        }
    }
}