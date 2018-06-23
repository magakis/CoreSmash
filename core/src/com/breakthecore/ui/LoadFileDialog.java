package com.breakthecore.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Objects;

public class LoadFileDialog extends Dialog {
    private final List<String> levelsFound;
    private final FilenameFilter levelBuilderFilter;
    private final Dialog dlgConfirmDelete;

    public LoadFileDialog(final Skin skin, final WindowStyle windowStyle, Stage stage1) {
        super("", windowStyle);

        levelBuilderFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                // TODO(25/5/2018): Enable this at some point? !s.startsWith("_editor_") &&
                return s.toLowerCase().endsWith(".xml");
            }
        };

        List.ListStyle ls = new List.ListStyle();
        ls.fontColorSelected = Color.GREEN;
        ls.fontColorUnselected = Color.WHITE;
        ls.selection = skin.newDrawable("box_white_5", Color.BLACK);
        ls.font = skin.getFont("h5");

        levelsFound = new List<>(ls);

        TextButton tbLoad = new TextButton("Load", skin, "dialogButton");
        tbLoad.getLabelCell()
                .pad(Value.percentHeight(1, tbLoad.getLabel()))
                .padTop(Value.percentHeight(.5f, tbLoad.getLabel()))
                .padBottom(Value.percentHeight(.5f, tbLoad.getLabel()));
        tbLoad.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                String chosen = levelsFound.getSelected();
                result(chosen.substring(0, chosen.length() - 4));
            }
        });

        TextButton tbDelete = new TextButton("Delete", skin, "dialogButton");
        tbDelete.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String chosen = levelsFound.getSelected();
                ((Label) dlgConfirmDelete.getContentTable().getCells().get(0).getActor())
                        .setText("Delete level '[GREEN]" + chosen + "[]'?");
                dlgConfirmDelete.show(getStage());
            }
        });
        tbDelete.getLabelCell()
                .pad(Value.percentHeight(1, tbDelete.getLabel()))
                .padTop(Value.percentHeight(.5f, tbDelete.getLabel()))
                .padBottom(Value.percentHeight(.5f, tbDelete.getLabel()));

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

        dlgConfirmDelete = new Dialog("", windowStyle) {
            @Override
            protected void result(Object object) {
                if ((boolean) object) {
                    String chosen = levelsFound.getSelected();
                    FileHandle file = Gdx.files.external("/CoreSmash/levels/" + chosen);
                    file.delete();
                    Array tmp = levelsFound.getItems();
                    tmp.removeValue(chosen, false);
                    levelsFound.setItems(tmp);
                    Components.showToast("Deleted '" + chosen + "'", this.getStage());
                }
            }
        };
        dlgConfirmDelete.text(new Label("", skin, "h3f"));
        dlgConfirmDelete.button(new TextButton("Yes", skin, "dialogButton"), true);
        dlgConfirmDelete.button(new TextButton("No", skin, "dialogButton"), false);
        Cell<Label> txtCell = dlgConfirmDelete.getContentTable().getCells().get(0);
        txtCell.pad(Value.percentHeight(.5f, txtCell.getActor()));
        txtCell.maxWidth(Value.percentWidth(0.5f, UIUnits.getScreenActor()));
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
                .maxWidth(Value.percentWidth(.75f, UIUnits.getScreenActor()))
                .maxHeight(Value.percentHeight(.5f, UIUnits.getScreenActor()));

        Table buttons = getButtonTable();
        buttons.pad(Value.percentHeight(.25f, tbLoad));
        buttons.add(tbLoad).padRight(Value.percentHeight(.5f, tbLoad)).expandX().left();
        buttons.add(tbDelete).padRight(Value.percentHeight(.5f, tbLoad)).expandX().center();
        buttons.add(tbCancel).expandX().right();
    }

    public Dialog show(Stage stage) {
        levelsFound.clearItems();
        String[] files = Gdx.files.external("/CoreSmash/levels/").file().list(levelBuilderFilter);
        Arrays.sort(Objects.requireNonNull(files));
        levelsFound.setItems(files);
        super.show(stage);
        return this;
    }
}

