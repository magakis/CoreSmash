package com.breakthecore.ui;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.breakthecore.Lottery;
import com.breakthecore.UserAccount;
import com.breakthecore.tiles.TileType.PowerupType;

public class LotteryDialog extends Dialog {
    final private Skin skin;
    final private UserAccount user;

    private CardButton[] cardButtons;
    private TextButton btnClaim, btnBegin, btnExit, btnRetry;
    private Lottery lottery;
    private Reward reward;

    public LotteryDialog(Skin sk, UserAccount userAccount) {
        super("", sk, "PickPowerUpDialog");
        this.user = userAccount;
        skin = sk;
        lottery = new Lottery();
        reward = new Reward();

        btnClaim = UIFactory.createTextButton("Claim Reward!", skin, "dialogButton");
//        btnClaim.getLabelCell().pad(Value.percentHeight(.5f, btnClaim.getLabel()));
        btnClaim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                result(reward);
                hide();
            }
        });

        btnBegin = UIFactory.createTextButton("Start!", skin, "dialogButton");
//        btnBegin.getLabelCell().pad(Value.percentHeight(.5f, btnBegin.getLabel()));
        btnBegin.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (user.consumeLotteryCoin()) {
                    for (CardButton btn : cardButtons) {
                        btn.setDisabled(false);
                    }
                    getButtonTable().clearChildren();
                    pack();
                } else {
                    throw new RuntimeException("Coins:" + user.getLotteryCoins());
                }
            }
        });

        btnExit = UIFactory.createTextButton("Cancel", skin, "dialogButton");
//        btnExit.getLabelCell().pad(Value.percentHeight(.5f, btnExit.getLabel()));
        btnExit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        btnRetry = UIFactory.createTextButton("Try Again!", skin, "dialogButton");
//        btnRetry.getLabelCell().pad(Value.percentHeight(.5f, btnRetry.getLabel()));
        btnRetry.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                result(reward);
                restart();
                btnBegin.setChecked(!btnBegin.isChecked());
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
                                    //.pad(5 * Gdx.graphics.getDensity())
                                    getButtonTable().add(btnClaim);
                                    getButtonTable().add(btnRetry);
                                    btnRetry.setDisabled(user.getLotteryCoins() <= 0);
                                    pack();
                                }
                            })
                    ));
                }
            });
        }

        Table contents = getContentTable();
        contents.pad(5 * Gdx.graphics.getDensity());
        contents.padBottom(0);

        for (CardButton ib : cardButtons) {
            contents.add(ib).pad(5 * Gdx.graphics.getDensity()).size(Value.percentWidth(.25f, UIUtils.getScreenActor(ib)), Value.percentWidth(ib.heightToWidthRatio * .25f, UIUtils.getScreenActor(ib)));
            ib.pack();
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

        getButtonTable().padBottom(4 * Gdx.graphics.getDensity());
    }

    @Override
    public Dialog show(Stage stage) {
        restart();
        return super.show(stage);
    }

    private void restart() {
        for (CardButton btn : cardButtons) {
            btn.setDisabled(true);
            btn.hideReward();
        }
        Table buttons = getButtonTable();
        buttons.clearChildren();
        buttons.add(btnBegin);
        buttons.add(btnExit);
        btnBegin.setDisabled(user.getLotteryCoins() <= 0);
        reward.reset();
//        pack();
    }

    public static class Reward {
        PowerupType type;
        int amount;

        private Reward() {
        }

        private Reward(PowerupType type, int amount) {
            this.type = type;
            this.amount = amount;
        }

        public void set(PowerupType type, int amount) {
            this.type = type;
            this.amount = amount;
        }

        public PowerupType getType() {
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
        Drawable disabledCardback;
        Drawable cardBack;

        Image imgBackground;
        Image imgReward;
        Image shade;
        Label lblReward;
        float heightToWidthRatio;

        CardButton(Skin skin) {
            super(skin);
            imgReward = new Image();

            cardBack = skin.getDrawable("cardBack");
            disabledCardback = skin.newDrawable("cardBack", Color.GRAY);

            imgBackground = new Image(cardBack);
            imgReward = new Image();
            imgReward.setScaling(Scaling.fit);
            lblReward = new Label("~", skin, "h3");
            lblReward.setAlignment(Align.bottom);

            shade = new Image(skin.getDrawable("cardShade"));

            heightToWidthRatio = imgBackground.getDrawable().getMinHeight() / imgBackground.getDrawable().getMinWidth();

            Stack stack = new Stack(imgBackground, shade, imgReward, lblReward);
            add(stack).grow().pad(3 * Gdx.graphics.getDensity());
        }

        @Override
        public void setDisabled(boolean isDisabled) {
            imgBackground.setDrawable(isDisabled ? disabledCardback : cardBack);
            super.setDisabled(isDisabled);
        }

        void setReward(PowerupType type, int amount) {
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