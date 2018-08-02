package com.coresmash.levels;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.coresmash.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;


abstract class CampaignArea {
    private List<LevelButton> levels;
    private WidgetGroup levelGroup;
    private Image background;
    private float refWidth;

    public CampaignArea(Drawable drawable) {
        levels = new ArrayList<>();

        levelGroup = new WidgetGroup();
        background = new Image(drawable, Scaling.fillX);
        background.setAlign(Align.bottom);
        refWidth = drawable.getMinWidth();
    }

    public Image getBackground() {
        return background;
    }

    public WidgetGroup getLevelsGroup(Stage stage) {
        float scale = stage.getWidth() / refWidth;

        for (LevelButton btn : levels) {
            btn.setPosition(btn.getX() * scale, btn.getY() * scale, Align.center);
        }
        return levelGroup;
    }

    public List<LevelButton> getLevels() {
        return levels;
    }

    protected void addLevel(LevelButton level) {
        levels.add(level);
        levelGroup.addActor(level);
    }

    protected static class LevelButton extends TextButton {
        public LevelButton(int level, int x, int y, Skin skin, ChangeListener listener) {
            super(String.valueOf(level), skin, "levelButton");
            addListener(listener);
            addListener(UIUtils.getButtonSoundListener());
            setSize(getStyle().font.getLineHeight() * 2f, getStyle().font.getLineHeight() * 2f);
            setPosition(x, y);
            setName(String.valueOf(level));
        }
    }

}
