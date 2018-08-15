package com.coresmash.tiles;

/**
 * Created by Michail on 19/3/2018.
 */

public class MovingBall extends TileContainer {
    private float speed;
    private float scale;
    private boolean flag;

    public MovingBall() {

    }

    /*
     * Throws exception if cast can't be done, CAN return NULL if empty
     */
    public Launchable getLaunchable() {
        return (Launchable) getTile();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setSpeed(float speed) {
        this.speed = speed * 100;
    }

    public float getScale() {
        return scale;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlagForDisposal(boolean x) {
        flag = x;
    }

    public void dispose() {
        flag = true;
    }

    public void moveBy(float x, float y) {
            positionInWorld.add(x, y);
    }

    public void update(float delta) {
        moveBy(0, speed * delta);
    }

    public Tile extractTile() {
        Tile res = getTile();
        setTile(null);
        return res;
    }
}
