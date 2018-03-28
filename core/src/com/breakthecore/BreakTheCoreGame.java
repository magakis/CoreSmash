package com.breakthecore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.MainMenuScreen;

import static com.badlogic.gdx.Gdx.gl;

public class BreakTheCoreGame extends Game {
	private MainMenuScreen mainMenuScreen;
	private GameScreen gameScreen;
	private float dtForFrame;
	private InputMultiplexer m_inputMultiplexer;

	@Override
	public void create () {
		WorldSettings.init();
		m_inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(m_inputMultiplexer);
		mainMenuScreen = new MainMenuScreen(this);
		setScreen(mainMenuScreen);
	}

	@Override
	public void render () {
		dtForFrame = Gdx.graphics.getDeltaTime();

		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		screen.render(dtForFrame);
	}

	public void addInputHandler(InputProcessor ip) {
		m_inputMultiplexer.addProcessor(ip);
	}

	public void addInputHandler(int index, InputProcessor ip) {
		m_inputMultiplexer.addProcessor(index, ip);
	}

	public void removeInputHandler(InputProcessor ip) {
		m_inputMultiplexer.removeProcessor(ip);
	}

	@Override
	public void dispose () {
	}
}
