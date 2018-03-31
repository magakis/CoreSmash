package com.breakthecore;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.breakthecore.BreakTheCoreGame;

public class AndroidLauncher extends AndroidApplication {
    private BreakTheCoreGame m_game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        m_game = new BreakTheCoreGame();
        initialize(m_game, config);
    }

    //I have to move this in the core files at some point
    @Override
    public void onBackPressed() {
        if (m_game.isMainMenuActive()) {
            exit();
        } else {
            m_game.setMainMenuScreen();
        }
    }
}
