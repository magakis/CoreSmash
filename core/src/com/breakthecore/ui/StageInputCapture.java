package com.breakthecore.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;

public class StageInputCapture {
    Container<?> screenFiller;
    Actor prevKeyboardFocus;

    public StageInputCapture() {
        screenFiller = new Container<>();
        screenFiller.setFillParent(true);
        screenFiller.setTouchable(Touchable.enabled);
    }

    public void setInputListener(EventListener listener) {
        screenFiller.clearListeners();
        screenFiller.addListener(listener);
    }

    public void capture(Stage stage) {
        stage.addActor(screenFiller);
        prevKeyboardFocus = stage.getKeyboardFocus();
        stage.setKeyboardFocus(screenFiller);
    }

    public void stop() {
        Stage stage = screenFiller.getStage();
        if (stage == null) return;

        stage.setKeyboardFocus(prevKeyboardFocus);
        prevKeyboardFocus = null;
        screenFiller.remove();
    }
}
