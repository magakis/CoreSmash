package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
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
import com.breakthecore.ui.UIUnits;

import java.util.Locale;

public class LoadingScreen extends ScreenBase {
    private AssetManager am;
    private Stage stage;
    private Skin skin;
    private Label percent;
    private AbstractTheme baseTheme;
    private final float PPI = Gdx.graphics.getDensity();

    private TextureLoader.TextureParameter textureParam;

    public LoadingScreen(CoreSmash game) {
        super(game);

        stage = new Stage(gameInstance.getUIViewport());

        am = game.getAssetManager();

        textureParam = new TextureLoader.TextureParameter();
        textureParam.genMipMaps = true;
        textureParam.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        textureParam.magFilter = Texture.TextureFilter.Nearest;

        FileHandleResolver resolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        generateBitmapFont(48, "h1.ttf");
        am.finishLoading();

        skin = game.getSkin();
        percent = new Label("null", new Label.LabelStyle(am.get("h1.ttf", BitmapFont.class), Color.WHITE));
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
        loadTexture("DefaultUserIcon.png");
        loadTexture("toast1.png");
        loadTexture("dialog1.png");
        loadTexture("GameScreenTop.png");
        loadTexture("softGray.png");
        loadTexture("progressbar_inner.png");
        loadTexture("map.png");
        loadTexture("SimpleOrangeBorder.png");
        loadTexture("BorderTrans.png");
        loadTexture("default.png");
        loadTexture("UIGameScreenTopRound.png");
        loadTexture("MovesIcon.png");
        loadTexture("HourGlass.png");
        loadTexture("HeartIcon.png");
        loadTexture("LevelBuilderButton.png");
    }

    private void loadAllBitmapFonts() {
        loadBitmapFont("comic_24b.fnt");
        loadBitmapFont("comic_32b.fnt");
        loadBitmapFont("comic_48b.fnt");
        loadBitmapFont("comic_96b.fnt");

        loadBitmapFont("comic_72bo.fnt");
        loadBitmapFont("comic_96bo.fnt");

        loadBitmapFont("gidole_24.fnt");
        loadBitmapFont("gidole_36.fnt");
        loadBitmapFont("gidole_48.fnt");
        loadBitmapFont("gidole_60.fnt");
        loadBitmapFont("gidole_72.fnt");
        loadBitmapFont("gidole_96.fnt");

//        generateBitmapFont(12, "h6.ttf");
//        generateBitmapFont(16, "h5.ttf");
//        generateBitmapFont(22, "h4.ttf");
//        generateBitmapFont(28, "h3.ttf");
//        generateBitmapFont(36, "h2.ttf");

        generateBitmapFont(12, "h6.ttf");
        generateBitmapFont(14, "h5.ttf");
        generateBitmapFont(18, "h4.ttf");
        generateBitmapFont(24, "h3.ttf");
        generateBitmapFont(36, "h2.ttf");
    }

    public static void loadAllBalls() {
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

    private void setupSkin() {
        Pixmap pix;
        Texture tex;
        NinePatch ninePatch;

        /* Used as the Original Scale of each asset */
        float defScale = 0;

        // Nine-Patches
        pix = new Pixmap(4, 4, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 1, 1, 1, 1);
        skin.add("flatColor", ninePatch);

        pix = new Pixmap(30, 30, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        pix.setColor(Color.rgba8888(30 / 255f, 30 / 255f, 30 / 255f, 1));
        pix.fillRectangle(5, 5, pix.getWidth() - 10, 30 - 10);
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 10, 10, 10, 10);
        skin.add("box_white_5", ninePatch);

        pix = new Pixmap(30, 30, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        pix.setColor(Color.rgba8888(30 / 255f, 30 / 255f, 30 / 255f, 1));
        pix.fillRectangle(10, 10, pix.getWidth() - 20, 30 - 20);
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 10, 10, 10, 10);
        skin.add("box_white_10", ninePatch);


        ninePatch = new NinePatch(am.get("toast1.png", Texture.class), 15, 15, 15, 15);
        skin.add("toast1", ninePatch);

        ninePatch = new NinePatch(am.get("BorderTrans.png", Texture.class), 5, 5, 5, 5);
        skin.add("borderTrans", ninePatch);

        ninePatch = new NinePatch(am.get("progressbar_inner.png", Texture.class), 7, 7, 7, 7);
        skin.add("progressbar_inner", ninePatch);

        ninePatch = new NinePatch(am.get("GameScreenTop.png", Texture.class), 24, 24, 24, 24);
        ninePatch.scale(.5f, .5f);
        skin.add("gameScreenTop", ninePatch);

        ninePatch = new NinePatch(am.get("LevelBuilderButton.png", Texture.class), 15, 15, 15, 15);
        defScale = 1f;
        ninePatch.scale(defScale * Gdx.graphics.getDensity(), defScale * Gdx.graphics.getDensity());
        skin.add("levelBuilderButton", ninePatch);

        ninePatch = new NinePatch(am.get("softGray.png", Texture.class), 31, 31, 31, 31);
        defScale = 0.5f;
        ninePatch.scale(defScale * Gdx.graphics.getDensity(), defScale * Gdx.graphics.getDensity());
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
        skin.add("userDefIcon", am.get("DefaultUserIcon.png"));
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

        registerFont(skin, "h6", "h6.ttf");
        registerFont(skin, "h5", "h5.ttf");
        registerFont(skin, "h4", "h4.ttf");
        registerFont(skin, "h3", "h3.ttf");
        registerFont(skin, "h2", "h2.ttf");
        registerFont(skin, "h1", "h1.ttf");

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
        stb.up = skin.newDrawable("borderTrans", Color.WHITE);
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
        stb.font = skin.getFont("h3");
        stb.up = skin.getDrawable("box_white_5");
        stb.down = skin.newDrawable("box_white_5", Color.GRAY);
        skin.add("dialogButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("asteroid", Color.WHITE);
        stb.down = skin.newDrawable("asteroid", Color.GRAY);
        stb.disabled = skin.newDrawable("asteroid", Color.DARK_GRAY);
        stb.font = skin.getFont("h2");
        stb.fontColor = Color.BLACK;
        skin.add("levelButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("levelBuilderButton");
        stb.down = skin.newDrawable("levelBuilderButton", Color.GRAY);
        stb.disabled = skin.newDrawable("levelBuilderButton", Color.DARK_GRAY);
        stb.font = skin.getFont("h3");
        skin.add("levelBuilderButton", stb);

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


        UIUnits.setUnitActor(skin.getFont("h6"));
    }

    private void loadTexture(String name) {
        am.load(name, Texture.class, textureParam);
    }

    private void loadBitmapFont(String name) {
        am.load(name, BitmapFont.class);
    }

    private void generateBitmapFont(int size, String name) {
        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "Gidole-Regular.ttf";
        fontParams.fontParameters.size = (int) (size * PPI);
        am.load(name, BitmapFont.class, fontParams);
    }

    private void registerFont(Skin skin, String fntName, String path) {
        BitmapFont bf = am.get(path);
        bf.setFixedWidthGlyphs("1234567890 ");
        skin.add(fntName, bf);
        Label.LabelStyle ls = new Label.LabelStyle(bf, Color.WHITE);
        skin.add(fntName, ls);
    }
}
