package com.breakthecore.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import java.util.Objects;

/**
 * This static class is responsible for having a resolution/density independent UI layout
 */
public class UIUtils {
    private static Actor unitActor = new Actor();
    private static Actor screenActor = new Actor();
    private static TextField.TextFieldFilter numbersOnly = new TextField.TextFieldFilter() {
        @Override
        public boolean acceptChar(TextField textField, char c) {
            return Character.isDigit(c) || c == '-';
        }
    };

    // Disable constructor
    private UIUtils() {
    }

    public static void setUnitActor(BitmapFont smallestFont) {
        Objects.requireNonNull(smallestFont);
        unitActor.setSize(smallestFont.getLineHeight(), smallestFont.getLineHeight());
    }

    public static void updateScreenActor(float width, float height) {
        screenActor.setSize(width, height);
    }

    public static Actor getScreenActor() {
        return screenActor;
    }

    public static Actor getUnitActor() {
        return unitActor;
    }

    public static TextField.TextFieldFilter getNumbersOnlyFilter() {
        return numbersOnly;
    }
}
