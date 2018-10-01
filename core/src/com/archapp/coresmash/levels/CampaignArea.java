package com.archapp.coresmash.levels;

import com.archapp.coresmash.UserAccount;
import com.archapp.coresmash.levelbuilder.LevelListParser;
import com.archapp.coresmash.levelbuilder.LevelListParser.RegisteredLevel;
import com.archapp.coresmash.levelbuilder.LevelParser;
import com.archapp.coresmash.ui.UIUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


abstract class CampaignArea {
    private List<LevelButton> levelButtonList;
    private WidgetGroup levelGroup;
    private Image background;
    private float refWidth;

    public CampaignArea(Drawable drawable) {
        levelButtonList = new ArrayList<>();

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
                for (LevelButton btn : levelButtonList) {
                    btn.setPosition(btn.originalX * scale + getX(), btn.originalY * scale + getY(), Align.center);
                }
            }
        };
        background.setAlign(Align.bottom);
    }

    /* levelButtonList MUST be sorted! */
    public void updateLevelStars(Array<RegisteredLevel> levels, UserAccount userAccount) {
        RegisteredLevel searchDummy = new RegisteredLevel(0, "");
        int unlockedLevels = userAccount.getUnlockedLevels();

        for (int i = 0; i < unlockedLevels; ++i) {
            LevelButton levelButton = levelButtonList.get(i);
            searchDummy.num = levelButton.level;

            int index = Arrays.binarySearch(levels.toArray(), searchDummy, LevelListParser.compLevel);
            if (index < 0) continue;

            int highscore = userAccount.getHighscoreForLevel(levelButton.level);
            LevelParser.TargetScore targetScore = LevelParser.getTargetScore(levels.get(index).name, LevelListParser.Source.INTERNAL);

            if (highscore < targetScore.three) {
                if (highscore < targetScore.two) {
                    if (highscore < targetScore.one) {
                        levelButton.setStars(0);
                    } else {
                        levelButton.setStars(1);
                    }
                } else {
                    levelButton.setStars(2);
                }
            } else {
                levelButton.setStars(3);
            }
        }
    }

    public Image getBackground() {
        return background;
    }

    public WidgetGroup getLevelsGroup() {
        return levelGroup;
    }

    public List<LevelButton> getLevelButtonList() {
        return levelButtonList;
    }

    void addLevel(LevelButton level) {
        levelButtonList.add(level);
        levelGroup.addActor(level);
    }

    static class LevelButton extends Container<Table> {
        private static Skin skin;

        private int originalX;
        private int originalY;
        private int level;
        private int starsUnlocked;
        private TextButton levelLabel;

        private HorizontalGroup starGroup;
        private Container<Image> star1, star2, star3;

        LevelButton(int level, int x, int y, Skin skin, ChangeListener listener) {
            addListener(listener);
            addListener(UIUtils.getButtonSoundListener());

            this.skin = skin;
            levelLabel = new TextButton("", skin, "ButtonLevel");

            originalX = x;
            originalY = y;
            setLevel(level);

            float buttonSize = levelLabel.getLabel().getPrefHeight() * 1.5f;
            levelLabel.getLabelCell().prefSize(buttonSize);
            setSize(buttonSize, buttonSize);
            levelLabel.setName(String.valueOf(level));

            star1 = new Container<>(new Image(skin, "GrayStar"));
            star2 = new Container<>(new Image(skin, "GrayStar"));
            star3 = new Container<>(new Image(skin, "GrayStar"));

            float starSize = buttonSize * .35f;
            star1.size(starSize);
            star2.size(starSize);
            star3.size(starSize);

            starGroup = new HorizontalGroup();
            starGroup.addActor(star1);
            starGroup.addActor(star2);
            starGroup.addActor(star3);

            Table root = new Table(skin);
            root.add(levelLabel)
                    .size(buttonSize)
                    .padBottom(-starSize * .6f)
                    .row();
            root.add(starGroup);

            setActor(root);
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
            levelLabel.setText(String.valueOf(level));
        }

        public void setStars(int starsAmount) {
            switch (starsAmount) {
                case 0:
                    star1.getActor().setDrawable(skin.getDrawable("GrayStar"));
                    star2.getActor().setDrawable(skin.getDrawable("GrayStar"));
                    star3.getActor().setDrawable(skin.getDrawable("GrayStar"));
                    break;
                case 1:
                    star1.getActor().setDrawable(skin.getDrawable("Star"));
                    star2.getActor().setDrawable(skin.getDrawable("GrayStar"));
                    star3.getActor().setDrawable(skin.getDrawable("GrayStar"));
                    break;
                case 2:
                    star1.getActor().setDrawable(skin.getDrawable("Star"));
                    star2.getActor().setDrawable(skin.getDrawable("Star"));
                    star3.getActor().setDrawable(skin.getDrawable("GrayStar"));
                    break;
                case 3:
                    star1.getActor().setDrawable(skin.getDrawable("Star"));
                    star2.getActor().setDrawable(skin.getDrawable("Star"));
                    star3.getActor().setDrawable(skin.getDrawable("Star"));
                    break;
                default:
                    throw new RuntimeException("Tried to put " + starsAmount + " stars in Level");
            }
            starsUnlocked = starsAmount;
        }

        public int getStarsUnlocked() {
            return starsUnlocked;
        }

        public void setDisabled(boolean disabled) {
            starGroup.setVisible(!disabled);
            levelLabel.setDisabled(disabled);
        }
    }

}
