package com.archapp.coresmash.screens;

import com.archapp.coresmash.CoreSmash;
import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.levelbuilder.LevelBuilderScreen;
import com.archapp.coresmash.levels.CampaignScreen;
import com.archapp.coresmash.sound.SoundManager;
import com.archapp.coresmash.ui.SettingsDialog;
import com.archapp.coresmash.ui.UIComponent;
import com.archapp.coresmash.ui.UIFactory;
import com.archapp.coresmash.ui.UIUtils;
import com.archapp.coresmash.utlis.FileUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Michail on 16/3/2018.
 */

public class MainMenuScreen extends ScreenBase {
    private CampaignScreen campaignScreen;
    private LevelBuilderScreen levelBuilderScreen;
    private Stage stage;
    private Skin skin;
    private Stack rootStack;
    private UIComponent uiMenuOverlay;
    private UIMainMenu uiMainMenu;
    private SettingsDialog settingsDialog;
    private ScreenMessageDialog welcomeMessage;

    /* TODO: Should be part of some options class */

    public MainMenuScreen(CoreSmash game) {
        super(game);
        Viewport viewport = game.getUIViewport();
        stage = new Stage(viewport);
        setupMainMenuStage(stage);

        settingsDialog = new SettingsDialog(skin);

        screenInputMultiplexer.addProcessor(stage);
        screenInputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    levelBuilderScreen.saveProgress();
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });

        campaignScreen = new CampaignScreen(gameInstance);
        levelBuilderScreen = new LevelBuilderScreen(gameInstance);
        SoundManager.get().playMenuMusic();


//                readAllBytes(Paths.get(Gdx.files.internal("docs/alpha_message.txt").path()));

        welcomeMessage = new ScreenMessageDialog(skin);

        welcomeMessage.setTitle("Core Smash")
                .setSubTitle("PRE-ALPHA TEST")
                .setMessage(FileUtils.fileToString(Gdx.files.internal("docs/alpha_message").reader(512))).show(stage);
    }

    @Override
    public void hide() {
        super.hide();
        if (welcomeMessage.isVisible())
            welcomeMessage.hide(null);

    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    private void setupMainMenuStage(Stage stage) {
        skin = gameInstance.getSkin();
        rootStack = new Stack();
        rootStack.setFillParent(true);
        stage.addActor(rootStack);

        uiMainMenu = new UIMainMenu();
        uiMenuOverlay = new UIOverlay();

        Image background = new Image(skin.getDrawable("MenuBackground"));
        background.setScaling(Scaling.fill);
        background.setAlign(Align.top);

        rootStack.add(background);
        rootStack.add(uiMainMenu.getRoot());
        rootStack.add(uiMenuOverlay.getRoot());
    }

    private void checkForLocalAccount() {
        Preferences prefs = Gdx.app.getPreferences("account");

        if (!prefs.getBoolean("valid", false)) {
            Gdx.input.getTextInput(new Input.TextInputListener() {
                @Override
                public void input(String text) {
                    if (text.length() < 3 || text.length() > 20) {
                        checkForLocalAccount();// XXX(26/4/2018): Recursive...
                        return;
                    }
                    Preferences prefs = Gdx.app.getPreferences("account");
                    prefs.putString("username", text);
                    prefs.putBoolean("valid", true);
                    prefs.flush();
                    gameInstance.getUserAccount().setUsername(text);
                }

                @Override
                public void canceled() {
                    checkForLocalAccount();
                }
            }, "Setup Username:", "", "Min 3 - Max 20 Characters");
        }
    }

    private class UIMainMenu implements UIComponent {
        WidgetGroup root;

        public UIMainMenu() {
            root = new WidgetGroup();

            Container<ImageButton> imgPlay = newMenuButton();

//            root.defaults()
//                    .width(Value.percentWidth(3 / 5f, rootStack))
//                    .height(Value.percentHeight(2 / 14f, rootStack));

            Label versInfo = new Label(CoreSmash.APP_VERSION + " | Michail Angelos Gakis", skin, "h6", Color.DARK_GRAY);
            versInfo.setAlignment(Align.bottom);

//            root.bottom();
//            root.add(imgPlay).padBottom(stage.getHeight()/14).row();
//            root.add(versInfo).align(Align.center).height(versInfo.getPrefHeight());

            root.addActor(imgPlay);
            root.addActor(versInfo);

            versInfo.setPosition(stage.getWidth() / 2, 10 * Gdx.graphics.getDensity(), Align.center);
            imgPlay.setPosition(stage.getWidth() / 2, stage.getHeight() / 6, Align.bottom);
        }

        private Container<ImageButton> newMenuButton() {
            ImageButton bt = UIFactory.createImageButton(skin.getDrawable("ButtonMenuPlay"), skin.newDrawable("ButtonMenuPlay", Color.GRAY));
            bt.getImageCell().grow();
            bt.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    campaignScreen.updateInfo(); // XXX(26/5/2018): Remove this
                    gameInstance.setScreen(campaignScreen);
                }
            });

            Container<ImageButton> result = new Container<>(bt);
            result.setTransform(true);
            result.fill();
            result.setRotation(-.25f);
            result.size(stage.getHeight() / 6);
            result.setOrigin(Align.center);
            result.addAction(Actions.forever(
                    Actions.sequence(
                            Actions.rotateBy(1.5f, 1.5f),
                            Actions.rotateBy(-1.5f, 1.5f)
                    )));
            result.addAction(Actions.forever(
                    Actions.sequence(
                            Actions.scaleBy(.08f, .04f, 0.75f),
                            Actions.scaleBy(-.08f, -.04f, 0.75f)
                    )));

            return result;
        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class UIOverlay implements UIComponent {
        Table root;

        public UIOverlay() {
            ImageButton.ImageButtonStyle imgbsMap = new ImageButton.ImageButtonStyle();
            imgbsMap.up = skin.getDrawable("boxSmall");
            imgbsMap.imageUp = skin.getDrawable("map");
            imgbsMap.imageDown = skin.newDrawable("map", Color.RED);

            ImageButton imgbMap = new ImageButton(imgbsMap);
            imgbMap.getImage().setScaling(Scaling.fill);
            imgbMap.getImageCell().padLeft(5).pad(3);
            imgbMap.addListener(UIUtils.getButtonSoundListener());
            imgbMap.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    gameInstance.setScreen(levelBuilderScreen);
                }
            });

            ImageButton imbSettings = new ImageButton(skin, "ButtonSettings");
            imbSettings.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    settingsDialog.show(stage);
                }
            });

            root = new Table();
            root.bottom().left();
            root.add(imgbMap)
                    .size(Value.percentWidth(1 / 7f, rootStack))
                    .maxSize(Value.percentHeight(1/8f, rootStack))
                    .padBottom(Value.percentHeight(1 / 16f, rootStack))
                    .padLeft(-10)
                    .left().expandX();
            root.add(imbSettings)
                    .size(Value.percentWidth(1 / 10f, rootStack))
                    .maxSize(Value.percentHeight(1 / 8f, rootStack))
                    .padBottom(Value.percentHeight(1 / 16f, rootStack))
                    .padRight(5 * Gdx.graphics.getDensity())
                    .right().expandX();

        }

        @Override
        public Group getRoot() {
            return root;
        }
    }

    private class MessageDialog extends Dialog {
        private Label lblMessage;
        private Label lblTitle;
        private ScrollPane scrollPane;

        MessageDialog(Skin skin) {
            super("", skin, "PopupMessage");

            float width = stage.getWidth() * .6f - getPadLeft() - getPadRight();
            float height = stage.getHeight() / 6f - getPadBottom() - getPadTop();

            lblMessage = new Label("", skin, "h5");
            lblTitle = new Label("", skin, "h4");
            lblMessage.setWrap(true);
            scrollPane = new ScrollPane(lblMessage);
            scrollPane.setScrollingDisabled(true, false);
            scrollPane.setOverscroll(false, false);

            getContentTable().add(lblTitle).row();
            getContentTable().add(scrollPane).grow();
            getCell(getContentTable()).width(width).maxHeight(height);
            getCell(getButtonTable()).width(width);
            setModal(false);
            setMovable(false);
            button("Ok");
        }

        public MessageDialog setTitle(String msg) {
            lblTitle.setText(msg);
            return this;
        }

        public MessageDialog setMessage(String msg) {
            lblMessage.setText(msg);
            return this;
        }

        @Override
        public Dialog show(Stage stage) {
            return show(stage, null);
        }

        public Dialog show(Stage stage, Action action) {
            scrollPane.getActor().setWidth(scrollPane.getWidth());
            super.show(stage, action);

            setPosition(stage.getWidth(), stage.getHeight() * .8f, Align.topRight);
            return this;
        }
    }

    private class ScreenMessageDialog extends Dialog {
        private Label lblMessage;
        private Label lblTitle;
        private Label lblSubTitle;
        private ScrollPane scrollPane;

        ScreenMessageDialog(Skin skin) {
            super("", skin, "PopupMessage");

            float width = WorldSettings.getDefaultDialogSize() - getPadLeft() - getPadRight();

            lblMessage = new Label("", skin, "h5");
            lblTitle = new Label("", skin, "h1");
            lblSubTitle = new Label("", skin, "h3");
            lblMessage.setWrap(true);
            scrollPane = new ScrollPane(lblMessage);
            scrollPane.setScrollingDisabled(true, false);
            scrollPane.setOverscroll(false, false);

            getContentTable().defaults().space(0);
            getContentTable().add(lblTitle).padTop(-lblTitle.getPrefHeight() * .1f).row();
            getContentTable().add(lblSubTitle).padTop(-lblTitle.getPrefHeight() * .25f).row();
            getContentTable().add(scrollPane).padLeft(width * .025f).padRight(width * .025f).grow();
            getCell(getContentTable()).width(width).padBottom(width * .025f).maxHeight(stage.getHeight() * .7f);
            getCell(getButtonTable()).width(width);
            setModal(true);
            setMovable(false);

            ImageButton btnIUnderstand = UIFactory.createImageButton(skin, "ButtonIUnderstand");
            btnIUnderstand.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hide(null);
                }
            });

            float buttonSize = width * .12f;
            getButtonTable().add(btnIUnderstand).height(buttonSize).width(UIUtils.getWidthFor(btnIUnderstand.getImage().getDrawable(), buttonSize));
        }

        public ScreenMessageDialog setSubTitle(String subTitle) {
            lblSubTitle.setText(subTitle);
            return this;
        }

        public ScreenMessageDialog setTitle(String title) {
            lblTitle.setText(title);
            return this;
        }

        public ScreenMessageDialog setMessage(String msg) {
            lblMessage.setText(msg);
            return this;
        }

        @Override
        public Dialog show(Stage stage) {
            return show(stage, null);
        }

        public Dialog show(Stage stage, Action action) {
            super.show(stage, action);

            setPosition(stage.getWidth() / 2f, stage.getHeight() / 2f, Align.center);
            return this;
        }
    }
}
