package com.breakthecore.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.breakthecore.Lottery;
import com.breakthecore.tiles.PowerupType;

public class LotteryDialog extends Dialog {
    final private Skin skin;
    private CardButton[] cardButtons;
    private TextButton btnClaim;
    private Lottery lottery;
    private Reward reward;

    public LotteryDialog(Skin sk) {
        super("", sk, "PickPowerUpDialog");
        skin = sk;
        lottery = new Lottery();
        reward = new Reward();

        btnClaim = UIFactory.createTextButton("Claim Reward!", skin, "dialogButton");
        btnClaim.getLabelCell().pad(Value.percentHeight(.5f, btnClaim.getLabel()));
        btnClaim.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                result(reward);
                hide();
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
                                    getButtonTable().add(btnClaim).pad(5 * Gdx.graphics.getDensity());
                                    pack();
                                }
                            })
                    ));
                }
            });
        }

        Table contents = getContentTable();

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
                    hide(null);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public Dialog show(Stage stage) {
        for (CardButton btn : cardButtons) {
            btn.setDisabled(false);
            btn.hideReward();
            btn.imgReward.setDrawable(skin, "FIREBALL");
        }
        getButtonTable().clearChildren();
        return super.show(stage);
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
    }

    private static class CardButton extends Button {
        Image imgReward;
        Image shade;
        Label lblReward;
        float heightToWidthRatio;

        CardButton(Skin skin) {
            super(skin);
            imgReward = new Image();

            Image background = new Image(skin.getDrawable("cardBack"));
            imgReward = new Image();
            imgReward.setScaling(Scaling.fit);
            lblReward = new Label("1x", skin, "h3");
            lblReward.setAlignment(Align.bottom);

            shade = new Image(skin.getDrawable("cardShade"));

            heightToWidthRatio = background.getDrawable().getMinHeight() / background.getDrawable().getMinWidth();

            Stack stack = new Stack(background, shade, imgReward, lblReward);
            add(stack).grow().pad(3 * Gdx.graphics.getDensity());
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