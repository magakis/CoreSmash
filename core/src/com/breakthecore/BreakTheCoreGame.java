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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.breakthecore.screens.GameScreen;
import com.breakthecore.screens.MainMenuScreen;

import static com.badlogic.gdx.Gdx.gl;

public class BreakTheCoreGame extends Game {
	private MainMenuScreen mainMenuScreen;
	private float dtForFrame;
	private Skin m_skin;
	private InputMultiplexer m_inputMultiplexer;

	@Override
	public void create () {
		WorldSettings.init();
		m_skin = createSkin();
		m_inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(m_inputMultiplexer);

		mainMenuScreen = new MainMenuScreen(this);
		setMainMenuScreen();
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

	public void setMainMenuScreen() {
		setInputProcessor(mainMenuScreen.getScreenInputProcessor());
		setScreen(mainMenuScreen);
	}

	@Override
	public void dispose () {
	}

	private Skin createSkin() {
		Skin skin = new Skin();
		Label.LabelStyle ls;
		Pixmap pix;
		Texture tex;
		NinePatch ninePatch;
		BitmapFont bf;

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

		registerFont(skin, "comic1_48", "comic1_48.fnt");

		registerFont(skin, "comic1_24b", "comic1_24b.fnt");
		registerFont(skin, "comic1_48b", "comic1_48b.fnt");
		registerFont(skin, "comic1_96b", "comic1_96b.fnt");

		TextButton.TextButtonStyle stb = new TextButton.TextButtonStyle();
		stb.up = skin.newDrawable("box_white_10", Color.WHITE);
		stb.down = skin.newDrawable("box_white_10", Color.GRAY);
		stb.checked = stb.up;
		stb.font = skin.getFont("comic1_96b");
		skin.add("menuButton", stb);

		stb = new TextButton.TextButtonStyle();
		stb.up = skin.newDrawable("box_white_5", Color.WHITE);
		stb.down = skin.newDrawable("box_white_5", Color.GRAY);
		stb.checked = stb.up;
		stb.font = skin.getFont("comic1_48b");
		skin.add("default", stb);

		return skin;
	}

	private void registerFont(Skin skin, String fntName, String path) {
		BitmapFont bf = new BitmapFont(Gdx.files.internal(path));
		skin.add(fntName, bf);
		Label.LabelStyle ls = new Label.LabelStyle(bf, Color.WHITE);
		skin.add(fntName, ls);
	}

}
