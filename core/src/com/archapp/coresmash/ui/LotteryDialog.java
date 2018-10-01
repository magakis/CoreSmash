package com.archapp.coresmash.ui;

import com.archapp.coresmash.Lottery;
import com.archapp.coresmash.UserAccount;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.platform.AdManager;
import com.archapp.coresmash.tiles.TileType;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
    private Lottery lottery;
    private Reward reward;


    public LotteryDialog(Skin sk, UserAccount.CurrencyManager currencyManager, final AdManager adManager) {
        super("", sk, "PickPowerUpDialog");
        currencies = currencyManager;
        skin = sk;
        lottery = new Lottery();
        reward = new Reward();

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
                result(reward);
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
                adManager.showAdForReward(listener, AdManager.VideoAdRewardType.LOTTERY_COIN);
            }
        });

        cardButtons = new CardButton[3];
        for (int i = 0; i < cardButtons.length; ++i) {
            final CardButton button = new CardButton(skin);
            cardButtons[i] = button;
            button.setTransform(true);
            button.addListener(new ChangeListener() {
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
                                                  Lottery.Item item = lottery.draw();
                                                  reward.set(item.getType(), item.getAmount());
                                                  button.setReward(item.getType(), item.getAmount());
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
        contents.columnDefaults(0).padRight(contentWidth * .025f);
        contents.columnDefaults(1).padRight(contentWidth * .025f);
        contents.padBottom(contentWidth * .025f);
        getCell(contents).width(contentWidth);
        float cardSize = contentWidth * .3f;
        for (final CardButton ib : cardButtons) {
            contents.add(ib).width(cardSize).height(UIUtils.getHeightFor(ib.cardBack, cardSize));
        }

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

        getButtonTable().defaults().expandX();//space((contentWidth - 2 * (contentWidth * buttonRatio)) / 4);
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
        reward.reset();
    }

    private void showImageButton(ImageButton button) {
        float buttonSize = contentWidth * buttonRatio;
        Table table = getButtonTable();
        table.add(button).height(buttonSize).width(UIUtils.getWidthFor(button.getImage().getDrawable(), buttonSize));
    }

    public static class Reward {
        TileType.PowerupType type;
        int amount;

        private Reward() {
        }

        private Reward(TileType.PowerupType type, int amount) {
            this.type = type;
            this.amount = amount;
        }

        public void set(TileType.PowerupType type, int amount) {
            this.type = type;
            this.amount = amount;
        }

        public TileType.PowerupType getType() {
            return type;
        }

        public int getAmount() {
            return amount;
        }

        private void reset() {
            type = null;
            amount = 0;
        }
    }

    private static class CardButton extends Button {
        private Drawable disabledCardback;
        private Drawable cardBack;

        private Image imgBackground;
        private Image imgReward;
        private Image shade;
        private Label lblReward;
        private float heightToWidthRatio;

        CardButton(Skin skin) {
            super(skin, "TransWithHighlight");
            imgReward = new Image();

            cardBack = skin.getDrawable("cardBack");
            disabledCardback = skin.newDrawable("cardBack", Color.DARK_GRAY);

            imgBackground = new Image(cardBack);
            imgReward = new Image();
            imgReward.setScaling(Scaling.fit);
            lblReward = new Label("null", skin, "h3");
            lblReward.setAlignment(Align.bottom);

            shade = new Image(skin.getDrawable("cardShade"));

            heightToWidthRatio = imgBackground.getDrawable().getMinHeight() / imgBackground.getDrawable().getMinWidth();

            Stack stack = new Stack(imgBackground, shade, imgReward, lblReward);
            add(stack).grow();
        }

        @Override
        public void setDisabled(boolean isDisabled) {
            imgBackground.setDrawable(isDisabled ? disabledCardback : cardBack);
            super.setDisabled(isDisabled);
        }

        void setReward(TileType.PowerupType type, int amount) {
            if (type == null) {
                imgReward.setDrawable(null);
            } else {
                imgReward.setDrawable(getSkin().getDrawable(type.name()));
            }
            lblReward.setText(amount + "x");
        }

        void showReward() {
            shade.setVisible(true);
            imgReward.setVisible(true);
            lblReward.setVisible(true);
        }

        void hideReward() {
            shade.setVisible(false);
            imgReward.setVisible(false);
            lblReward.setVisible(false);
        }

    }
}