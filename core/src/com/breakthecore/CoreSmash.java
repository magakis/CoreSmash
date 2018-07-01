package com.breakthecore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.screens.LoadingScreen;
import com.breakthecore.screens.MainMenuScreen;
import com.breakthecore.screens.ScreenBase;
import com.breakthecore.ui.UIUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;

import static com.badlogic.gdx.Gdx.gl;

public class CoreSmash extends Game {
    public static String VERSION = "0.1.3.2-alpha";
    public static boolean LOG_CRASHES = true;
    public static boolean DEBUG_TABLET = false;

    private boolean isInitialized;
    private Viewport viewport;
    private RenderManager renderManager;
    private AssetManager assetManager;
    private UserAccount userAccount;
    private Skin skin;
    private InputMultiplexer inputMultiplexer;

    private Stack<ScreenBase> screenStack;

    @Override
    public void create() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable err) {
                if (LOG_CRASHES) {
                    SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
                    try (Writer writer = Gdx.files.external("/CoreSmash/crash-logs/" + format.format(Calendar.getInstance().getTime()) + ".txt").writer(false)) {
                        err.printStackTrace(new PrintWriter(writer));
                    } catch (IOException ignored) {
                    }
                }
                err.printStackTrace();
                Gdx.app.exit();
            }
        });

        WorldSettings.init();
        screenStack = new Stack<>();

//        viewport = new ExtendViewport(768, 1024);
        viewport = new ScreenViewport();

        inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(true);

        assetManager = new AssetManager();
        renderManager = new RenderManager(assetManager);

        skin = new Skin();
        userAccount = new UserAccount();

        setScreen(new LoadingScreen(this));
        gl.glClearColor(30 / 255f, 30 / 255f, 30 / 255f, 1);
    }

    @Override
    public void render() {
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        try {
            super.render();
        } catch (RuntimeException err) {
            if (LOG_CRASHES) {
                SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
                try (Writer writer = Gdx.files.external("/CoreSmash/crash-logs/" + format.format(Calendar.getInstance().getTime()) + ".txt").writer(false)) {
                    err.printStackTrace(new PrintWriter(writer));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            throw err;
        }
    }

    public Skin getSkin() {
        return skin;
    }

    public void setInputProcessor(InputProcessor ip) {
        inputMultiplexer.clear();
        inputMultiplexer.addProcessor(ip);

    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setPrevScreen() {
        ScreenBase prev = screenStack.pop();
        setInputProcessor(prev.getScreenInputProcessor());
        super.setScreen(prev);
    }

    public void setScreen(ScreenBase newScreen) {
        screenStack.push((ScreenBase) screen);
        setInputProcessor(newScreen.getScreenInputProcessor());
        super.setScreen(newScreen);
    }

    public Viewport getUIViewport() {
        return viewport;
    }

    public RenderManager getRenderManager() {
        return renderManager;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void initApp() {
        if (!isInitialized) {
            setScreen(new MainMenuScreen(this));
            isInitialized = true;
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        UIUtils.updateScreenActor(width, height);
        super.resize(width, height);
    }
}
