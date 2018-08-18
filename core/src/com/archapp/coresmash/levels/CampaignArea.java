package com.archapp.coresmash.levels;

import com.archapp.coresmash.ui.UIUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

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
        refWidth = drawable.getMinWidth();
        background = new Image(drawable, Scaling.fillX) {
            @Override
            protected void positionChanged() {
                super.positionChanged();
                recalcLevelButtonPos();
            }

            @Override
            protected void sizeChanged() {
                super.sizeChanged();
                recalcLevelButtonPos();
            }

            void recalcLevelButtonPos() {
                float scale = getWidth() / refWidth;
                for (LevelButton btn : levels) {
                    btn.setPosition(btn.originalX * scale + getX(), btn.originalY * scale + getY(), Align.center);
                }
            }
        };
        background.setAlign(Align.bottom);
    }

    public Image getBackground() {
        return background;
    }

    public WidgetGroup getLevelsGroup() {
//        float scale = background.getWidth() / refWidth;
//        for (LevelButton btn : levels) {
//            btn.setPosition(btn.getX() * scale, + btn.getY() * scale, Align.center);
//        }
        return levelGroup;
    }

    public List<LevelButton> getLevels() {
        return levels;
    }

    void addLevel(LevelButton level) {
        levels.add(level);
        levelGroup.addActor(level);
    }

    class LevelButton extends TextButton {
        int originalX;
        int originalY;

        LevelButton(int level, int x, int y, Skin skin, ChangeListener listener) {
            super(String.valueOf(level), skin, "levelButton");
            addListener(listener);
            addListener(UIUtils.getButtonSoundListener());
            setSize(getStyle().font.getLineHeight() * 2f, getStyle().font.getLineHeight() * 2f);
            setPosition(x, y);
            setName(String.valueOf(level));
            originalX = x;
            originalY = y;
        }

    }

}
