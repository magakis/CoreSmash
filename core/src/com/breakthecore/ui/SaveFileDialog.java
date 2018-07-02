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
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class SaveFileDialog extends Dialog {
    private final List<String> levelsFound;
    private final FilenameFilter levelBuilderFilter;
    private final TextField textField;

    public SaveFileDialog(Skin skin, WindowStyle windowStyle) {
        super("", windowStyle);

        levelBuilderFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !s.startsWith("_editor_") && s.toLowerCase().endsWith(".xml");
            }
        };

        List.ListStyle ls = new List.ListStyle();
        ls.fontColorSelected = Color.WHITE;
        ls.fontColorUnselected = Color.WHITE;
        ls.selection = skin.newDrawable("boxSmall", 0, 0, 0, 0);
        ls.font = skin.getFont("h5");

        levelsFound = new List<>(ls);

        TextField.TextFieldStyle tfstyle = new TextField.TextFieldStyle();
        tfstyle.font = skin.getFont("h4");
        tfstyle.fontColor = Color.WHITE;
        tfstyle.background = skin.getDrawable("boxSmall");

        textField = new TextField("", tfstyle);
        textField.setMessageText("Requires 3-20 characters");
        textField.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isLetterOrDigit(c) || c == '_';
            }
        });
        textField.setMaxLength(20);

        TextButton tbSave = new TextButton("Save", skin, "dialogButton");
        tbSave.getLabelCell()
                .pad(Value.percentHeight(1, tbSave.getLabel()))
                .padTop(Value.percentHeight(.5f, tbSave.getLabel()))
                .padBottom(Value.percentHeight(.5f, tbSave.getLabel()));
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

        TextButton tbCancel = new TextButton("Cancel", skin, "dialogButton");
        tbCancel.getLabelCell()
                .pad(Value.percentHeight(1, tbCancel.getLabel()))
                .padTop(Value.percentHeight(.5f, tbCancel.getLabel()))
                .padBottom(Value.percentHeight(.5f, tbCancel.getLabel()));
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

        float lineHeight = levelsFound.getStyle().font.getLineHeight();
        Table content = getContentTable();
        content.pad(lineHeight)
                .padBottom(0);

        content.add(sp)
                .grow()
                .maxWidth(Value.percentWidth(.75f, UIUtils.getScreenActor(sp)))
                .maxHeight(Value.percentHeight(.5f, UIUtils.getScreenActor(sp)));

        Table buttons = getButtonTable();
        buttons.pad(Value.percentHeight(.25f, tbSave));
        buttons.add(textField).colspan(2).growX().row();
        buttons.add(tbSave).padRight(Value.percentHeight(.5f, tbSave)).expandX().left();
        buttons.add(tbCancel).expandX().right();
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
