package com.breakthecore;

import com.badlogic.gdx.Gdx;

import java.util.Random;

public final class WorldSettings {
    private static int s_width;
    private static int s_height;
    private static float s_aspect;
    private static int s_tileSize;

    private WorldSettings() {}

    public static void init() {
        s_aspect = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        s_width = (int) (1920 * s_aspect);
        s_height = 1920;

        s_tileSize = 75;
    }

    public static int getTileSize() {return s_tileSize;}

    public static int getWorldWidth() {
        return 1080;
    }

    public static int getWorldHeight() {
        return 1920;
    }

}
