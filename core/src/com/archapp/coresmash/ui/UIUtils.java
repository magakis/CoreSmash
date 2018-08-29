package com.archapp.coresmash.ui;

import com.archapp.coresmash.sound.SoundManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This static class is responsible for having a resolution/density independent UI layout
 */
public class UIUtils {
    private static Actor unitActor = new Actor();
    private static Actor screenActor = new Actor();
    private static List<Layout> observers = new ArrayList<>();
    private static TextField.TextFieldFilter numbersOnly = new TextField.TextFieldFilter() {
        @Override
        public boolean acceptChar(TextField textField, char c) {
            return Character.isDigit(c) || c == '-';
        }
    };

    private static ChangeListener buttonSoundListener;

    // Disable constructor
    private UIUtils() {
    }

    public static void initialize() {
        buttonSoundListener = new ChangeListener() {
            private SoundManager.SoundEffect btnSound = SoundManager.get().getSoundAsset("buttonClick");

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                btnSound.play();
            }
        };
    }

    public static void setUnitActor(BitmapFont smallestFont) {
        Objects.requireNonNull(smallestFont);
        unitActor.setSize(smallestFont.getLineHeight(), smallestFont.getLineHeight());
    }

    public static void updateScreenActor(float width, float height) {
        screenActor.setSize(width, height);
        for (Layout layout : observers) {
            layout.invalidateHierarchy();
        }
    }

    public static Actor getScreenActor(Layout observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
        return screenActor;
    }

    public static ChangeListener getButtonSoundListener() {

        return buttonSoundListener;
    }

    public static float getHeightFor(Drawable drawable, float size) {
        float ratio = drawable.getMinHeight() / drawable.getMinWidth();
        return ratio * size;
    }

    public static float getWidthFor(Drawable drawable, float size) {
        float ratio = drawable.getMinWidth() / drawable.getMinHeight();
        return ratio * size;
    }

    public static Actor getUnitActor() {
        return unitActor;
    }

    public static TextField.TextFieldFilter getNumbersOnlyFilter() {
        return numbersOnly;
    }
}
