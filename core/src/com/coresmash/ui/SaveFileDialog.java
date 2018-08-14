package com.coresmash.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.coresmash.levelbuilder.LevelListParser;

public class SaveFileDialog extends Dialog {
    private final List<LevelListParser.RegisteredLevel> levelList;
    private final Array<LevelListParser.RegisteredLevel> levels;
    private final LevelListParser levelListParser;
    private final TextField textField;
    private final Dialog dlgConfirmOverwrite;

    public SaveFileDialog(Skin skin) {
        super("", skin);

        levels = new Array<>();
        levelListParser = new LevelListParser();
        levelList = new List<>(skin);

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

        dlgConfirmOverwrite = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    endDialog(textField.getText());
                }
            }
        };
        dlgConfirmOverwrite.text(new Label("", skin, "h4f"));
        dlgConfirmOverwrite.button(UIFactory.createTextButton("Yes", skin, "dialogButton"), true);
        dlgConfirmOverwrite.button(UIFactory.createTextButton("No", skin, "dialogButton"), false);
        Cell<Label> txtCell = dlgConfirmOverwrite.getContentTable().getCells().get(0);
        dlgConfirmOverwrite.getCell(dlgConfirmOverwrite.getContentTable()).padBottom(txtCell.getActor().getStyle().font.getLineHeight());

        txtCell = ((TextButton) dlgConfirmOverwrite.getButtonTable().getCells().get(0).getActor()).getLabelCell();
        txtCell.pad(Value.percentHeight(1f, txtCell.getActor()));
        txtCell.padTop(Value.percentHeight(0.5f, txtCell.getActor()));
        txtCell.padBottom(Value.percentHeight(0.5f, txtCell.getActor()));

        txtCell = ((TextButton) dlgConfirmOverwrite.getButtonTable().getCells().get(1).getActor()).getLabelCell();
        txtCell.pad(Value.percentHeight(1f, txtCell.getActor()));
        txtCell.padTop(Value.percentHeight(0.5f, txtCell.getActor()));
        txtCell.padBottom(Value.percentHeight(0.5f, txtCell.getActor()));
        dlgConfirmOverwrite.getButtonTable().getCells().get(0).expandX().center();
        dlgConfirmOverwrite.getButtonTable().getCells().get(1).expandX().center();
        dlgConfirmOverwrite.pad(Value.percentHeight(.8f, txtCell.getActor()));

        TextButton tbSave = UIFactory.createTextButton("Save", skin, "dialogButton");
        tbSave.getLabelCell()
                .pad(Value.percentHeight(1, tbSave.getLabel()))
                .padTop(Value.percentHeight(.5f, tbSave.getLabel()))
                .padBottom(Value.percentHeight(.5f, tbSave.getLabel()));
        tbSave.addListener(new ChangeListener() {
            private LevelListParser.RegisteredLevel dummySearchLevel = new LevelListParser.RegisteredLevel(0, "");

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = textField.getText();
                if (name.length() >= 3 && !name.equals("_editor_")) {
                    dummySearchLevel.name = name;
                    if (levelList.getItems().contains(dummySearchLevel, false)) {
                        String chosen = textField.getText();
                        ((Label) dlgConfirmOverwrite.getContentTable().getCells().get(0).getActor())
                                .setText("Overwrite level '[GREEN]" + chosen + "[]'?");
                        dlgConfirmOverwrite.show(getStage());
                    } else {
                        endDialog(name);
                    }
                } else {
                    endDialog(null);
                }
            }
        });

        final TextButton tbCancel = UIFactory.createTextButton("Cancel", skin, "dialogButton");
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

        ScrollPane sp = new ScrollPane(levelList);
        sp.setScrollingDisabled(true, false);
        sp.setOverscroll(false, false);

        setMovable(false);
        setResizable(false);
        setKeepWithinStage(true);
        setModal(true);

        float lineHeight = levelList.getStyle().font.getLineHeight();
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

        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    tbCancel.toggle();
                    return true;
                }
                return false;
            }
        });
    }

    public void endDialog(String name) {
        hide();
        result(name);
    }

    public Dialog show(Stage stage, String defText) {
        levelList.clearItems();
        levels.clear();

        levelListParser.getLevels(levels, LevelListParser.Source.EXTERNAL);
        levels.sort(LevelListParser.compLevel);

        levelList.setItems(levels);
        textField.setText(defText);
        super.show(stage);
        return this;
    }
}
