package com.breakthecore.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.breakthecore.WorldSettings;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class AssignLevelDialog extends Dialog {
    private final List<String> levelsFound;
    private final FilenameFilter levelBuilderFilter;
    private final TextField textField;

    public AssignLevelDialog(Skin skin, WindowStyle windowStyle) {
        super("", windowStyle);

        levelBuilderFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !s.startsWith("_") && s.toLowerCase().endsWith(".xml");
            }
        };

        List.ListStyle ls = new List.ListStyle();
        ls.fontColorSelected = Color.WHITE;
        ls.fontColorUnselected = Color.WHITE;
        ls.selection = skin.newDrawable("box_white_5", 0, 0, 0, 0);
        ls.font = skin.getFont("h5");

        levelsFound = new List<>(ls);

        TextField.TextFieldStyle tfstyle = new TextField.TextFieldStyle();
        tfstyle.font = skin.getFont("h4");
        tfstyle.fontColor = Color.WHITE;
        tfstyle.background = skin.getDrawable("box_white_5");

        textField = new TextField("", tfstyle);
        textField.setMessageText("Requires 3-20 characters");
        textField.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isLetterOrDigit(c);
            }
        });
        textField.setMaxLength(20);

        TextButton tbSave = new TextButton("Save", skin);
        tbSave.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                String name = textField.getText();
                if (name.length() >= 3) {
                    result(name);
                } else {
                    result(null);
                }
            }
        });

        TextButton tbCancel = new TextButton("Cancel", skin);
        tbCancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        ScrollPane sp = new ScrollPane(levelsFound);
        sp.setScrollingDisabled(true, false);
        sp.setOverscroll(false, false);

        setMovable(false);
        setResizable(false);
        setKeepWithinStage(true);
        setModal(true);

        padTop(10);

        Table content = getContentTable();
        content.pad(20).padBottom(0).add(sp).colspan(2).fill().width(WorldSettings.getWorldWidth() / 2).height(400).left().row();

        Table buttons = getButtonTable();
        buttons.pad(20);
        buttons.add(textField).colspan(2).padBottom(20).growX().row();
        buttons.add(tbSave).width(170).height(100).padRight(20);
        buttons.add(tbCancel).width(170).height(100);

    }

    public Dialog show(Stage stage, String defText) {
        levelsFound.clearItems();
        String[] files = Gdx.files.external("/CoreSmash/levels/").file().list(levelBuilderFilter);
        Arrays.sort(files);
        levelsFound.setItems(files);
        textField.setText(defText);
        super.show(stage);
        return this;
    }
}

