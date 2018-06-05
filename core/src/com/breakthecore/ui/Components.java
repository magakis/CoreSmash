package com.breakthecore.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.breakthecore.WorldSettings;

public final class Components {
    private static boolean isInitialized;
    private static Dialog dlgToast;

    private Components() {
    }

    public static void initialize(Skin skin) {
        if (!isInitialized) {
            Window.WindowStyle ws = new Window.WindowStyle();
            ws.background = skin.getDrawable("toast1");
            ws.titleFont = skin.getFont("h5");

            dlgToast = new Dialog("", ws);
            dlgToast.text(new Label("", skin, "h5"));
            dlgToast.setTouchable(Touchable.disabled);
            isInitialized = true;
        }
    }

    public static void showToast(String text, Stage stage) {
        showToast(text, 2.5f, stage);
    }

    public static void showToast(String text, float duration, Stage stage) {
        ((Label) dlgToast.getContentTable().getCells().get(0).getActor()).setText(text);
        dlgToast.clearActions();
        dlgToast.show(stage, Actions.sequence(
                Actions.alpha(0),
                Actions.fadeIn(.4f),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        dlgToast.hide(Actions.fadeOut(.4f));
                    }
                })));
        dlgToast.setPosition(WorldSettings.getWorldWidth() / 2 - dlgToast.getWidth() / 2, WorldSettings.getWorldHeight() * .90f);
    }

}
