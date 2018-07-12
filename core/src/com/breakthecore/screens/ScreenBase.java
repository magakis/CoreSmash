package com.breakthecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.breakthecore.CoreSmash;

public abstract class ScreenBase extends ScreenAdapter {
    protected CoreSmash gameInstance;
    protected InputMultiplexer screenInputMultiplexer;

    public ScreenBase(CoreSmash game) {
        gameInstance = game;
        screenInputMultiplexer = new InputMultiplexer();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(screenInputMultiplexer);
    }

    public InputProcessor getScreenInputProcessor() {
        return screenInputMultiplexer;
    }
}
