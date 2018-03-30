package com.breakthecore.screens;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;

public abstract class ScreenBase extends ScreenAdapter {
    public abstract InputProcessor getScreenInputProcessor();
}
