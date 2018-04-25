package com.breakthecore.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.breakthecore.BreakTheCoreGame;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.managers.TilemapManager;

public class LevelBuilderScreen extends ScreenBase {
    private RenderManager renderManager;
    private Camera camera;
    private TilemapManager tilemapManager;

    public LevelBuilderScreen(BreakTheCoreGame game) {
        super(game);
        renderManager = game.getRenderManager();
        camera = game.getWorldViewport().getCamera();
        tilemapManager = new TilemapManager();

        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(new GestureDetector(new LevelBuilderGestureListner()));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }


    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                gameInstance.setPrevScreen();
            }
            return false;
        }
    }
    private class LevelBuilderGestureListner implements GestureDetector.GestureListener {

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return false;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }

        @Override
        public void pinchStop() {

        }
    }
}
