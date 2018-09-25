package com.archapp.coresmash.animation;

import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.managers.RenderManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnimationManager {
    private final int BALLSIZE = WorldSettings.getTileSize();
    private final int HALFSIZE = BALLSIZE / 2;
    private final Pool<BallAnimation> ballAnimationPool = new Pool<BallAnimation>() {
        @Override
        protected BallAnimation newObject() {
            return new BallAnimation();
        }
    };
    private final List<BallAnimation> activeList;

    public AnimationManager() {
        activeList = new ArrayList<>();
    }

    public void update(float delta) {
        Iterator<BallAnimation> iter = activeList.iterator();
        while (iter.hasNext()) {
            BallAnimation anim = iter.next();
            anim.elapsedTime += delta;
            if (anim.elapsedTime >= BallAnimation.DURATION) {
                anim.tmTile.dispose();
                iter.remove();
                ballAnimationPool.free(anim);
            }
        }
    }

    public void reset() {
        Iterator<BallAnimation> iter = activeList.iterator();
        while (iter.hasNext()) {
            BallAnimation anim = iter.next();
            ballAnimationPool.free(anim);
            iter.remove();
        }
    }

    public void put(TilemapTile tmTile) {
        activeList.add(ballAnimationPool.obtain().set(tmTile));
        assert tmTile.getTile() != null;
    }

    public void draw(RenderManager renderManager) {
        SpriteBatch batch = renderManager.getBatch();

        for (BallAnimation anim : activeList) {
            batch.draw(renderManager.getTextureFor(anim.getTileID()),
                    anim.getWorldPosX() - HALFSIZE, anim.getWorldPosY() - HALFSIZE,
                    HALFSIZE, HALFSIZE, BALLSIZE, BALLSIZE,
                    anim.getScale(), anim.getScale(), -(float) Math.toDegrees(anim.tmTile.getRotation())
            );
        }
    }

    private class BallAnimation implements Pool.Poolable {
        private static final float DURATION = .4f;
        private static final float INCREASE_DURATION = DURATION * .4f;
        private static final float DECREASE_DURATION = DURATION - INCREASE_DURATION;
        private static final float SCALE_INCREASE = .5f;
        private static final float MAX_SCALE = 1 + SCALE_INCREASE;

        private TilemapTile tmTile;
        private float elapsedTime;

        BallAnimation() {
        }

        public int getTileID() {
            return tmTile.getTileID();
        }

        public float getWorldPosX() {
            return tmTile.getPositionInWorld().x;
        }

        public float getWorldPosY() {
            return tmTile.getPositionInWorld().y;
        }

        public BallAnimation set(TilemapTile tile) {
            this.tmTile = tile;
            return this;
        }

        public float getScale() {
            if (elapsedTime < INCREASE_DURATION)
                return 1 + SCALE_INCREASE * Interpolation.linear.apply(elapsedTime / INCREASE_DURATION);

            return MAX_SCALE * Interpolation.slowFast.apply(1 - (elapsedTime - INCREASE_DURATION) / DECREASE_DURATION);
        }

        @Override
        public void reset() {
            tmTile = null;
            elapsedTime = 0;
        }
    }
}
