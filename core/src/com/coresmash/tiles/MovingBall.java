package com.coresmash.tiles;

/**
 * Created by Michail on 19/3/2018.
 */

public class MovingBall extends com.coresmash.tiles.TileContainer {
    private float m_speed;
    private float scale;
    private boolean flag;

    public MovingBall() {

    }

    /*
     * Throws exception if cast can't be done, CAN return NULL if empty
     */
    public com.coresmash.tiles.Launchable getLaunchable() {
        return (com.coresmash.tiles.Launchable) getTile();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setSpeed(float speed) {
        m_speed = speed * 100;
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
        moveBy(0, m_speed * delta);
    }

    public com.coresmash.tiles.Tile extractTile() {
        com.coresmash.tiles.Tile res = getTile();
        setTile(null);
        return res;
    }
}
