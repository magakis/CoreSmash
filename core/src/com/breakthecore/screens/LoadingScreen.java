package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.breakthecore.BreakTheCoreGame;

public class LoadingScreen extends ScreenBase {
    AssetManager am;
    Stage m_stage;
    Skin skin;

    Label percent;

    public LoadingScreen(BreakTheCoreGame game) {
        super(game);
        m_stage = new Stage(m_game.getWorldViewport());
        am = game.getAssetManager();
        skin = game.getSkin();
        percent = new Label("null", new Label.LabelStyle(new BitmapFont(Gdx.files.internal("comic_96b.fnt"), false), Color.WHITE));
        percent.setAlignment(Align.center);
        percent.setFillParent(true);
        m_stage.addActor(percent);

        screenInputMultiplexer.addProcessor(m_stage);
        loadAllTextures();
        loadAllBitmapFonts();
    }

    @Override
    public void render(float delta) {
        if (am.update()) {
            setupSkin();
            m_game.initApp();
        }
        percent.setText(String.format("%.0f %%", am.getProgress() * 100));

        m_stage.act();
        m_stage.draw();
    }

    private void loadAllTextures() {
        loadTexture("asteroid.png");
        loadTexture("ball.png");
    }

    private void loadAllBitmapFonts() {
        loadBitmapFont("comic_32.fnt");
        loadBitmapFont("comic_48.fnt");

        loadBitmapFont("comic_24b.fnt");
        loadBitmapFont("comic_32b.fnt");
        loadBitmapFont("comic_48b.fnt");
        loadBitmapFont("comic_96b.fnt");

        loadBitmapFont("comic_72bo.fnt");
        loadBitmapFont("comic_96bo.fnt");
    }

    private void loadTexture(String name) {
        am.load(name, Texture.class);
    }

    private void loadBitmapFont(String name) {
        am.load(name, BitmapFont.class);
    }

    private void setupSkin() {
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

        skin.add("ball", am.get("ball.png"));
        skin.add("whiteFading", am.get("asteroid.png"));

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
    }

    private void registerFont(Skin skin, String fntName, String path) {
        BitmapFont bf = am.get(path);
        skin.add(fntName, bf);
        Label.LabelStyle ls = new Label.LabelStyle(bf, Color.WHITE);
        skin.add(fntName, ls);
    }

}