package com.breakthecore.ui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

public final class UIFactory {
    private UIFactory() {
    }

    public static Label createLabel(String text, Skin skin, String style, int align) {
        Label tmp = new Label(text,skin, style);
        tmp.setAlignment(align);
        return tmp;
    }

    public static CheckBox createCheckBox(String name, Skin skin) {
        CheckBox cb = new CheckBox(name, skin);
        cb.getImageCell().width(cb.getLabel().getPrefHeight()).height(cb.getLabel().getPrefHeight()).padRight(15);
        cb.getImage().setScaling(Scaling.fill);
        return cb;
    }

    public static TextButton createTextButton(String txt, Skin skin, String stylename) {
        TextButton button = new TextButton(txt, skin, stylename);
        button.addListener(UIUtils.getButtonSoundListener());
        return button;
    }

    public static TextButton createTextButton(String txt, Skin skin) {
        TextButton button = new TextButton(txt, skin);
        button.addListener(UIUtils.getButtonSoundListener());
        return button;
    }

    public static TextButton createTextButton(String txt, TextButton.TextButtonStyle style) {
        TextButton button = new TextButton(txt, style);
        button.addListener(UIUtils.getButtonSoundListener());
        return button;
    }
}
