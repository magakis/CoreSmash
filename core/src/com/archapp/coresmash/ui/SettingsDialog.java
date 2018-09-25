package com.archapp.coresmash.ui;

import com.archapp.coresmash.GameSettings;
import com.archapp.coresmash.WorldSettings;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class SettingsDialog extends Dialog {
    private float contentWidth;
    private CheckBox menuMusicEnabled, gameMusicEnabled, soundEffectsEnabled;

    public SettingsDialog(Skin skin) {
        super("", skin, "PickPowerUpDialog");

        contentWidth = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();

        menuMusicEnabled = UIFactory.createCheckBox("Menu Music", skin);
        menuMusicEnabled.setChecked(GameSettings.get().isMenuMusicEnabled());
        menuMusicEnabled.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.get().setMenuMusicEnabled(menuMusicEnabled.isChecked());
            }
        });

        gameMusicEnabled = UIFactory.createCheckBox("Game Music", skin);
        gameMusicEnabled.setChecked(GameSettings.get().isGameMusicEnabled());
        gameMusicEnabled.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.get().setGameMusicEnabled(gameMusicEnabled.isChecked());
            }
        });

        soundEffectsEnabled = UIFactory.createCheckBox("Sound Effects", skin);
        soundEffectsEnabled.setChecked(GameSettings.get().isSoundEffectsEnabled());
        soundEffectsEnabled.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.get().setSoundEffectsEnabled(soundEffectsEnabled.isChecked());
            }
        });

        Table content = getContentTable();
        getCell(content).width(contentWidth);

        Table checkboxes = new Table(skin);
        checkboxes.defaults().expandX().padBottom(menuMusicEnabled.getPrefHeight() / 2).left();
        checkboxes.columnDefaults(0).padRight(menuMusicEnabled.getPrefHeight());
        checkboxes.add(menuMusicEnabled);
        checkboxes.add(gameMusicEnabled).row();
        checkboxes.add(soundEffectsEnabled);
        checkboxes.add();

        Label title = UIFactory.createLabel("Settings", skin, "h2", Align.center);

        ImageButton btnReturn = UIFactory.createImageButton(skin, "ButtonReturn");
        btnReturn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        content.defaults().padBottom(contentWidth * .02f);
        content.add(title).padTop(title.getPrefHeight() * -.2f).row();
        content.add(checkboxes).row();

        float buttonSize = WorldSettings.getDefaultButtonHeight();
        getButtonTable().add(btnReturn).height(buttonSize).width(UIUtils.getWidthFor(btnReturn.getImage().getDrawable(), buttonSize));

        setMovable(false);
        setResizable(false);
    }

}
