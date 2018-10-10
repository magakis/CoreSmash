package com.archapp.coresmash.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Align;

public class Annotator {
    private AnnotationDialog annotationDialog;
    private Annotation annotation;
    private Vector2 target;

    public Annotator(Skin skin) {
        annotation = new Annotation();
        annotationDialog = new AnnotationDialog(skin);
        target = new Vector2();
    }

    public Annotation newAnnotation(Actor actor) {
        assert actor != null;
        annotationDialog.text.setText("");
        annotation.reset();

        annotation.actor = actor;
        return annotation;
    }

    public class Annotation {
        private Actor actor;
        private int align;
        private float pad;
        private Callback callback;

        public Annotation setText(String text) {
            annotationDialog.text.setText(text);
            return this;
        }

        public Annotation setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public Annotation pad(float pad) {
            this.pad = pad;
            return this;
        }

        public Annotation align(int align) {
            this.align = align;
            return this;
        }

        public void show() {
            annotationDialog.annotation = this;
            final float h = actor.getHeight(), w = actor.getWidth();
            Vector2 target = actor.localToStageCoordinates(Annotator.this.target.setZero());
            annotationDialog.show(actor.getStage(), Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.5f, Interpolation.fade)));
            if ((align & Align.left) != 0) {
                if ((align & Align.top) != 0) {
                    annotationDialog.setPosition(target.x + w / 2 - (annotationDialog.getPadLeft() + annotationDialog.pointer.getWidth() / 2), target.y - pad, Align.topLeft);
                    annotationDialog.pointerWrapper.top().left();
                    annotationDialog.pointerWrapper.getActor().setRotation(0);
                } else {
                    annotationDialog.setPosition(target.x + w / 2 - (annotationDialog.getPadLeft() + annotationDialog.pointer.getWidth() / 2), target.y - pad - h, Align.bottomLeft);
                    annotationDialog.pointerWrapper.bottom().left();
                    annotationDialog.pointerWrapper.getActor().setRotation(180);
                }
            } else {
                if ((align & Align.top) != 0) {
                    annotationDialog.setPosition(target.x + w / 2 + (annotationDialog.getPadRight() + annotationDialog.pointer.getWidth() / 2), target.y - pad, Align.topRight);
                    annotationDialog.pointerWrapper.top().right();
                    annotationDialog.pointerWrapper.getActor().setRotation(0);
                } else {
                    annotationDialog.setPosition(target.x + w / 2 + (annotationDialog.getPadRight() + annotationDialog.pointer.getWidth() / 2), target.y - pad + h, Align.bottomRight);
                    annotationDialog.pointerWrapper.bottom().right();
                    annotationDialog.pointerWrapper.getActor().setRotation(180);
                }
            }

            annotationDialog.pointerWrapper.invalidate();
        }

        private void reset() {
            actor = null;
            callback = null;
            align = Align.topLeft;
            pad = 0;
        }
    }

    private class AnnotationDialog extends Dialog {
        private Annotation annotation;
        private Label text;
        private Image pointer;
        private Container<Container<Image>> pointerWrapper;

        private AnnotationDialog(Skin skin) {
            super("", skin, "TipDialog");

            text = new Label("", skin, "h5");
            pointer = new Image(skin, "TipPointer");
            pointerWrapper = new Container<>(new Container<>(pointer));
            float scale = .3f * Gdx.graphics.getDensity();
            pointerWrapper.getActor()
                    .height(pointer.getPrefHeight() * scale)
                    .width(pointer.getPrefWidth() * scale)
                    .padTop(-pointerWrapper.getPrefHeight());
            pointerWrapper.top();
            pointerWrapper.getActor().setTransform(true);

            Container<Label> textWrapper = new Container<>(text);
            textWrapper.pad(Value.percentHeight(.1f));

            Table content = getContentTable();
            content.add(new Stack(textWrapper, pointerWrapper)).grow();

            addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    hide();
                    return false;
                }
            });

            clearChildren();
            add(content);
            setMovable(false);
            setResizable(false);
            setClip(false);
        }

        @Override
        public void hide(Action action) {
            super.hide(action);
            if (annotation.callback != null) annotation.callback.call();
        }
    }

    public interface Callback {
        void call();
    }
}
