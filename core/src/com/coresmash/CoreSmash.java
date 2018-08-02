package com.coresmash;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.coresmash.managers.RenderManager;
import com.coresmash.screens.LoadingScreen;
import com.coresmash.ui.UIUtils;

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

    private Viewport viewport;
    private RenderManager renderManager;
    private AssetManager assetManager;
    private UserAccount userAccount;
    private Skin skin;
    private AdManager adManager;

    private Stack<Screen> screenStack;

    public CoreSmash() {
    }

    public CoreSmash(AdManager adManager) {
        this.adManager = adManager;
    }

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

        Gdx.input.setCatchBackKey(true);

        assetManager = new AssetManager();
        renderManager = new RenderManager(assetManager);

        skin = new Skin();
        userAccount = new UserAccount();

        super.setScreen(new LoadingScreen(this));
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

    public AdManager getAdManager() {
        return adManager;
    }

    public Skin getSkin() {
        return skin;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setPrevScreen() {
        screenStack.pop();
        super.setScreen(screenStack.peek());
    }

    public void setScreen(Screen screen) {
        screenStack.push(screen);
        super.setScreen(screen);
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        UIUtils.updateScreenActor(width, height);
        super.resize(width, height);
    }
}
