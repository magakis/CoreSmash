package com.archapp.coresmash.ui;

import com.archapp.coresmash.AdManager;
import com.archapp.coresmash.UserAccount.HeartManager;
import com.archapp.coresmash.WorldSettings;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;


public class HeartReplenishDialog extends Dialog {

    public HeartReplenishDialog(Skin skin, final HeartManager heartManager, final AdManager adManager) {
        super("", skin, "PickPowerUpDialog");

        ImageButton freeHeart = UIFactory.createImageButton(skin, "ButtonFreeHeart");
        freeHeart.addListener(new ChangeListener() {
            AdManager.AdRewardListener listener = new AdManager.AdRewardListener() {
                @Override
                public void reward(String type, int amount) {
                    heartManager.restoreHeart();
                    hide();
                }
            };

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                adManager.showAdForReward(listener, AdManager.VideoAdRewardType.HEART);
            }
        });

        float contentWidth = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();

        Label title = new Label("No more hearts", skin, "h3");
        Label description = new Label("You run out of hearts! You can wait for them to replenish or watch an ad.", skin, "h5");
        description.setWrap(true);

        Table content = getContentTable();
        getCell(content).width(contentWidth);
        content.defaults().padBottom(title.getPrefHeight() / 2);
        content.add(title)
                .row();
        content.add(description)
                .width(contentWidth * .8f);

        float buttonHeight = WorldSettings.DefaultRatio.dialogButtonToContent() * contentWidth;
        Table buttons = getButtonTable();
        buttons.add(freeHeart).height(buttonHeight).width(UIUtils.getWidthFor(freeHeart.getImage().getDrawable(), buttonHeight));

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    hide();
                    return true;
                }
                return false;
            }
        });
    }
}
