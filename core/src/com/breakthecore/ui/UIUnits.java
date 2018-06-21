package com.breakthecore.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.Objects;

/**
 * This static class is responsible for having a resolution/density independent UI layout
 */
public class UIUnits {
    private static Actor unitActor = new Actor();
    private static Actor screenActor = new Actor();

    // Disable constructor
    private UIUnits() {
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
}
