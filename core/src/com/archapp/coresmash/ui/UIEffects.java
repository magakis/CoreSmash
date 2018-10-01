package com.archapp.coresmash.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public final class UIEffects {
    private UIEffects() {
    }

    public static Action getEffect(Effect effect) {
        return getBubblyEffectSmooth();
    }

    private static Action getBubblyEffect() {
        return Actions.sequence(
                Actions.scaleBy(.05f, -.2f, .3f),
                Actions.scaleBy(-.2f, .25f, .2f),
                Actions.scaleBy(.15f, -.05f, .2f)
        );
    }

    private static Action getBubblyEffectSmooth() {
        return Actions.sequence(
                Actions.scaleBy(.025f, -.05f, .25f),
                Actions.scaleBy(-.05f, .075f, .35f),
                Actions.scaleBy(.025f, -.025f, .25f)
        );
    }

    public enum Effect {
        BUBBLY
    }
}