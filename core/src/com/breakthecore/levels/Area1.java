package com.breakthecore.levels;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Area1 extends CampaignArea {


    public Area1(Skin skin, ChangeListener onLevelChange) {
        super(skin.getDrawable("CampaignBackground"));

        addLevel(new LevelButton(1, 189, 192, skin, onLevelChange));
        addLevel(new LevelButton(2, 448, 256, skin, onLevelChange));
        addLevel(new LevelButton(3, 691, 273, skin, onLevelChange));
        addLevel(new LevelButton(4, 852, 452, skin, onLevelChange));
        addLevel(new LevelButton(5, 862, 682, skin, onLevelChange));
        addLevel(new LevelButton(6, 707, 862, skin, onLevelChange));
        addLevel(new LevelButton(7, 473, 941, skin, onLevelChange));
        addLevel(new LevelButton(8, 297, 1101, skin, onLevelChange));
        addLevel(new LevelButton(9, 223, 1321, skin, onLevelChange));
        addLevel(new LevelButton(10, 253, 1584, skin, onLevelChange));
        addLevel(new LevelButton(11, 442, 1751, skin, onLevelChange));
        addLevel(new LevelButton(12, 642, 1905, skin, onLevelChange));
    }

}
