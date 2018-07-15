package com.breakthecore.levels;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Area1 extends CampaignArea {


    public Area1(Skin skin, ChangeListener onLevelChange) {
        super(skin.getDrawable("CampaignBackground"));

        addLevel(new LevelButton(1, 293, 158, skin, onLevelChange));
        addLevel(new LevelButton(2, 529, 262, skin, onLevelChange));
        addLevel(new LevelButton(3, 824, 291, skin, onLevelChange));
        addLevel(new LevelButton(4, 903, 563, skin, onLevelChange));
        addLevel(new LevelButton(5, 669, 645, skin, onLevelChange));
        addLevel(new LevelButton(6, 384, 517, skin, onLevelChange));
        addLevel(new LevelButton(7, 186, 667, skin, onLevelChange));
        addLevel(new LevelButton(8, 275, 879, skin, onLevelChange));
        addLevel(new LevelButton(9, 522, 944, skin, onLevelChange));
        addLevel(new LevelButton(10, 813, 957, skin, onLevelChange));
        addLevel(new LevelButton(11, 783, 1219, skin, onLevelChange));
    }

}
