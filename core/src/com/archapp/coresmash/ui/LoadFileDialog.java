package com.archapp.coresmash.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class LoadFileDialog extends Dialog {
    private final List<com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel> levelList;
    private final Array<com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel> levels;
    private final com.archapp.coresmash.levelbuilder.LevelListParser levelListParser;
    private final Dialog dlgConfirmDelete;

    public LoadFileDialog(final Skin skin) {
        super("", skin);

        levelListParser = new com.archapp.coresmash.levelbuilder.LevelListParser();

        levels = new Array<>();
        levelList = new List<>(skin);

        TextButton tbLoad = UIFactory.createTextButton("Load", skin, "dialogButton");
        tbLoad.getLabelCell()
                .pad(Value.percentHeight(1, tbLoad.getLabel()))
                .padTop(Value.percentHeight(.5f, tbLoad.getLabel()))
                .padBottom(Value.percentHeight(.5f, tbLoad.getLabel()));
        tbLoad.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                String chosen = levelList.getSelected().name;
                result(chosen);
            }
        });

        TextButton tbDelete = UIFactory.createTextButton("Delete", skin, "dialogButton");
        tbDelete.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String chosen = levelList.getSelected().name;
                ((Label) dlgConfirmDelete.getContentTable().getCells().get(0).getActor())
                        .setText("Delete level '[GREEN]" + chosen + "[]'?");
                dlgConfirmDelete.show(getStage());
            }
        });
        tbDelete.getLabelCell()
                .pad(Value.percentHeight(.5f, tbDelete.getLabel()));

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

        dlgConfirmDelete = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                if ((boolean) object) {
                    com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel chosen = levelList.getSelected();
                    FileHandle file = Gdx.files.external("/CoreSmash/levels/" + chosen.name + ".xml");
                    file.delete();
                    Array<com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel> tmp = levelList.getItems();
                    tmp.removeValue(chosen, false);
                    levelList.setItems(tmp);
                    if (chosen.num != 0) {
                        levelListParser.serializeLevelList(tmp);
                    }
                    com.archapp.coresmash.ui.Components.showToast("Deleted '" + chosen.name + "'", this.getStage());
                }
            }
        };

        dlgConfirmDelete.text(new Label("", skin, "h3f"));
        dlgConfirmDelete.button(UIFactory.createTextButton("Yes", skin, "dialogButton"), true);
        dlgConfirmDelete.button(UIFactory.createTextButton("No", skin, "dialogButton"), false);
        Cell<Label> txtCell = dlgConfirmDelete.getContentTable().getCells().get(0);
        dlgConfirmDelete.getCell(dlgConfirmDelete.getContentTable()).padBottom(txtCell.getActor().getStyle().font.getLineHeight());

        txtCell = ((TextButton) dlgConfirmDelete.getButtonTable().getCells().get(0).getActor()).getLabelCell();
        txtCell.pad(Value.percentHeight(1f, txtCell.getActor()));
        txtCell.padTop(Value.percentHeight(0.5f, txtCell.getActor()));
        txtCell.padBottom(Value.percentHeight(0.5f, txtCell.getActor()));

        txtCell = ((TextButton) dlgConfirmDelete.getButtonTable().getCells().get(1).getActor()).getLabelCell();
        txtCell.pad(Value.percentHeight(1f, txtCell.getActor()));
        txtCell.padTop(Value.percentHeight(0.5f, txtCell.getActor()));
        txtCell.padBottom(Value.percentHeight(0.5f, txtCell.getActor()));
        dlgConfirmDelete.getButtonTable().getCells().get(0).expandX().center();
        dlgConfirmDelete.getButtonTable().getCells().get(1).expandX().center();
        dlgConfirmDelete.pad(Value.percentHeight(.8f, txtCell.getActor()));

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
        buttons.pad(Value.percentHeight(.25f, tbLoad));
        buttons.add(tbLoad).padRight(Value.percentHeight(.5f, tbLoad)).expandX().left();
        buttons.add(tbDelete).padRight(Value.percentHeight(.5f, tbLoad)).expandX().center();
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

    @Override
    public Dialog show(Stage stage) {
        levelList.clearItems();
        levels.clear();

        levelListParser.getAllLevels(levels);
        levels.sort(com.archapp.coresmash.levelbuilder.LevelListParser.compLevel);

        levelList.setItems(levels);
        super.show(stage);
        return this;
    }
}

