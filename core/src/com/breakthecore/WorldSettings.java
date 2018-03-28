package com.breakthecore;

import com.badlogic.gdx.Gdx;

public final class WorldSettings {
    private static int s_width;
    private static int s_height;
    private static float s_aspect;

    public static void init() {
        s_aspect = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        s_width = (int) (1920 * s_aspect);
        s_height = 1920;
    }

    public static int getWorldWidth() {
        return s_width;
    }

    public static int getWorldHeight() {
        return s_height;
    }
}
