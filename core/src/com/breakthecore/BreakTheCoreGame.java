package com.breakthecore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.MainMenuScreen;

import static com.badlogic.gdx.Gdx.gl;

public class BreakTheCoreGame extends Game {
	private MainMenuScreen mainMenuScreen;
	private GameScreen gameScreen;
	private float dtForFrame;

	@Override
	public void create () {
		mainMenuScreen = new MainMenuScreen(this);
		gameScreen = new GameScreen(this);
		setScreen(gameScreen);
	}

	@Override
	public void render () {
		dtForFrame = Gdx.graphics.getDeltaTime();

		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		screen.render(dtForFrame);
	}
	
	@Override
	public void dispose () {
	}
}
