package com.archapp.coresmash.screens;

import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.sound.SoundManager.MusicTrack;
import com.archapp.coresmash.sound.SoundManager.SoundTrack;
import com.archapp.coresmash.themes.AbstractTheme;
import com.archapp.coresmash.themes.BaseTheme;
import com.archapp.coresmash.tiles.TileType.PowerupType;
import com.archapp.coresmash.ui.Components;
import com.archapp.coresmash.ui.UIUtils;
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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

import java.util.Locale;

public class LoadingScreen extends ScreenBase {
    private AssetManager am;
    private Stage stage;
    private Skin skin;
    private Label percent;
    private AbstractTheme baseTheme;
    private final float PPI;

    private final String SOUND_DIR = "sound/";

    private TextureLoader.TextureParameter textureParam;

    public LoadingScreen(CoreSmash game) {
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
            gameInstance.setScreen(new MainMenuScreen(gameInstance));
        }
        percent.setText(String.format(Locale.ENGLISH, "%.0f %%", am.getProgress() * 100));

        stage.act();
        stage.draw();
    }

    private void loadAllTextures() {
        loadTexture("DefaultUserIcon.png");
        loadTexture("toast1.png");
        loadTexture("softGray.png");
        loadTexture("progressbar_inner.png");
        loadTexture("map.png");
        loadTexture("default.png");
        loadTexture("UIGameScreenTopRound.png");
        loadTexture("Heart.png");
        loadTexture("RedCross.png");
        loadTexture("CenterTileIndicator.png");
        loadTexture("LevelBuilderButton.png");
        loadTexture("UserBoardBackground.png");
        loadTexture("LotteryCard.png");
        loadTexture("CardRewardShade.png");
        loadTexture("FrameWooden.png");
        loadTexture("UserAccountFrame.png");
        loadTexture("LotteryCoin.png");
        loadTexture("UserLevelBackground.png");
        loadTexture("NotificationIndicator.png");

        loadTexture("GrayStar.png");
        loadTexture("Star.png");
        loadTexture("Trophy.png");

        loadTexture("ButtonPowerup.png");
        loadTexture("ButtonLottery.png");
        loadTexture("ButtonMenuPlay.png");

        loadTexture("MessageFrame.png");

        loadTexture("ButtonCancel.png");
        loadTexture("ButtonPlay.png");
        loadTexture("ButtonGiveUp.png");
        loadTexture("ButtonPlayOnFree.png");
        loadTexture("ButtonPlayOnAd.png");
        loadTexture("ButtonLevel.png");
        loadTexture("ButtonFreePick.png");
        loadTexture("ButtonPick.png");
        loadTexture("ButtonClose.png");
        loadTexture("ButtonClaim.png");
        loadTexture("ButtonPickAgain.png");
        loadTexture("ButtonEditor.png");
        loadTexture("ButtonMenu.png");
        loadTexture("ButtonSettings.png");
        loadTexture("ButtonIUnderstand.png");
        loadTexture("ButtonGotIt.png");
        loadTexture("ButtonReturn.png");
        loadTexture("ButtonHowToPlay.png");
        loadTexture("ButtonFeedback.png");
        loadTexture("ButtonFreeHeart.png");
        loadTexture("ButtonSpeedUp.png");
        loadTexture("ButtonGoogleGamesSignIn.png");

        loadTexture("MenuBackground.png");
        loadTexture("CampaignBackground.png");
        loadTexture("BrightOuterDarkInner.png");
        loadTexture("FrameColored.png");

        am.load("atlas/gameui.atlas", TextureAtlas.class);
    }

    private void loadSounds() {
        am.load(SOUND_DIR + "menu_music.ogg", Music.class);
        am.load(SOUND_DIR + "game_music.ogg", Music.class);

        am.load(SOUND_DIR + "blop.ogg", Sound.class);
        am.load("click.mp3", Sound.class);
        am.load(SOUND_DIR + "explosion1.ogg", Sound.class);
        am.load(SOUND_DIR + "launch1.ogg", Sound.class);
        am.load(SOUND_DIR + "astronaut_release.ogg", Sound.class);
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
        fontParams.fontFileName = "BubblegumSans.otf";
        fontParams.fontParameters.borderWidth = 1 * PPI;
        fontParams.fontParameters.size = (int) (36 * PPI);
        am.load("h2o.ttf", BitmapFont.class, fontParams);

        fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "BubblegumSans.otf";
        fontParams.fontParameters.borderWidth = 1 * PPI;
        fontParams.fontParameters.size = (int) (24 * PPI);
        am.load("h3o.ttf", BitmapFont.class, fontParams);

        fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "BubblegumSans.otf";
        fontParams.fontParameters.borderWidth = 1 * PPI;
        fontParams.fontParameters.size = (int) (18 * PPI);
        am.load("h4o.ttf", BitmapFont.class, fontParams);

        fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "BubblegumSans.otf";
        fontParams.fontParameters.size = (int) (24 * PPI);
        fontParams.fontParameters.shadowColor = Color.BLACK;
        fontParams.fontParameters.shadowOffsetX = (int) (1 * PPI);
        fontParams.fontParameters.shadowOffsetY = (int) (1 * PPI);
        am.load("h3s.ttf", BitmapFont.class, fontParams);
    }

    private void setupSounds() {
        SoundManager soundManager = SoundManager.get();
        soundManager.loadMusic(MusicTrack.MENU_MUSIC, am.get(SOUND_DIR + "menu_music.ogg", Music.class));
        soundManager.loadMusic(MusicTrack.GAME_MUSIC, am.get(SOUND_DIR + "game_music.ogg", Music.class));

        soundManager.loadSound(SoundTrack.REGULAR_BALL_DESTROY, am.get(SOUND_DIR + "blop.ogg", Sound.class));
        soundManager.loadSound(SoundTrack.BUTTON_CLICK, am.get("click.mp3", Sound.class));
        soundManager.loadSound(SoundTrack.FIREBALL_EXPLOSION, am.get(SOUND_DIR + "explosion1.ogg", Sound.class));
        soundManager.loadSound(SoundTrack.FIREBALL_LAUNCH, am.get(SOUND_DIR + "launch1.ogg", Sound.class));
        soundManager.loadSound(SoundTrack.ASTRONAUT_RELEASE, am.get(SOUND_DIR + "astronaut_release.ogg", Sound.class), .6f);

        soundManager.setMenuMusic(MusicTrack.MENU_MUSIC);
        soundManager.setGameMusic(MusicTrack.GAME_MUSIC);
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
        Color IntenseGray = new Color(.4f, .4f, .4f, 1);
        /* Used as the Original Scale of each asset */
        float defScale;

        // Nine-Patches
        pix = new Pixmap(30, 30, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        pix.fill();
        pix.setColor(30 / 255f, 30 / 255f, 30 / 255f, 1);
        pix.fillRectangle(10, 10, pix.getWidth() - 20, 30 - 20);
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 10, 10, 10, 10);
        defScale = .6f * PPI;
        ninePatch.scale(defScale, defScale);
        skin.add("boxBig", ninePatch);

        pix = new Pixmap(3, 3, Pixmap.Format.RGBA4444);
        pix.setColor(0, 0, 0, .65f);
        pix.fill();
        tex = new Texture(pix);
        ninePatch = new NinePatch(tex, 1, 1, 1, 1);
        skin.add("BackgroundBlack", ninePatch);

        ninePatch = new NinePatch(am.get("toast1.png", Texture.class), 15, 15, 15, 15);
        skin.add("toast1", ninePatch);

        ninePatch = new NinePatch(am.get("progressbar_inner.png", Texture.class), 7, 7, 7, 7);
        defScale = .5f * PPI;
        ninePatch.scale(defScale, defScale);
        skin.add("progressbar_inner", ninePatch);

        ninePatch = new NinePatch(am.get("LevelBuilderButton.png", Texture.class), 15, 15, 15, 15);
        defScale = .5f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("levelBuilderButton", ninePatch);

        ninePatch = new NinePatch(am.get("softGray.png", Texture.class), 31, 31, 31, 31);
        defScale = 0.5f * PPI;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("softGray", ninePatch);

        ninePatch = new NinePatch(am.get("UserBoardBackground.png", Texture.class), 40, 40, 40, 40);// 64, 64, 64, 64);
        defScale = .25f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("UserAccountFrame", ninePatch);

        ninePatch = new NinePatch(am.get("MessageFrame.png", Texture.class), 41, 41, 41, 41);
        defScale = .25f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        skin.add("PopupMessageFrame", ninePatch);

        ninePatch = new NinePatch(am.get("ButtonEditor.png", Texture.class), 31, 31, 31, 31);
        defScale = 0.25f;
        ninePatch.scale(defScale * PPI, defScale * PPI);
        ninePatch.setPadding(ninePatch.getPadLeft() / 2, ninePatch.getPadRight() / 2, ninePatch.getPadTop() / 2, ninePatch.getPadBottom() / 2);
        skin.add("boxSmall", ninePatch);

        ninePatch = new NinePatch(ninePatch);
        skin.add("EditorBigFrame", ninePatch);

        ninePatch = new NinePatch(am.get("FrameColored.png", Texture.class), 40, 40, 40, 40);//80//123
        defScale = .25f * PPI;
        ninePatch.scale(defScale, defScale);
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

        pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.alpha(0));
        pix.fill();
        tex = new Texture(pix);
        skin.add("invisible", tex);

        skin.add("DefaultUserIcon", am.get("DefaultUserIcon.png"));
        skin.add("map", am.get("map.png"));
        skin.add("cardBack", am.get("LotteryCard.png"));
        skin.add("cardShade", am.get("CardRewardShade.png"));
        skin.add("ButtonMenuPlay", am.get("ButtonMenuPlay.png"));
        skin.add("Heart", am.get("Heart.png"));
        skin.add("RedCross", am.get("RedCross.png"));
        skin.add("gameScreenTopRound", am.get("UIGameScreenTopRound.png"));
        skin.add("LotteryCoin", am.get("LotteryCoin.png"));
        skin.add("ButtonLottery", am.get("ButtonLottery.png"));
        skin.add("ButtonPowerup", am.get("ButtonPowerup.png"));
        skin.add("ButtonCancel", am.get("ButtonCancel.png"));
        skin.add("ButtonMenuPlay", am.get("ButtonMenuPlay.png"));
        skin.add("UserLevelBackground", am.get("UserLevelBackground.png"));
        skin.add("NotificationIndicator", am.get("NotificationIndicator.png"));
        skin.add("DefaultTexture", am.get("default.png"));
        skin.add("Trophy", am.get("Trophy.png"));

        skin.add("Star", am.get("Star.png"));
        skin.add("GrayStar", am.get("GrayStar.png"));

        skin.add("ButtonPlay", am.get("ButtonPlay.png"));
        skin.add("ButtonGiveUp", am.get("ButtonGiveUp.png"));
        skin.add("ButtonPlayOnFree", am.get("ButtonPlayOnFree.png"));
        skin.add("ButtonPlayOnAd", am.get("ButtonPlayOnAd.png"));
        skin.add("ButtonPick", am.get("ButtonPick.png"));
        skin.add("ButtonFreePick", am.get("ButtonFreePick.png"));
        skin.add("ButtonMenu", am.get("ButtonMenu.png"));
        skin.add("ButtonClose", am.get("ButtonClose.png"));
        skin.add("ButtonClaim", am.get("ButtonClaim.png"));
        skin.add("ButtonPickAgain", am.get("ButtonPickAgain.png"));
        skin.add("ButtonSettings", am.get("ButtonSettings.png"));
        skin.add("ButtonLevel", am.get("ButtonLevel.png"));
        skin.add("ButtonGotIt", am.get("ButtonGotIt.png"));
        skin.add("ButtonIUnderstand", am.get("ButtonIUnderstand.png"));
        skin.add("ButtonReturn", am.get("ButtonReturn.png"));
        skin.add("ButtonHowToPlay", am.get("ButtonHowToPlay.png"));
        skin.add("ButtonFeedback", am.get("ButtonFeedback.png"));
        skin.add("ButtonFreeHeart", am.get("ButtonFreeHeart.png"));
        skin.add("ButtonSpeedUp", am.get("ButtonSpeedUp.png"));
        skin.add("ButtonGoogleGamesSignIn", am.get("ButtonGoogleGamesSignIn.png"));

        skin.add("MenuBackground", am.get("MenuBackground.png"));
        skin.add("CampaignBackground", am.get("CampaignBackground.png"));

        TextureAtlas atlas = am.get("atlas/gameui.atlas");

        skin.add("timeIcon", atlas.findRegion("HourGlass"), TextureRegion.class);
        skin.add("heartIcon", atlas.findRegion("HeartIcon"), TextureRegion.class);
        skin.add("movesIcon", atlas.findRegion("MovesIcon"), TextureRegion.class);
        skin.add("BoardTime", atlas.findRegion("BoardTime"), TextureRegion.class);
        skin.add("BoardScore", atlas.findRegion("BoardScore"), TextureRegion.class);
        skin.add("BoardCenter", atlas.findRegion("BoardCenter"), TextureRegion.class);

        for (PowerupType type : PowerupType.values()) {
            skin.add(type.name(), baseTheme.getTexture(type.getType().getID()), TextureRegion.class);
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

        //Drop-Shadow fonts
        registerFont(skin, "h3s", "h3s.ttf");


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
        stb.up = skin.newDrawable("boxSmall", Color.WHITE);
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
        skin.add("ButtonLevel", stb);

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
        imgbs.imageUp = skin.getDrawable("ButtonPlay");
        imgbs.imageDown = skin.newDrawable("ButtonPlay", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonPlay", Color.DARK_GRAY);
        skin.add("ButtonPlay", imgbs);

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
        imgbs.imageUp = skin.getDrawable("ButtonPickAgain");
        imgbs.imageDown = skin.newDrawable("ButtonPickAgain", Color.DARK_GRAY);
        imgbs.imageDisabled = skin.newDrawable("ButtonPickAgain", Color.DARK_GRAY);
        skin.add("ButtonPickAgain", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonClaim");
        imgbs.imageDown = skin.newDrawable("ButtonClaim", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonClaim", Color.DARK_GRAY);
        skin.add("ButtonClaim", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonPick");
        imgbs.imageDown = skin.newDrawable("ButtonPick", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonPick", Color.DARK_GRAY);
        skin.add("ButtonPick", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonPlayOnFree");
        imgbs.imageDown = skin.newDrawable("ButtonPlayOnFree", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonPlayOnFree", Color.DARK_GRAY);
        skin.add("ButtonPlayOnFree", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonPlayOnAd");
        imgbs.imageDown = skin.newDrawable("ButtonPlayOnAd", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonPlayOnAd", Color.DARK_GRAY);
        skin.add("ButtonPlayOnAd", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonFreePick");
        imgbs.imageDown = skin.newDrawable("ButtonFreePick", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonFreePick", Color.DARK_GRAY);
        skin.add("ButtonFreePick", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonFeedback");
        imgbs.imageDown = skin.newDrawable("ButtonFeedback", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonFeedback", Color.DARK_GRAY);
        skin.add("ButtonFeedback", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonGiveUp");
        imgbs.imageDown = skin.newDrawable("ButtonGiveUp", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonGiveUp", Color.DARK_GRAY);
        skin.add("ButtonGiveUp", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonFreeHeart");
        imgbs.imageDown = skin.newDrawable("ButtonFreeHeart", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonFreeHeart", Color.DARK_GRAY);
        skin.add("ButtonFreeHeart", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonMenu");
        imgbs.imageDown = skin.newDrawable("ButtonMenu", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonMenu", Color.DARK_GRAY);
        skin.add("ButtonMenu", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonMenu");
        imgbs.imageDown = skin.newDrawable("ButtonMenu", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonMenu", Color.DARK_GRAY);
        skin.add("default", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonSettings");
        imgbs.imageDown = skin.newDrawable("ButtonSettings", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonSettings", Color.DARK_GRAY);
        skin.add("ButtonSettings", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonGotIt");
        imgbs.imageDown = skin.newDrawable("ButtonGotIt", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonGotIt", Color.DARK_GRAY);
        skin.add("ButtonGotIt", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonIUnderstand");
        imgbs.imageDown = skin.newDrawable("ButtonIUnderstand", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonIUnderstand", Color.DARK_GRAY);
        skin.add("ButtonIUnderstand", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonReturn");
        imgbs.imageDown = skin.newDrawable("ButtonReturn", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonReturn", Color.DARK_GRAY);
        skin.add("ButtonReturn", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonHowToPlay");
        imgbs.imageDown = skin.newDrawable("ButtonHowToPlay", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonHowToPlay", Color.DARK_GRAY);
        skin.add("ButtonHowToPlay", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("ButtonGoogleGamesSignIn");
        imgbs.imageDown = skin.newDrawable("ButtonGoogleGamesSignIn", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("ButtonGoogleGamesSignIn", Color.DARK_GRAY);
        skin.add("ButtonGoogleGamesSignIn", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.newDrawable("ButtonSpeedUp", 1, 1, 1, .35f);
        imgbs.imageDown = skin.getDrawable("ButtonSpeedUp");
        imgbs.imageDisabled = skin.newDrawable("ButtonSpeedUp", Color.DARK_GRAY);
        skin.add("ButtonSpeedUp", imgbs);

        imgbs = new ImageButton.ImageButtonStyle();
        imgbs.imageUp = skin.getDrawable("DefaultTexture");
        imgbs.imageDown = skin.newDrawable("DefaultTexture", SlightGray);
        imgbs.imageDisabled = skin.newDrawable("DefaultTexture", Color.DARK_GRAY);
        skin.add("default", imgbs);

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
        bs.up = skin.getDrawable("invisible");
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
        ws.background = skin.getDrawable("PopupMessageFrame");
        ws.stageBackground = skin.getDrawable("BackgroundBlack");
        ws.titleFont = skin.getFont("h6");
        skin.add("PopupMessage", ws);

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

    private void generateBitmapFont(int size, String name) {
        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "BubblegumSans.otf";
        fontParams.fontParameters.size = (int) (size * PPI);
        am.load(name, BitmapFont.class, fontParams);
    }

    private void registerFont(Skin skin, String fntName, String path) {
        BitmapFont bf = am.get(path);
        bf.setFixedWidthGlyphs("1234567890 ");
        bf.setUseIntegerPositions(false);
        skin.add(fntName, bf);
        Label.LabelStyle ls = new Label.LabelStyle(bf, Color.WHITE);
        skin.add(fntName, ls);
    }
}
