package com.archapp.coresmash.desktop;

import com.archapp.coresmash.CoreSmash;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
//		int height = 1024;
//		int width = 768;
		int height = 1024;
        int width = 576;

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.resizable = false;
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
		config.forceExit = false;
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		new LwjglApplication(new CoreSmash(), config);
	}
}
