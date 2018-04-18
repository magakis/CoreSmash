package com.breakthecore.screens;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.breakthecore.BreakTheCoreGame;

public abstract class ScreenBase extends ScreenAdapter {
    protected BreakTheCoreGame gameInstance;
    protected InputMultiplexer screenInputMultiplexer;

    public ScreenBase(BreakTheCoreGame game) {
        gameInstance = game;
        screenInputMultiplexer = new InputMultiplexer();
    }

    public InputProcessor getScreenInputProcessor() {
        return screenInputMultiplexer;
    }
}
