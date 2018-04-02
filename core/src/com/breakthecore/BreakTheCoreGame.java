package com.breakthecore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.breakthecore.screens.CampaignScreen;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.GameSettingsScreen;
import com.breakthecore.screens.MainMenuScreen;
import com.breakthecore.screens.ScreenBase;

import static com.badlogic.gdx.Gdx.gl;

public class BreakTheCoreGame extends Game {
	private MainMenuScreen mainMenuScreen;
	private ExtendViewport m_viewport;
	private float dtForFrame;
	private Skin m_skin;
	private InputMultiplexer m_inputMultiplexer;
	private boolean isMainMenuActive;

	@Override
	public void create () {
		Gdx.input.setCatchBackKey(true);
		WorldSettings.init();
		m_viewport = new ExtendViewport(1080, 1920);
		m_skin = createSkin();
		m_inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(m_inputMultiplexer);

		mainMenuScreen = new MainMenuScreen(this);
		setMainMenuScreen();
//		setScreen(new CampaignScreen(this));
	}

	@Override
	public void render () {
		dtForFrame = Gdx.graphics.getDeltaTime();

		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		screen.render(dtForFrame);
	}

	public Skin getSkin() {
		return m_skin;
	}

	public void setInputProcessor(InputProcessor ip) {
		m_inputMultiplexer.clear();
		m_inputMultiplexer.addProcessor(ip);

	}

	// WARNING: All the screen should store a BreakTheCoreGame reference and not a plain Game
	// reference in order for the right function to get called
	public void setScreen(ScreenBase screen) {
		isMainMenuActive = false;
		setInputProcessor(screen.getScreenInputProcessor());
		super.setScreen(screen);
	}

	public boolean isMainMenuActive() {
		return isMainMenuActive;
	}

	public Viewport getWorldViewport() {
		return m_viewport;
	}

	public void setMainMenuScreen() {
		setInputProcessor(mainMenuScreen.getScreenInputProcessor());
		setScreen(mainMenuScreen);
		isMainMenuActive = true;
	}

	@Override
	public void dispose () {
	}

	private Skin createSkin() {
		Skin skin = new Skin();
		Pixmap pix;
		Texture tex;
		NinePatch ninePatch;

		// Nine-Patches
		int pixHeight = 30;
		pix = new Pixmap(pixHeight, pixHeight, Pixmap.Format.RGB888);
		pix.setColor(Color.WHITE);
		pix.fill();
		pix.setColor(Color.BLACK);
		pix.fillRectangle(5, 5, pix.getWidth() - 10, pixHeight - 10);
		tex = new Texture(pix);
		ninePatch = new NinePatch(tex, 10, 10, 10, 10);
		skin.add("box_white_5", ninePatch);

		pix = new Pixmap(pixHeight, pixHeight, Pixmap.Format.RGB888);
		pix.setColor(Color.WHITE);
		pix.fill();
		pix.setColor(Color.BLACK);
		pix.fillRectangle(10, 10, pix.getWidth() - 20, pixHeight - 20);
		tex = new Texture(pix);
		ninePatch = new NinePatch(tex, 10, 10, 10, 10);
		skin.add("box_white_10", ninePatch);

		// Textures
		pix = new Pixmap(41, 41, Pixmap.Format.RGBA8888);
		pix.setColor(Color.alpha(0));
		pix.fill();
		pix.setColor(Color.BLACK);
		pix.fillCircle(20, 20, 20);
		pix.setColor(Color.WHITE);
		pix.fillCircle(20, 20, 17);
		tex = new Texture(pix);
		skin.add("myBall", tex);

		skin.add("ball", new Texture("ball.png"));
		skin.add("whiteFading", new Texture("asteroid.png"));

		// Fonts
		registerFont(skin, "comic_32", "comic_32.fnt");
		registerFont(skin, "comic_48", "comic_48.fnt");

		registerFont(skin, "comic_24b", "comic_24b.fnt");
		registerFont(skin, "comic_32b", "comic_32b.fnt");
		registerFont(skin, "comic_48b", "comic_48b.fnt");
		registerFont(skin, "comic_96b", "comic_96b.fnt");

		registerFont(skin, "comic_72bo", "comic_72bo.fnt");
		registerFont(skin, "comic_96bo", "comic_96bo.fnt");

		// TextButtonStyles
		TextButton.TextButtonStyle stb = new TextButton.TextButtonStyle();
		stb.up = skin.newDrawable("box_white_10", Color.WHITE);
		stb.down = skin.newDrawable("box_white_10", Color.GRAY);
		stb.checked = stb.up;
		stb.font = skin.getFont("comic_96b");
		skin.add("menuButton", stb);

		stb = new TextButton.TextButtonStyle();
		stb.up = skin.newDrawable("box_white_10", Color.WHITE);
		stb.down = skin.newDrawable("box_white_10", Color.GRAY);
		stb.checked = stb.up;
		stb.font = skin.getFont("comic_48");
		skin.add("modeButton", stb);

		stb = new TextButton.TextButtonStyle();
		stb.up = skin.newDrawable("box_white_5", Color.WHITE);
		stb.down = skin.newDrawable("box_white_5", Color.GRAY);
		stb.checked = stb.up;
		stb.font = skin.getFont("comic_48b");
		skin.add("default", stb);

		stb = new TextButton.TextButtonStyle();
		stb.up = skin.newDrawable("whiteFading", Color.WHITE);
		stb.down = skin.newDrawable("whiteFading", Color.GRAY);
		stb.checked = stb.up;
		stb.font = skin.getFont("comic_72bo");
		skin.add("levelBtnEnabled", stb);

		stb = new TextButton.TextButtonStyle();
		stb.up = skin.newDrawable("whiteFading", Color.DARK_GRAY);
		stb.down = skin.newDrawable("whiteFading", Color.DARK_GRAY);
		stb.checked = stb.up;
		stb.font = skin.getFont("comic_72bo");
		skin.add("levelBtnDisabled", stb);


		//SliderStyles
		Slider.SliderStyle ss = new Slider.SliderStyle();
		ss.background = skin.getDrawable("box_white_5");
		ss.background.setMinHeight(60);
		ss.knob = skin.getDrawable("myBall");
		ss.knob.setMinHeight(80);
		ss.knob.setMinWidth(80);
		skin.add("default-horizontal", ss);

		return skin;
	}

	private void registerFont(Skin skin, String fntName, String path) {
		BitmapFont bf = new BitmapFont(Gdx.files.internal(path));
		skin.add(fntName, bf);
		Label.LabelStyle ls = new Label.LabelStyle(bf, Color.WHITE);
		skin.add(fntName, ls);
	}

	@Override
	public void resize(int width, int height) {
		m_viewport.update(width, height, true);
	}
}
