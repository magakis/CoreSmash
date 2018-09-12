package com.archapp.coresmash.levels;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Area1 extends CampaignArea {


    public Area1(Skin skin, ChangeListener onLevelChange) {
        super(skin.getDrawable("CampaignBackground"));

        addLevel(new LevelButton(1, 212, 252, skin, onLevelChange));
        addLevel(new LevelButton(2, 490, 252, skin, onLevelChange));
        addLevel(new LevelButton(3, 761, 307, skin, onLevelChange));
        addLevel(new LevelButton(4, 870, 552, skin, onLevelChange));
        addLevel(new LevelButton(5, 798, 813, skin, onLevelChange));
        addLevel(new LevelButton(6, 544, 920, skin, onLevelChange));
        addLevel(new LevelButton(7, 315, 1076, skin, onLevelChange));
        addLevel(new LevelButton(8, 211, 1323, skin, onLevelChange));
        addLevel(new LevelButton(9, 249, 1594, skin, onLevelChange));
        addLevel(new LevelButton(10, 448, 1783, skin, onLevelChange));
        addLevel(new LevelButton(11, 688, 1923, skin, onLevelChange));
        addLevel(new LevelButton(12, 851, 2143, skin, onLevelChange));
        addLevel(new LevelButton(13, 851, 2417, skin, onLevelChange));
        addLevel(new LevelButton(14, 681, 2632, skin, onLevelChange));
        addLevel(new LevelButton(15, 442, 2771, skin, onLevelChange));
        addLevel(new LevelButton(16, 203, 2910, skin, onLevelChange));
        addLevel(new LevelButton(17, 118, 3167, skin, onLevelChange));
        addLevel(new LevelButton(18, 210, 3427, skin, onLevelChange));
    }

}
