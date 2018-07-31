package com.breakthecore.levels;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Area1 extends CampaignArea {


    public Area1(Skin skin, ChangeListener onLevelChange) {
        super(skin.getDrawable("CampaignBackground"));

        addLevel(new LevelButton(1, 184, 246, skin, onLevelChange));
        addLevel(new LevelButton(2, 468, 247, skin, onLevelChange));
        addLevel(new LevelButton(3, 746, 297, skin, onLevelChange));
        addLevel(new LevelButton(4, 870, 541, skin, onLevelChange));
        addLevel(new LevelButton(5, 801, 811, skin, onLevelChange));
        addLevel(new LevelButton(6, 540, 921, skin, onLevelChange));
        addLevel(new LevelButton(7, 308, 1076, skin, onLevelChange));
        addLevel(new LevelButton(8, 209, 1340, skin, onLevelChange));
        addLevel(new LevelButton(9, 261, 1616, skin, onLevelChange));
        addLevel(new LevelButton(10, 476, 1799, skin, onLevelChange));
        addLevel(new LevelButton(11, 718, 1949, skin, onLevelChange));
        addLevel(new LevelButton(12, 864, 2188, skin, onLevelChange));
        addLevel(new LevelButton(13, 829, 2466, skin, onLevelChange));
        addLevel(new LevelButton(14, 633, 2670, skin, onLevelChange));
        addLevel(new LevelButton(15, 379, 2799, skin, onLevelChange));
        addLevel(new LevelButton(16, 158, 2971, skin, onLevelChange));
        addLevel(new LevelButton(17, 131, 3250, skin, onLevelChange));
        addLevel(new LevelButton(18, 267, 3497, skin, onLevelChange));
    }

}
