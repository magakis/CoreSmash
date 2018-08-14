package com.coresmash;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.coresmash.ui.UIComponent;

public class StreakUI implements UIComponent,Observer {
    Skin m_skin;
    Table root;
    Container<Label> m_label;

    public StreakUI(Skin skin) {
        m_skin = skin;
        root = new Table();
        Label label = new Label("", m_skin, "h2o");

        m_label = new Container<Label>(label);
        m_label.setTransform(true);
        m_label.setScale(.5f);


        root.center().top().padTop(100);
        root.add(m_label);

    }

    public void reset() {
        m_label.clearActions();
        m_label.getActor().setText("");
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        switch (type) {
            case NOTIFICATION_TYPE_SCORE_INCREMENTED:
                m_label.getActor().setText("+" + ob.toString());
                m_label.setOrigin(m_label.getPrefWidth() / 2, m_label.getPrefHeight() / 2);
                m_label.clearActions();
                m_label.addAction(Actions.sequence(
                        Actions.alpha(1),
                        Actions.parallel(
                                Actions.scaleTo(1.3f, 1.3f, 1, Interpolation.swingOut),
                                Actions.fadeOut(1.2f)),
                        Actions.scaleTo(.5f, .5f)));
                break;
        }
    }


    @Override
    public Group getRoot() {
        return root;
    }
}
