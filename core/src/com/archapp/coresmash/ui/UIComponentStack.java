package com.archapp.coresmash.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.Stack;

public class UIComponentStack implements UIComponent {
    private boolean isHidden;
    private Container<Group> root;
    private Stack<UIComponent> history;

    public UIComponentStack() {
        root = new Container<>();
        root.fill();
        root.setTouchable(Touchable.enabled);
        root.addCaptureListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                event.handle();
                return true;
            }
        });

        history = new Stack<>();
    }

    public void push(UIComponent comp) {
        history.push(comp);
        if (!isHidden) {
            root.setActor(comp.getRoot());
        }
    }

    public void pop() {
        history.pop();
        if (!isHidden) {
            root.setActor(history.peek().getRoot());
        }
    }


    public int size() {
        return history.size();
    }

    public void setRoot(UIComponent comp) {
        history.clear();
        push(comp);
    }

    public void restoreRoot() {
        UIComponent origin = history.firstElement();
        setRoot(origin);
    }

    public void setBackground(Drawable background) {
        root.setBackground(background);
    }

    public void setMaxHeight(Value height) {
        root.maxHeight(height);
    }

    @Override
    public Group getRoot() {
        return root;
    }
}