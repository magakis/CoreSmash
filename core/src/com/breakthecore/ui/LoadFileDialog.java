package com.breakthecore.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.breakthecore.WorldSettings;

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

        TextButton tbLoad = new TextButton("Load", skin);
        tbLoad.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                String chosen = (String) levelsFound.getSelected();
                result(chosen.substring(0, chosen.length() - 4));
            }
        });

        TextButton tbDelete = new TextButton("Delete", skin);
        tbDelete.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String chosen = (String) levelsFound.getSelected();
                ((Label) dlgConfirmDelete.getContentTable().getCells().get(0).getActor()).setText("Delete level '" + chosen + "'?");
                dlgConfirmDelete.show(getStage());
            }
        });

        TextButton tbCancel = new TextButton("Cancel", skin);
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
                    String chosen = (String) levelsFound.getSelected();
                    FileHandle file = Gdx.files.external("/CoreSmash/levels/" + chosen);
                    file.delete();
                    Array tmp = levelsFound.getItems();
                    tmp.removeValue(chosen,false);
                    levelsFound.setItems(tmp);
                    Components.showToast("Deleted '" + chosen + "'", this.getStage());
                }
            }
        };
        dlgConfirmDelete.pad(30);
        dlgConfirmDelete.getContentTable().defaults().padBottom(20);
        dlgConfirmDelete.getButtonTable().columnDefaults(0).padRight(40);
        dlgConfirmDelete.getButtonTable().defaults().height(100).width(150);
        dlgConfirmDelete.text(new Label("",skin, "h3"));
        dlgConfirmDelete.button(new TextButton("Yes",skin), true);
        dlgConfirmDelete.button(new TextButton("No",skin), false);

        ScrollPane sp = new ScrollPane(levelsFound);
        sp.setScrollingDisabled(true, false);
        sp.setOverscroll(false, false);

        setMovable(false);
        setResizable(false);
        setKeepWithinStage(true);
        setModal(true);

        padTop(10);

        Table content = getContentTable();
        content.pad(20).padBottom(0).add(sp).colspan(2).fill().width(WorldSettings.getWorldWidth()/2).height(400).left().row();

        Table buttons = getButtonTable();
        buttons.pad(10);
        buttons.add(tbLoad).width(170).height(100).padRight(20);
        buttons.add(tbDelete).width(170).height(100).padRight(20);
        buttons.add(tbCancel).width(170).height(100);

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

