package com.breakthecore.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.breakthecore.CoreSmash;

public class DesktopLauncher {
	public static void main (String[] arg) {
//		int height = 1920;
//		int width = 1080;
        int height = 1024;
        int width = 768;

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		config.resizable = false;
		config.y = 0;
		if (CoreSmash.DEBUG_TABLET) {
			config.height = width;
			config.width = height;
		} else {
			config.height = height;
			config.width = width;
		}
        config.overrideDensity = 220;
		config.foregroundFPS = 60;
		config.backgroundFPS = 15;
		config.vSyncEnabled = false;
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		new LwjglApplication(new CoreSmash(), config);
	}
}
