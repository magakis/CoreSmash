package com.archapp.coresmash;

import com.badlogic.gdx.Gdx;

public final class WorldSettings {
    private static int tileSize = 80;
    private static int screenWidth = Gdx.graphics.getWidth();
    private static int screenHeight = Gdx.graphics.getHeight();
    private static int smallestScreenDimension = screenHeight > screenWidth ? screenWidth : screenHeight;

    private WorldSettings() {}

    public static int getSmallestScreenDimension() {
        return smallestScreenDimension;
    }

    public static float getDefaultDialogSize() {
        return smallestScreenDimension * DefaultRatio.dialogToScreen;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static int getTileSize() {
        return tileSize;
    }

    public static int getWorldWidth() {
        return 1080;
    }

    public static int getWorldHeight() {
        return 1920;
    }

    public static class DefaultRatio {
        private static float dialogToScreen = .8f;
        private static float dialogButtonHeightToContent = .14f;

        private DefaultRatio() {
        }

        public static float dialogToScreen() {
            return dialogToScreen;
        }

        public static float dialogButtonToContent() {
            return dialogButtonHeightToContent;
        }
    }
}
