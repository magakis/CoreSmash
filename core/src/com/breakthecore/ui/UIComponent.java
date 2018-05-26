package com.breakthecore.ui;

import com.badlogic.gdx.scenes.scene2d.Group;

public interface UIComponent {
    /** Performs any actions required prior to displaying the component and returns the <u>root</u> Group. */
    Group show();
}
