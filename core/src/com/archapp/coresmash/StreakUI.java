package com.archapp.coresmash;

import com.archapp.coresmash.ui.UIComponent;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class StreakUI implements UIComponent,Observer {
    private Table root;
    private Container<Label> container;

    public StreakUI(Skin skin) {
        root = new Table();
        Label label = new Label("", skin, "h2o");

        container = new Container<>(label);
        container.setTransform(true);
        container.setScale(.5f);


        root.center().top().padTop(100);
        root.add(container);

    }

    public void reset() {
        container.clearActions();
        container.getActor().setText("");
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_SCORE_INCREMENTED:
                container.getActor().setText("+" + ob.toString());
                container.setOrigin(container.getPrefWidth() / 2, container.getPrefHeight() / 2);
                container.clearActions();
                container.addAction(Actions.sequence(
                        Actions.alpha(0),
                        Actions.scaleTo(.5f, .5f),
                        Actions.parallel(
                                Actions.sequence(Actions.fadeIn(.2f), Actions.delay(.5f), Actions.fadeOut(.3f)),
                                Actions.scaleTo(1.3f, 1.3f, 1, Interpolation.swingOut)
                        )
                ));
                break;
        }
    }


    @Override
    public Group getRoot() {
        return root;
    }
}
