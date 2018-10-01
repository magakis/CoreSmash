package com.archapp.coresmash.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public final class UIEffects {
    private UIEffects() {
    }

    public static Action getEffect(Effect effect) {
        return getEffect(effect, 1);
    }

    public static Action getEffect(Effect effect, float intensity) {
        switch (effect) {
            case bubbly_x:
                return getBubblyEffectSmoothX(intensity);
            case bubbly_y:
                return getBubblyEffectSmoothY(intensity);
        }
        throw new RuntimeException("Unimplemented Action");
    }

    private static Action getBubblyEffect() {
        return Actions.sequence(
                Actions.scaleBy(.05f, -.2f, .3f),
                Actions.scaleBy(-.2f, .25f, .2f),
                Actions.scaleBy(.15f, -.05f, .2f)
        );
    }

    private static Action getBubblyEffectSmoothY(float intensity) {
        return Actions.sequence(
                Actions.scaleBy(.025f * intensity, -.05f * intensity, .25f),
                Actions.scaleBy(-.05f * intensity, .075f * intensity, .35f),
                Actions.scaleBy(.025f * intensity, -.025f * intensity, .25f)
        );
    }

    private static Action getBubblyEffectSmoothX(float intensity) {
        return Actions.sequence(
                Actions.scaleBy(-.05f * intensity, .025f * intensity, .25f),
                Actions.scaleBy(.075f * intensity, -.05f * intensity, .35f),
                Actions.scaleBy(-.025f * intensity, .025f * intensity, .25f)
        );
    }

    public enum Effect {
        bubbly_x,
        bubbly_y
    }
}