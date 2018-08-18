package com.archapp.coresmash.ui;

import com.archapp.coresmash.levelbuilder.LevelListParser;
import com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;


public class AssignLevelDialog extends Dialog {
    private Array<RegisteredLevel> levels;
    private List<RegisteredLevel> levelList;
    private LevelListParser levelListParser;

    private AssignLevelTextInput assignLevelTextInput;
    private InsertLevelTextInput insertLevelTextInput;
    private Input.TextInputListener swapLevelTextInput;

    public AssignLevelDialog(Skin skin) {
        super("", skin);

        levels = new Array<>();
        levelList = new List<>(skin);
        levelListParser = new LevelListParser();

        assignLevelTextInput = new AssignLevelTextInput();
        insertLevelTextInput = new InsertLevelTextInput();
        swapLevelTextInput = new SwapLevelTextInput();

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
                .maxHeight(Value.percentHeight(.5f, UIUtils.getScreenActor(sp)));


        TextButton tbAssign = UIFactory.createTextButton("Assign", skin, "dialogButton");
        final float buttonWidth = tbAssign.getPrefWidth() * 1.2f;

        tbAssign.getLabelCell().width(buttonWidth);
        tbAssign.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                RegisteredLevel chosen = levelList.getSelected();
                assignLevelTextInput.activeLevel = chosen;
                Gdx.input.getTextInput(assignLevelTextInput, "Assign '" + chosen.name + "' at:", String.valueOf(chosen.num), "");
            }
        });


        final TextButton tbCancel = UIFactory.createTextButton("Cancel", skin, "dialogButton");
        tbCancel.getLabelCell().width(buttonWidth);
        tbCancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        TextButton tbInsert = UIFactory.createTextButton("Insert", skin, "dialogButton");
        tbInsert.getLabelCell().width(buttonWidth);
        tbInsert.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                RegisteredLevel chosen = levelList.getSelected();
                insertLevelTextInput.activeLevel = chosen;
                Gdx.input.getTextInput(insertLevelTextInput, "Inserted '" + chosen.name + "' at:", String.valueOf(chosen.num), "");
            }
        });

        TextButton tbSwap = UIFactory.createTextButton("Swap", skin, "dialogButton");
        tbSwap.getLabelCell().width(buttonWidth);
        tbSwap.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                RegisteredLevel chosen = levelList.getSelected();
                Gdx.input.getTextInput(swapLevelTextInput, "Swap '" + chosen.name + "' with:", String.valueOf(chosen.num), "Digits (0-9)");
            }
        });

//        Table buttonsTable = getButtonTable();

//        buttons.defaults()
//                .width(tbAssign.getPrefWidth()*1.2f)
//                .padRight(tbAssign.getPrefWidth()*.1f)
//                .expandX().center();
//        buttons.pad(5 * Gdx.graphics.getDensity());

        HorizontalGroup buttons = new HorizontalGroup();
        buttons.wrap(true);
        buttons.space(buttonWidth * .1f);
        buttons.align(Align.center);

        buttons.addActor(tbAssign);
        buttons.addActor(tbInsert);
        buttons.addActor(tbSwap);
        buttons.addActor(tbCancel);

        getButtonTable().padTop(buttonWidth * .1f).padBottom(buttonWidth * .1f).add(buttons).minWidth(buttonWidth * 5);

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

    @Override
    public Dialog show(Stage stage) {
        updateLevelList();
        super.show(stage);
        return this;
    }

    private void updateLevelList() {
        levelList.clearItems();
        levels.clear();

        levelListParser.getAllLevels(levels);
        levels.sort(LevelListParser.compLevel);

        levelList.setItems(levels);
    }

    private class AssignLevelTextInput implements Input.TextInputListener {
        private RegisteredLevel activeLevel;
        private RegisteredLevel dummySearchLevel = new RegisteredLevel(0, "");

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

    private class InsertLevelTextInput implements Input.TextInputListener {
        private RegisteredLevel activeLevel;
        private RegisteredLevel dummySearchLevel = new RegisteredLevel(0, "");

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

        @Override
        public void canceled() {

        }
    }

    private class SwapLevelTextInput implements Input.TextInputListener {
        private RegisteredLevel dummySearchLevel = new RegisteredLevel(0, "");

        @Override
        public void input(String text) {
            if (!text.isEmpty() && text.matches("[0-9]+")) {
                RegisteredLevel activeLevel = levelList.getSelected();
                int assignedValue = Integer.parseInt(text);
                if (assignedValue != activeLevel.num) {
                    dummySearchLevel.num = assignedValue;
                    int index = Arrays.binarySearch(levelList.getItems().toArray(), dummySearchLevel, LevelListParser.compLevel);

                    if (index >= 0) {
                        RegisteredLevel secondLevel = levels.get(index);
                        int tmp = secondLevel.num;
                        secondLevel.num = activeLevel.num;
                        activeLevel.num = tmp;

                        levelList.getItems().sort(LevelListParser.compLevel);
                        levelList.setItems(levelList.getItems());
                        levelListParser.serializeLevelList(levelList.getItems());
                        Components.showToast("'" + activeLevel.name + "' was swaped with '" + secondLevel.name + "'", getStage());
                        return;
                    }
                }
            }
            Components.showToast("Could not perform Swap!", getStage());
        }

        @Override
        public void canceled() {

        }
    }
}
