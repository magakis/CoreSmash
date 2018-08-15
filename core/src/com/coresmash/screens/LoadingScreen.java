package com.coresmash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.coresmash.sound.SoundManager;
import com.coresmash.themes.AbstractTheme;
import com.coresmash.themes.BaseTheme;
import com.coresmash.tiles.TileType.PowerupType;
import com.coresmash.ui.Components;
import com.coresmash.ui.UIUtils;

import java.util.Locale;

public class LoadingScreen extends ScreenBase {
    private AssetManager am;
    private Stage stage;
    private Skin skin;
    private Label percent;
    private AbstractTheme baseTheme;
    private final float PPI;

    private TextureLoader.TextureParameter textureParam;

    public LoadingScreen(com.coresmash.CoreSmash game) {
        super(game);

        PPI = Gdx.graphics.getDensity();
        stage = new Stage(gameInstance.getUIViewport());
        am = game.getAssetManager();

        textureParam = new TextureLoader.TextureParameter();
        textureParam.genMipMaps = true;
        textureParam.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        textureParam.magFilter = Texture.TextureFilter.MipMapLinearNearest;

        FileHandleResolver internalResolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(internalResolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(internalResolver));

        ResolutionFileResolver.Resolution p1920x1080 = new ResolutionFileResolver.Resolution(1920, 1080, "p1920x1080");
        ResolutionFileResolver textureResolver = new ResolutionFileResolver(internalResolver, p1920x1080);
        am.setLoader(Texture.class, new TextureLoader(textureResolver));

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
        loadSounds();

        baseTheme = new BaseTheme();
        baseTheme.queueForLoad(am);
        gameInstance.getRenderManager().setTheme(baseTheme);
    }

    @Override
    public void render(float delta) {
        if (am.update()) {
            baseTheme.finishLoading();
            setupFonts();
            setupSounds();
            setupSkin();
            Components.initialize(skin);
            UIUtils.initialize();
            gameInstance.setScreen(new com.coresmash.screens.MainMenuScreen(gameInstance));
        }
        percent.setText(String.format(Locale.ENGLISH, "%.0f %%", am.getProgress() * 100));

        stage.act();
        stage.draw();
    }

    private void loadAllTextures() {
        loadTexture("ball.png");
        loadTexture("cog.png");
        loadTexture("speaker.png");
        loadTexture("DefaultUserIcon.png");
        loadTexture("toast1.png");
        loadTexture("GameScreenTop.png");
        loadTexture("softGray.png");
        loadTexture("progressbar_inner.png");
        loadTexture("map.png");
        loadTexture("BorderTrans.png");
        loadTexture("default.png");
        loadTexture("UIGameScreenTopRound.png");
        loadTexture("MovesIcon.png");
        loadTexture("HourGlass.png");
        loadTexture("HeartIcon.png");
        loadTexture("Heart.png");
        loadTexture("LevelBuilderButton.png");
        loadTexture("SpikyBall.png");
        loadTexture("card.png");
        loadTexture("CardRewardShade.png");
        loadTexture("SlotMachine.png");
        loadTexture("FrameWooden.png");
        loadTexture("UserAccountFrame.png");
        loadTexture("LotteryCoin.png");
        loadTexture("ButtonPowerup.png");
        loadTexture("ButtonPowerupFrame.png");
        loadTexture("ButtonLottery.png");
        loadTexture("ButtonPlay.png");
        loadTexture("ButtonCancel.png");
        loadTexture("ButtonStart.png");
        loadTexture("ButtonLevel.png");
        loadTexture("ButtonOpen.png");
        loadTexture("ButtonClose.png");
        loadTexture("ButtonClaim.png");
        loadTexture("ButtonRetry.png");
        loadTexture("ButtonEditor.png");
        loadTexture("ButtonMenu.png");
        loadTexture("MenuBackground.png");
        loadTexture("CampaignBackground.png");
        loadTexture("BrightOuterDarkInner.png");
        loadTexture("FrameColored.png");
        loadTexture("BoardTime.png");
        loadTexture("BoardScore.png");
        loadTexture("BoardBackground.png");
        loadTexture("BoardCenter.png");
    }

    private void loadSounds() {
        am.load("blop.mp3", Sound.class);
        am.load("BackgroundLoop.mp3", Sound.class);
        am.load("Music.mp3", Music.class);
        am.load("click.mp3", Sound.class);
        am.load("bombExplosion.mp3", Sound.class);
    }

    private void loadAllBitmapFonts() {
        generateBitmapFont(10, "h6.ttf");
        generateBitmapFont(14, "h5.ttf");
        generateBitmapFont(18, "h4.ttf");
        generateBitmapFont(24, "h3.ttf");
        generateBitmapFont(36, "h2.ttf");

        generateBitmapFont(24, "h3f.ttf");
        generateBitmapFont(18, "h4f.ttf");

        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "Gidole-Regular.ttf";
        fontParams.fontParameters.borderWidth = 1 * PPI;
        fontParams.fontParameters.size = (int) (36 * PPI);
        am.load("h2o.ttf", BitmapFont.class, fontParams);

        fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "Gidole-Regular.ttf";
        fontParams.fontParameters.borderWidth = 1 * PPI;
        fontParams.fontParameters.size = (int) (24 * PPI);
        am.load("h3o.ttf", BitmapFont.class, fontParams);

        fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "Gidole-Regular.ttf";
        fontParams.fontParameters.borderWidth = 1 * PPI;
        fontParams.fontParameters.size = (int) (18 * PPI);
        am.load("h4o.ttf", BitmapFont.class, fontParams);
    }

    private void setupSounds() {
        SoundManager soundManager = SoundManager.get();
        soundManager.loadMusic("backgroundMusic", am.get("Music.mp3", Music.class));
        soundManager.loadSound("regularBallDestroy", am.get("blop.mp3", Sound.class));
        soundManager.loadSound("buttonClick", am.get("click.mp3", Sound.class));
        soundManager.loadSound("bombExplosion", am.get("bombExplosion.mp3", Sound.class));
    }

    private void setupFonts() {
        BitmapFont font = am.get("h3f.ttf");
        font.getData().markupEnabled = true;

        font = am.get("h4f.ttf");
        font.getData().markupEnabled = true;
    }

    private void setupSkin() {
        Pixmap pix;
        Texture tex;
        NinePatch ninePatch;
        Color SlightGray = new Color(.9f, .9f, .9f, 1);
        Color IntenseGray = new Color(0x808080ff);
        /* Used as the Original Scale of each asset */
        float defScale = 0;

        // Nine-Patches
        pix = new Pixmap(30, 30, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        pix.setColor(Color.rgba8888(30 / 255f, 30 / 255f, 30 / 255f, 1));
        pix.fillRectangle(10, 10, pix.getWidth() - 20, 30 - 20);
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 10, 10, 10, 10);
        defScale = .6f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("boxBig", ninePatch);

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
        defScale = .5f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("levelBuilderButton", ninePatch);

        ninePatch = new NinePatch(am.get("softGray.png", Texture.class), 31, 31, 31, 31);
        defScale = 0.5f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("softGray", ninePatch);

        ninePatch = new NinePatch(am.get("UserAccountFrame.png", Texture.class), 64, 64, 64, 64);
        defScale = 0.25f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("UserAccountFrame", ninePatch);

        ninePatch = new NinePatch(am.get("ButtonEditor.png", Texture.class), 31, 31, 31, 31);
        defScale = 0.25f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        ninePatch.setPadding(ninePatch.getPadLeft() / 2, ninePatch.getPadRight() / 2, ninePatch.getPadTop() / 2, ninePatch.getPadBottom() / 2);
        skin.add("boxSmall", ninePatch);

        ninePatch = new NinePatch(ninePatch);
        skin.add("EditorBigFrame", ninePatch);

        ninePatch = new NinePatch(am.get("FrameColored.png", Texture.class), 80, 80, 80, 80);//80//123
        defScale = .25f;
//        defScale = 1f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("simpleFrameTrans", ninePatch);


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
        skin.add("map", am.get("map.png"));
        skin.add("speaker", am.get("speaker.png"));
        skin.add("timeIcon", am.get("HourGlass.png"));
        skin.add("movesIcon", am.get("MovesIcon.png"));
        skin.add("heartIcon", am.get("HeartIcon.png"));
        skin.add("cardBack", am.get("card.png"));
        skin.add("cardShade", am.get("CardRewardShade.png"));
        skin.add("ButtonPlay", am.get("ButtonPlay.png"));
        skin.add("Heart", am.get("Heart.png"));
        skin.add("slotMachine", am.get("SlotMachine.png"));
        skin.add("gameScreenTopRound", am.get("UIGameScreenTopRound.png"));
        skin.add("LotteryCoin", am.get("LotteryCoin.png"));
        skin.add("ButtonLottery", am.get("ButtonLottery.png"));
        skin.add("ButtonPowerup", am.get("ButtonPowerup.png"));
        skin.add("ButtonPowerupFrame", am.get("ButtonPowerupFrame.png"));
        skin.add("ButtonCancel", am.get("ButtonCancel.png"));
        skin.add("ButtonStart", am.get("ButtonStart.png"));
        skin.add("ButtonOpen", am.get("ButtonOpen.png"));
        skin.add("ButtonMenu", am.get("ButtonMenu.png"));
        skin.add("ButtonClose", am.get("ButtonClose.png"));
        skin.add("ButtonClaim", am.get("ButtonClaim.png"));
        skin.add("ButtonRetry", am.get("ButtonRetry.png"));
        skin.add("ButtonLevel", am.get("ButtonLevel.png"));
        skin.add("MenuBackground", am.get("MenuBackground.png"));
        skin.add("CampaignBackground", am.get("CampaignBackground.png"));
        skin.add("BoardTime", am.get("BoardTime.png"));
        skin.add("BoardScore", am.get("BoardScore.png"));
        skin.add("BoardBackground", am.get("BoardBackground.png"));
        skin.add("BoardCenter", am.get("BoardCenter.png"));

        for (PowerupType type : PowerupType.values()) {
            skin.add(type.name(), baseTheme.getTexture(type.getType().getID()));
        }

        registerFont(skin, "h6", "h6.ttf");
        registerFont(skin, "h5", "h5.ttf");
        registerFont(skin, "h4", "h4.ttf");
        registerFont(skin, "h3", "h3.ttf");
        registerFont(skin, "h2", "h2.ttf");
        registerFont(skin, "h1", "h1.ttf");

        // Markup-Enabled fonts
        registerFont(skin, "h3f", "h3f.ttf");
        registerFont(skin, "h4f", "h4f.ttf");

        // Outlined fonts
        registerFont(skin, "h2o", "h2o.ttf");
        registerFont(skin, "h3o", "h3o.ttf");
        registerFont(skin, "h4o", "h4o.ttf");

        // 96   84  72  60  48  36

        // TextButtonStyles
        TextButton.TextButtonStyle stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("boxBig", Color.WHITE);
        stb.down = skin.newDrawable("boxBig", Color.GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h1");
        skin.add("menuButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("boxBig", Color.WHITE);
        stb.down = skin.newDrawable("boxBig", Color.GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h4");
        skin.add("modeButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("borderTrans", Color.WHITE);
        stb.down = skin.newDrawable("boxSmall", Color.GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h4");
        skin.add("default", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("boxSmall", Color.GRAY);
        stb.down = skin.newDrawable("boxSmall", Color.DARK_GRAY);
        stb.checked = stb.up;
        stb.font = skin.getFont("h4");
        skin.add("box_gray_5", stb);

        stb = new TextButton.TextButtonStyle();
        stb.font = skin.getFont("h4");
        stb.fontColor = Color.WHITE;
        stb.disabledFontColor = Color.DARK_GRAY;
        stb.disabled = skin.newDrawable("boxSmall", Color.DARK_GRAY);
        stb.up = skin.getDrawable("boxSmall");
        stb.down = skin.newDrawable("boxSmall", Color.GRAY);
        skin.add("dialogButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("ButtonLevel", Color.WHITE);
        stb.down = skin.newDrawable("ButtonLevel", SlightGray);
        stb.disabled = skin.newDrawable("ButtonLevel", IntenseGray);
        stb.font = skin.getFont("h4o");
        stb.fontColor = Color.WHITE;
        stb.disabledFontColor = IntenseGray;
        skin.add("levelButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("boxSmall");
        stb.down = skin.newDrawable("boxSmall", Color.GRAY);
        stb.disabled = skin.newDrawable("boxSmall", Color.DARK_GRAY);
        stb.font = skin.getFont("h5");
        skin.add("levelBuilderButton", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.newDrawable("boxSmall");
        stb.down = skin.newDrawable("boxSmall", Color.GRAY);
        stb.checked = skin.newDrawable("boxSmall", Color.GREEN);
        stb.disabled = skin.newDrawable("boxSmall", Color.DARK_GRAY);
        stb.font = skin.getFont("h5");
        skin.add("levelBuilderButtonChecked", stb);

        stb = new TextButton.TextButtonStyle();
        stb.up = skin.getDrawable("boxSmall");
        stb.down = skin.newDrawable("boxSmall", Color.GRAY);
        stb.disabled = skin.newDrawable("boxSmall", Color.DARK_GRAY);
        stb.font = skin.getFont("h4");
        stb.fontColor = Color.WHITE;
        stb.disabledFontColor = Color.DARK_GRAY;
        skin.add("tmpPowerup", stb);

        //ImageButton
        ImageButton.ImageButtonStyle imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonStart");
        imgbs.imageDown = skin.newDrawable("ButtonStart", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonStart", Color.DARK_GRAY);
        skin.add("ButtonStart", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonCancel");
        imgbs.imageDown = skin.newDrawable("ButtonCancel", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonCancel", Color.DARK_GRAY);
        skin.add("ButtonCancel", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonLottery");
        imgbs.imageDown = skin.newDrawable("ButtonLottery", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonLottery", Color.DARK_GRAY);
        skin.add("ButtonLottery", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonClose");
        imgbs.imageDown = skin.newDrawable("ButtonClose", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonClose", Color.DARK_GRAY);
        skin.add("ButtonClose", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonRetry");
        imgbs.imageDown = skin.newDrawable("ButtonRetry", Color.DARK_GRAY);
        imgbs.imageDisabled = skin.newDrawable("ButtonRetry", Color.DARK_GRAY);
        skin.add("ButtonRetry", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonClaim");
        imgbs.imageDown = skin.newDrawable("ButtonClaim", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonClaim", Color.DARK_GRAY);
        skin.add("ButtonClaim", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonOpen");
        imgbs.imageDown = skin.newDrawable("ButtonOpen", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonOpen", Color.DARK_GRAY);
        skin.add("ButtonOpen", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonMenu");
        imgbs.imageDown = skin.newDrawable("ButtonMenu", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonMenu", Color.DARK_GRAY);
        skin.add("ButtonMenu", imgbs);

        // SliderStyles
        Slider.SliderStyle ss = new Slider.SliderStyle();
        ss.background = skin.newDrawable("boxSmall");
        ss.background.setMinHeight(15 * PPI);
        ss.knob = skin.getDrawable("myBall");
        ss.knob.setMinHeight(20 * PPI);
        ss.knob.setMinWidth(20 * PPI);
        skin.add("default-horizontal", ss);

        // CheckboxStyles
        CheckBox.CheckBoxStyle cbs = new CheckBox.CheckBoxStyle();
        cbs.checkboxOff = skin.newDrawable("boxBig", Color.RED);
        cbs.checkboxOn = skin.newDrawable("boxBig", Color.GREEN);
        cbs.font = skin.getFont("h5");
        cbs.disabledFontColor = Color.DARK_GRAY;
        skin.add("default", cbs);

        // Textfield
        TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.background = skin.newDrawable("boxSmall");
        tfs.background.setRightWidth(tfs.background.getRightWidth() / 3);
        tfs.background.setLeftWidth(tfs.background.getLeftWidth() / 3);
        tfs.font = skin.getFont("h4");
        tfs.fontColor = Color.WHITE;
        skin.add("default", tfs);

        //ListStyles
        List.ListStyle ls = new List.ListStyle();
        ls.fontColorSelected = Color.GREEN;
        ls.fontColorUnselected = Color.WHITE;
        ls.selection = skin.newDrawable("boxSmall", Color.BLACK);
        ls.font = skin.getFont("h5");
        skin.add("default", ls);

        // ButtonStyles
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = skin.getDrawable("boxSmall");
        bs.down = skin.newDrawable("boxSmall", Color.GRAY);
        bs.disabled = skin.newDrawable("boxSmall", Color.DARK_GRAY);
        skin.add("default", bs);

        bs = new Button.ButtonStyle();
        bs.up = skin.newDrawable("boxSmall", 0, 0, 0, 0);
        bs.down = skin.newDrawable("boxSmall", Color.GRAY);
        bs.disabled = skin.newDrawable("boxSmall", 0, 0, 0, 0);
        skin.add("TransWithHighlight", bs);

        Color invisible = new Color(0, 0, 0, 0);
        bs = new Button.ButtonStyle();
        bs.up = skin.newDrawable("ButtonPowerup", invisible);
        bs.down = skin.newDrawable("ButtonPowerup", invisible);
        bs.disabled = skin.newDrawable("ButtonPowerup", invisible);
        skin.add("ButtonPowerup", bs);

        // WindowStyles
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.background = skin.getDrawable("simpleFrameTrans");
        ws.titleFont = skin.getFont("h6");
        skin.add("PickPowerUpDialog", ws);

        ws = new Window.WindowStyle();
        Drawable drawable = skin.newDrawable("EditorBigFrame");
        drawable.setRightWidth(drawable.getLeftWidth() / 2);
        drawable.setLeftWidth(drawable.getLeftWidth() / 2);
        drawable.setTopHeight(drawable.getLeftWidth() / 2);
        drawable.setBottomHeight(drawable.getLeftWidth() / 2);
        ws.background = drawable;
        ws.titleFont = skin.getFont("h6");
        skin.add("default", ws);

        UIUtils.setUnitActor(skin.getFont("h6"));
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
