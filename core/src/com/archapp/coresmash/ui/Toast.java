package com.archapp.coresmash.ui;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public class Toast {
    private Table tbl;
    private Label msg;

    public Toast(ToastStyle style) {
        msg = new Label("", new Label.LabelStyle(style.font, Color.WHITE));
        msg.setWrap(true);
        msg.setAlignment(Align.center);
        tbl = new Table();
        tbl.background(style.background);
        tbl.pad(5 * Gdx.graphics.getDensity());
        tbl.setTouchable(Touchable.disabled);
        tbl.add(msg);
    }

    public Toast setText(String text) {
        msg.setText(text);
        return this;
    }

    public void show(Stage stage) {
        show(stage, null);
    }

    public void show(Stage stage, Action action) {
        msg.getGlyphLayout().setText(msg.getStyle().font, msg.getText());
        float textWidth = msg.getGlyphLayout().width;
        float textHeight = msg.getGlyphLayout().height * 2;
        float stageWidth = stage.getWidth() * .9f;

        float width = textWidth > stageWidth ? stageWidth : textWidth;
        float height = (float) (Math.ceil(textWidth / stageWidth)) * textHeight;

        tbl.clearActions();
        tbl.addAction(Actions.alpha(1));
        tbl.addAction(Actions.rotateTo(0));
        tbl.getCell(msg).size(width, height);
        tbl.setSize(width + tbl.getPadLeft() * 2, height + tbl.getPadBottom() * 2);
        tbl.setPosition(stage.getWidth() / 2, stage.getHeight() * .95f, Align.center);
        if (action != null) {
            tbl.addAction(action);
        }

        msg.layout(); // Required! (fixes bug where msg is not showing)

        stage.addActor(tbl);
    }

    public void hide() {
        hide(null);
    }

    public void hide(Action action) {
        tbl.clearActions();
        if (action != null) {
            tbl.addAction(Actions.sequence(action, Actions.run(new Runnable() {
                @Override
                public void run() {
                    tbl.remove();
                }
            })));
        } else {
            tbl.remove();
        }
    }

    public static class ToastStyle {
        public Drawable background;
        public BitmapFont font;
    }
}
