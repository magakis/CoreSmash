package com.coresmash.ui;

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
import com.coresmash.tiles.TileType;

public class LotteryDialog extends Dialog {
    final private Skin skin;
    final private com.coresmash.UserAccount user;

    private CardButton[] cardButtons;
    private ImageButton btnClose, btnOpen, btnClaim, btnRetry;
    private com.coresmash.Lottery lottery;
    private Reward reward;


    public LotteryDialog(Skin sk, com.coresmash.UserAccount userAccount) {
        super("", sk, "PickPowerUpDialog");
        this.user = userAccount;
        skin = sk;
        lottery = new com.coresmash.Lottery();
        reward = new Reward();

        btnClaim = UIFactory.createImageButton(skin, "ButtonClaim");
        btnClaim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                result(reward);
                hide();
            }
        });

        btnOpen = UIFactory.createImageButton(skin, "ButtonOpen");
        btnOpen.addListener(new ChangeListener() {
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

        btnClose = UIFactory.createImageButton(skin, "ButtonClose");
        btnClose.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        btnRetry = UIFactory.createImageButton(skin, "ButtonRetry");
        btnRetry.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                result(reward);
                restart();
                btnOpen.setChecked(!btnOpen.isChecked()); // trigger ChangeListener of button
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
                                                  com.coresmash.Lottery.Item item = lottery.draw();
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
                                    showImageButton(btnClaim);
                                    showImageButton(btnRetry);
                                    btnRetry.setDisabled(user.getLotteryCoins() <= 0);
                                    pack();
                                }
                            })
                    ));
                }
            });
        }

        Table contents = getContentTable();
        for (CardButton ib : cardButtons) {
            contents.add(ib).space(5 * Gdx.graphics.getDensity()).size(Value.percentWidth(.25f, UIUtils.getScreenActor(ib)), Value.percentWidth(ib.heightToWidthRatio * .25f, UIUtils.getScreenActor(ib)));
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

        pad(15 * Gdx.graphics.getDensity());
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
        getButtonTable().clearChildren();
        showImageButton(btnOpen);
        showImageButton(btnClose);
        btnOpen.setDisabled(user.getLotteryCoins() <= 0);
        reward.reset();
    }

    private void showImageButton(ImageButton button) {
        float ratio = 0.25f;
        Table table = getButtonTable();
        table.add(button).height(getContentTable().getPrefHeight() * ratio).width(UIUtils.getWidthFor(button.getImage().getDrawable(), getContentTable().getPrefHeight() * ratio));
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