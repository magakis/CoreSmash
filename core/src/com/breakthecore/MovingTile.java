package com.breakthecore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Michail on 19/3/2018.
 */

public class MovingTile {
    private Vector2 worldPos;
    private float speed = 400;
    private float scale;
    private int color;
    private boolean flag;

    public MovingTile(float x, float y, int color) {
        this(new Vector2(x, y), color);
    }

    public MovingTile(Vector2 pos, int color){
        scale = 3/4f;
        worldPos = new Vector2(pos);
        this.color = color;
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

    public int getColor() {return color;}

    public MovingTile setColor(int col) {
        color = col;
        return this;
    }

    public void moveBy(float x, float y) {
            worldPos.add(x, y);
    }

    public void update(float delta) {
            moveBy(0, speed * delta);
    }

    public MovingTile setPos(float x, float y) {
        worldPos.set(x, y);
        return this;
    }

    public Vector2 getPositionInWorld() {return worldPos;}
}
