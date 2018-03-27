package com.breakthecore.debug;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Michail on 22/3/2018.
 */

public class DebugRenderer {
    private static DebugRenderer instance;
    private static ShapeRenderer shpRenderer;

    public static DebugRenderer get() {
        if (instance == null) {
            instance = new DebugRenderer();
        }
        return instance;
    }

    private DebugRenderer() {
        shpRenderer = new ShapeRenderer();
    }

    public ShapeRenderer getShapeRenderer() {
        return shpRenderer;
    }
}
