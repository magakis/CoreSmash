package com.breakthecore.ui;

import com.badlogic.gdx.scenes.scene2d.Group;

public class UIComponent {
    private Group root;

    public Group getRoot() {
        return root;
    }
    protected void setRoot(Group grp) { root = grp; }
}
