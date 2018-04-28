package com.breakthecore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.screens.LoadingScreen;
import com.breakthecore.screens.MainMenuScreen;
import com.breakthecore.screens.ScreenBase;

import java.util.Stack;

import static com.badlogic.gdx.Gdx.gl;

public class BreakTheCoreGame extends Game {
	private boolean isInitialized;
	private MainMenuScreen mainMenuScreen;
	private ExtendViewport m_viewport;
	private RenderManager m_renderManager;
	private AssetManager m_assetManager;
	private UserAccount userAccount;
	private Skin m_skin;
	private InputMultiplexer m_inputMultiplexer;

	private Stack<ScreenBase> m_screenStack;

	@Override
	public void create () {
		WorldSettings.init();
		m_screenStack = new Stack<ScreenBase>();
		m_viewport = new ExtendViewport(1080, 1920);

		m_inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(m_inputMultiplexer);
		Gdx.input.setCatchBackKey(true);

		m_assetManager = new AssetManager();
		m_skin = new Skin();
		userAccount = new UserAccount();

		setScreen(new LoadingScreen(this));
	}

	@Override
	public void render () {
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}

	public Skin getSkin() {
		return m_skin;
	}

	public void setInputProcessor(InputProcessor ip) {
		m_inputMultiplexer.clear();
		m_inputMultiplexer.addProcessor(ip);

	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setPrevScreen() {
		ScreenBase prev = m_screenStack.pop();
		setInputProcessor(prev.getScreenInputProcessor());
//        prev.show();
		super.setScreen(prev);
	}

	public void setScreen(ScreenBase newScreen) {
		m_screenStack.push((ScreenBase) screen);
		setInputProcessor(newScreen.getScreenInputProcessor());
		super.setScreen(newScreen);
	}

	public Viewport getWorldViewport() {
		return m_viewport;
	}

	public RenderManager getRenderManager() {
		return m_renderManager;
	}

	public AssetManager getAssetManager() {
		return m_assetManager;
	}

	public void initApp() {
		if (!isInitialized) {
			m_renderManager = new RenderManager(m_assetManager);
			mainMenuScreen = new MainMenuScreen(this);
			setInputProcessor(mainMenuScreen.getScreenInputProcessor());
			super.setScreen(mainMenuScreen);
			isInitialized = true;
		}
	}

	@Override
	public void dispose () {
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		m_viewport.update(width, height, true);
	}
}
