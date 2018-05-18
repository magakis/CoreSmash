package com.breakthecore.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.breakthecore.CoreSmash;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = true;
		config.height = 850;
		config.width = config.height * 9 / 16;
		config.foregroundFPS = 60;
		config.backgroundFPS = 15;
		config.vSyncEnabled = false;
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		new LwjglApplication(new CoreSmash(), config);
	}
}
