package com.breakthecore;

import com.badlogic.gdx.Gdx;

import java.util.Random;

public final class WorldSettings {
    private static int s_width;
    private static int s_height;
    private static float s_aspect;
    private static Random s_rng;

    public static void init() {
        s_aspect = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        s_width = (int) (1920 * s_aspect);
        s_height = 1920;

        s_rng = new Random();
    }

    public static int getWorldWidth() {
        return s_width;
    }

    public static int getWorldHeight() {
        return s_height;
    }

    public static int getRandomInt(int i) {
        return s_rng.nextInt(i);
    }
}
