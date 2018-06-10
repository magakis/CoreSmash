package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.breakthecore.CoreSmash;
import com.breakthecore.themes.AbstractTheme;
import com.breakthecore.themes.BaseTheme;
import com.breakthecore.tiles.TileAttributes;
import com.breakthecore.tiles.TileIndex;
import com.breakthecore.tiles.TileType;
import com.breakthecore.ui.Components;

import java.util.Locale;

public class LoadingScreen extends ScreenBase {
    private AssetManager am;
    private Stage stage;
    private Skin skin;
    private Label percent;
    private AbstractTheme baseTheme;

    private TextureLoader.TextureParameter textureParam;

    public LoadingScreen(CoreSmash game) {
        super(game);
        stage = new Stage(gameInstance.getUIViewport());

        am = game.getAssetManager();
        textureParam = new TextureLoader.TextureParameter();
        textureParam.genMipMaps = true;
        textureParam.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        textureParam.magFilter = Texture.TextureFilter.Nearest;

        skin = game.getSkin();
        percent = new Label("null", new Label.LabelStyle(new BitmapFont(Gdx.files.internal("gidole_72.fnt"), false), Color.WHITE));
        percent.setAlignment(Align.center);
        percent.setFillParent(true);
        stage.addActor(percent);

        screenInputMultiplexer.addProcessor(stage);
        loadAllTextures();
        loadAllBitmapFonts();
        loadAllBalls();

        baseTheme = new BaseTheme();
        baseTheme.queueForLoad(am);
        gameInstance.getRenderManager().setTheme(baseTheme);
    }

    @Override
    public void render(float delta) {
        if (am.update()) {
            baseTheme.finishLoading();
            setupSkin();
            Components.initialize(skin);
            gameInstance.initApp();
        }
        percent.setText(String.format(Locale.ENGLISH, "%.0f %%", am.getProgress() * 100));

        stage.act();
        stage.draw();
    }

    private void loadAllTextures() {
        loadTexture("asteroid.png");
        loadTexture("balloon.png");
        loadTexture("ball.png");
        loadTexture("cog.png");
        loadTexture("group.png");
        loadTexture("speaker.png");
        loadTexture("userIcon.png");
        loadTexture("NinePatches/toast1.png");
        loadTexture("NinePatches/dialog1.png");
        loadTexture("NinePatches/GameScreenTop.png");
        loadTexture("NinePatches/softGray.png");
        loadTexture("NinePatches/progressbar_inner.png");
        loadTexture("map.png");
        loadTexture("default.png");
        loadTexture("UIGameScreenTopRound.png");
        loadTexture("MovesIcon.png");
        loadTexture("HourGlass.png");
        loadTexture("HeartIcon.png");
    }

    private void loadAllBitmapFonts() {
        loadBitmapFont("comic_24b.fnt");
        loadBitmapFont("comic_32b.fnt");
        loadBitmapFont("comic_48b.fnt");
        loadBitmapFont("comic_96b.fnt");

        loadBitmapFont("comic_72bo.fnt");
        loadBitmapFont("comic_96bo.fnt");

        loadBitmapFont("gidole_12.fnt");
        loadBitmapFont("gidole_14.fnt");
        loadBitmapFont("gidole_16.fnt");
        loadBitmapFont("gidole_18.fnt");
        loadBitmapFont("gidole_24.fnt");
        loadBitmapFont("gidole_32.fnt");

        loadBitmapFont("gidole_21.fnt");
        loadBitmapFont("gidole_28.fnt");
        loadBitmapFont("gidole_37.fnt");
        loadBitmapFont("gidole_50.fnt");
        loadBitmapFont("gidole_67.fnt");
        loadBitmapFont("gidole_83.fnt");
    }

    private void loadAllBalls() {
        TileAttributes ballAttr;

        TileIndex tileIndex = TileIndex.get();
        if (tileIndex.isFrozen()) return;

        ballAttr = TileAttributes.getBuilder()
                .setID(0)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(1)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(2)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(3)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(4)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(5)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(6)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(7)
                .setBreakable(true)
                .setMatchable(true)
                .setPlaceable(true)
                .setTileType(TileType.REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

//        ballAttr = TileAttributes.getBuilder()
//                .setID(8)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);
//
//        ballAttr = TileAttributes.getBuilder()
//                .setID(9)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);
//
//        ballAttr = TileAttributes.getBuilder()
//                .setID(10)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);
//
//        ballAttr = TileAttributes.getBuilder()
//                .setID(11)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);
//
//        ballAttr = TileAttributes.getBuilder()
//                .setID(12)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);
//
//        ballAttr = TileAttributes.getBuilder()
//                .setID(13)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);
//
//        ballAttr = TileAttributes.getBuilder()
//                .setID(14)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);
//
//        ballAttr = TileAttributes.getBuilder()
//                .setID(15)
//                .setBreakable(true)
//                .setMatchable(true)
//                .setPlaceable(true)
//                .setTileType(TileType.REGULAR)
//                .build();
//        TileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(17)
                .setBreakable(false)
                .setMatchable(false)
                .setPlaceable(true)
                .setTileType(TileType.RANDOM_REGULAR)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(18)
                .setBreakable(false)
                .setMatchable(false)
                .setPlaceable(true)
                .setTileType(TileType.WALL)
                .build();
        tileIndex.registerTile(ballAttr);

        ballAttr = TileAttributes.getBuilder()
                .setID(19)
                .setBreakable(false)
                .setMatchable(false)
                .setPlaceable(false)
                .setTileType(TileType.BOMB)
                .build();
        tileIndex.registerTile(ballAttr);

        tileIndex.freeze();
    }

    private void loadTexture(String name) {
        am.load(name, Texture.class, textureParam);
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
        pix.setColor(Color.rgba8888(30 / 255f, 30 / 255f, 30 / 255f, 1));
        pix.fillRectangle(5, 5, pix.getWidth() - 10, pixHeight - 10);
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 10, 10, 10, 10);
        skin.add("box_white_5", ninePatch);

        pix = new Pixmap(pixHeight, pixHeight, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        pix.setColor(Color.rgba8888(30 / 255f, 30 / 255f, 30 / 255f, 1));
        pix.fillRectangle(10, 10, pix.getWidth() - 20, pixHeight - 20);
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 10, 10, 10, 10);
        skin.add("box_white_10", ninePatch);

        ninePatch = new NinePatch(am.get("NinePatches/toast1.png", Texture.class), 15, 15, 15, 15);
        skin.add("toast1", ninePatch);

        ninePatch = new NinePatch(am.get("NinePatches/progressbar_inner.png", Texture.class), 7, 7, 7, 7);
        skin.add("progressbar_inner", ninePatch);

        ninePatch = new NinePatch(am.get("NinePatches/GameScreenTop.png", Texture.class), 15, 15, 15, 15);
        skin.add("gameScreenTop", ninePatch);

        ninePatch = new NinePatch(am.get("NinePatches/softGray.png", Texture.class), 15, 15, 15, 15);
        skin.add("softGray", ninePatch);

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

        skin.add("cog", am.get("cog.png"));
        skin.add("userDefIcon", am.get("userIcon.png"));
        skin.add("ball", am.get("ball.png"));
        skin.add("asteroid", am.get("asteroid.png"));
        skin.add("map", am.get("map.png"));
        skin.add("speaker", am.get("speaker.png"));
        skin.add("timeIcon", am.get("HourGlass.png"));
        skin.add("movesIcon", am.get("MovesIcon.png"));
        skin.add("heartIcon", am.get("HeartIcon.png"));
        skin.add("gameScreenTopRound", am.get("UIGameScreenTopRound.png"));

        // Fonts
//        registerFont(skin, "h5", "comic_32.fnt");
//        registerFont(skin, "h4", "comic_48.fnt");

        registerFont(skin, "h6", "gidole_21.fnt");
        registerFont(skin, "h5", "gidole_28.fnt");
        registerFont(skin, "h4", "gidole_37.fnt");
        registerFont(skin, "h3", "gidole_50.fnt");
        registerFont(skin, "h2", "gidole_67.fnt");
        registerFont(skin, "h1", "gidole_83.fnt");

        registerFont(skin, "comic_72bo", "comic_72bo.fnt");
        registerFont(skin, "comic_96bo", "comic_96bo.fnt");

        // 96   84  72  60  48  36

        // TextButtonStyles
        TextButton.TextButtonStyle stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("box_white_10", Color.WHITE);
        stb.down = skin.newDrawable("box_white_10", Color.GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h1");
        skin.add("menuButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("box_white_10", Color.WHITE);
        stb.down = skin.newDrawable("box_white_10", Color.GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h4");
        skin.add("modeButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("box_white_5", Color.WHITE);
        stb.down = skin.newDrawable("box_white_5", Color.GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h4");
        skin.add("default", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("box_white_5", Color.GRAY);
        stb.down = skin.newDrawable("box_white_5", Color.DARK_GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h4");
        skin.add("box_gray_5", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("asteroid", Color.WHITE);
        stb.down = skin.newDrawable("asteroid", Color.GRAY);
        stb.disabled = skin.newDrawable("asteroid", Color.DARK_GRAY);
        stb.font = skin.getFont("h2");
        stb.fontColor = Color.BLACK;
        skin.add("levelButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("myBall", Color.CORAL);
        stb.down = skin.newDrawable("myBall", Color.RED);
        stb.font = skin.getFont("h4");
        skin.add("tmpPowerup", stb);

        // SliderStyles
        Slider.SliderStyle ss = new Slider.SliderStyle();
        ss.background = skin.newDrawable("box_white_5");
        ss.background.setMinHeight(20);
        ss.knob = skin.getDrawable("myBall");
        ss.knob.setMinHeight(40);
        ss.knob.setMinWidth(40);
        skin.add("default-horizontal", ss);

        // CheckboxStyles
        CheckBox.CheckBoxStyle cbs = new CheckBox.CheckBoxStyle();
        cbs.checkboxOff = skin.newDrawable("box_white_10", Color.RED);
        cbs.checkboxOn = skin.newDrawable("box_white_10", Color.GREEN);
        cbs.font = skin.getFont("h4");
        cbs.disabledFontColor = Color.DARK_GRAY;
        skin.add("default", cbs);

        // Textfield
        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.background = skin.getDrawable("box_white_5");
        tfs.font = skin.getFont("h4");
        tfs.fontColor = Color.WHITE;
        skin.add("default", tfs);

    }

    private void registerFont(Skin skin, String fntName, String path) {
        BitmapFont bf = am.get(path);
        bf.setFixedWidthGlyphs("1234567890 ");
        skin.add(fntName, bf);
        Label.LabelStyle ls = new Label.LabelStyle(bf, Color.WHITE);
        skin.add(fntName, ls);
    }

}
