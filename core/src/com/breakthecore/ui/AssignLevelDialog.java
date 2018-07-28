package com.breakthecore.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.breakthecore.levelbuilder.LevelListParser;
import com.breakthecore.levelbuilder.LevelListParser.RegisteredLevel;

import java.util.Arrays;


public class AssignLevelDialog extends Dialog {
    private Array<RegisteredLevel> levels;
    private List<RegisteredLevel> levelList;
    private LevelListParser levelListParser;

    private AssignLevelTextInput assignLevelTextInput;
    private InsertLevelTextInput insertLevelTextInput;

    public AssignLevelDialog(Skin skin) {
        super("", skin);

        levels = new Array<>();
        levelList = new List<>(skin);
        levelListParser = new LevelListParser();

        assignLevelTextInput = new AssignLevelTextInput();
        insertLevelTextInput = new InsertLevelTextInput();

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

        TextButton tbAssign = UIFactory.createTextButton("Assign", skin, "dialogButton");
        tbAssign.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                RegisteredLevel chosen = levelList.getSelected();
                assignLevelTextInput.activeLevel = chosen;
                Gdx.input.getTextInput(assignLevelTextInput, "Assign '" + chosen.name + "' at:", String.valueOf(chosen.num), "");
            }
        });
        tbAssign.getLabelCell().pad(Value.percentHeight(.5f, tbAssign.getLabel()));

        TextButton tbCancel = UIFactory.createTextButton("Cancel", skin, "dialogButton");
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

        TextButton tbInsert = UIFactory.createTextButton("Insert", skin, "dialogButton");
        tbInsert.getLabelCell()
                .pad(Value.percentHeight(1, tbInsert.getLabel()))
                .padTop(Value.percentHeight(.5f, tbInsert.getLabel()))
                .padBottom(Value.percentHeight(.5f, tbInsert.getLabel()));
        tbInsert.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                RegisteredLevel chosen = levelList.getSelected();
                insertLevelTextInput.activeLevel = chosen;
                Gdx.input.getTextInput(insertLevelTextInput, "Inserted '" + chosen.name + "' at:", String.valueOf(chosen.num), "");
            }
        });

        Table buttons = getButtonTable();
        buttons.pad(Value.percentHeight(.25f, tbAssign));
        buttons.add(tbAssign).padRight(Value.percentHeight(.5f, tbAssign)).expandX().center();
        buttons.add(tbInsert).padRight(Value.percentHeight(.5f, tbAssign)).expandX().center();
        buttons.add(tbCancel).expandX().right();
    }

    @Override
    public Dialog show(Stage stage) {
        levelList.clearItems();
        levels.clear();

        levelListParser.getLevels(levels);
        levels.sort(LevelListParser.compLevel);

        levelList.setItems(levels);
        super.show(stage);
        return this;
    }

    private class AssignLevelTextInput implements Input.TextInputListener {
        protected RegisteredLevel activeLevel;
        protected RegisteredLevel dummySearchLevel = new RegisteredLevel(0, "");

        @Override
        public void input(String text) {
            if (activeLevel != null && !text.isEmpty() && text.matches("[0-9]+")) {
                int assignedValue = Integer.parseInt(text);
                if (assignedValue != activeLevel.num) {
                    dummySearchLevel.num = assignedValue;
                    int index = Arrays.binarySearch(levelList.getItems().toArray(), dummySearchLevel, LevelListParser.compLevel);

                    if (index >= 0) {
                        levelList.getItems().get(index).num = 0;
                    }

                    activeLevel.num = assignedValue;
                    levelList.getItems().sort(LevelListParser.compLevel);
                    levelList.setItems(levelList.getItems());
                    levelListParser.serializeLevelList(levelList.getItems());
                    Components.showToast("'" + activeLevel.name + "' was assigned to Level: " + activeLevel.num, getStage());
                }
            }
            activeLevel = null;
        }

        @Override
        public void canceled() {
            activeLevel = null;
        }
    }

    private class InsertLevelTextInput extends AssignLevelTextInput {
        @Override
        public void input(String text) {
            if (activeLevel != null && !text.isEmpty() && text.matches("[0-9]+")) {
                int assignedValue = Integer.parseInt(text);
                if (assignedValue != activeLevel.num) {
                    dummySearchLevel.num = assignedValue;
                    int index = Arrays.binarySearch(levelList.getItems().toArray(), dummySearchLevel, LevelListParser.compLevel);

                    if (index >= 0) {
                        Array<RegisteredLevel> list = levelList.getItems();
                        do {
                            ++list.get(index).num;
                            ++index;
                        }
                        while (index < list.size && list.get(index).num == list.get(index - 1).num);
                    }

                    activeLevel.num = assignedValue;
                    levelList.getItems().sort(LevelListParser.compLevel);
                    levelList.setItems(levelList.getItems());
                    levelListParser.serializeLevelList(levelList.getItems());
                    Components.showToast("'" + activeLevel.name + "' was assigned to Level: " + activeLevel.num, getStage());
                }
            }
            activeLevel = null;
        }
    }
}
