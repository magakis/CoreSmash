package com.breakthecore.screens;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;

public abstract class ScreenBase extends ScreenAdapter {
    protected InputMultiplexer screenInputMultiplexer;

    public ScreenBase() {
        screenInputMultiplexer = new InputMultiplexer();
    }

    public InputProcessor getScreenInputProcessor() {
        return screenInputMultiplexer;
    }
}
