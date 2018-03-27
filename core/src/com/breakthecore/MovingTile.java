package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Michail on 19/3/2018.
 */

public class MovingTile extends Tile {
    private float speed = 1300;
    private float scale;
    private boolean flag;

    public MovingTile(float x, float y, int color) {
        this(new Vector2(x, y), color);
    }

    public MovingTile(Vector2 pos, int color){
        super(color);
        scale = 3/5f;
        m_positionInWorld.set(pos);
    }

    public float getSpeed() {
        return speed;
    }

    public float getScale() {
        return scale;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean x) {
        flag = x;
    }

    public void dispose() {
        flag = true;
    }

    public void moveBy(float x, float y) {
            m_positionInWorld.add(x, y);
    }

    public void update(float delta) {
            moveBy(0, speed * delta);
    }

}
