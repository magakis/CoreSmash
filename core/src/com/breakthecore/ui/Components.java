package com.breakthecore.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public final class Components {
    private static boolean isInitialized;
    private static Toast toast;

    // Disable constructor
    private Components() {
    }

    public static void initialize(Skin skin) {
        if (!isInitialized) {
            Toast.ToastStyle ws = new Toast.ToastStyle();
            ws.background = skin.getDrawable("toast1");
            ws.font = skin.getFont("h5");

            toast = new Toast(ws);
            isInitialized = true;
        }
    }

    public static void showToast(String text, Stage stage) {
        showToast(text, 2.5f, stage);
    }

    public static void showToast(String text, float duration, Stage stage) {
        toast.setText(text);
        toast.show(stage, Actions.sequence(
                Actions.alpha(0),
                Actions.fadeIn(.4f),
                Actions.delay(duration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        toast.hide(Actions.fadeOut(.4f));
                    }
                })));
    }
}
