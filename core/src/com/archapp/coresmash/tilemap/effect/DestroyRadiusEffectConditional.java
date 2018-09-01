package com.archapp.coresmash.tilemap.effect;

import com.archapp.coresmash.tilemap.Tilemap;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.List;

/* NOTE: The only reason I don't use a single method apply() with all the required parameters including
 * the execution is to comply with the TilemapEffect interface so it can be stored as that
 */
public class DestroyRadiusEffectConditional implements TilemapManager.TilemapEffect, Pool.Poolable {
    private static Pool<DestroyRadiusEffectConditional> pool;

    static {
        pool = new Pool<DestroyRadiusEffectConditional>() {
            @Override
            protected DestroyRadiusEffectConditional newObject() {
                return new DestroyRadiusEffectConditional();
            }
        };
    }

    public static DestroyRadiusEffectConditional newInstance(int radius, int layer, int coordX, int coordY, Condition condition) {
        DestroyRadiusEffectConditional result = pool.obtain();
        result.setup(radius, layer, coordX, coordY, condition);
        return result;
    }

    private int radius, layer, originX, originY;
    private Condition condition;
    private boolean isNew;
    private List<TilemapTile> destroyList;

    private DestroyRadiusEffectConditional() {
        destroyList = new ArrayList<>();
    }

    @Override
    public void apply(TilemapManager tmm) {
        if (!isNew) throw new RuntimeException("Effects can only be used once!");

        if (radius == 0) {
            destroyList.add(tmm.getTilemapTile(layer, originX, originY));
        } else {
            int minX = originX - radius;
            int minY = originY - radius;
            int maxX = originX + radius + 1;
            int maxY = originY + radius + 1;

            for (int y = minY; y < maxY; ++y) {
                for (int x = minX; x < maxX; ++x) {
                    if (Tilemap.getTileDistance(x, y, originX, originY) <= radius) {
                        TilemapTile tile = tmm.getTilemapTile(layer, x, y);
                        if (tile != null && condition.isConditionMet(tile)) {
                            destroyList.add(tile);
                        }
                    }
                }
            }
        }

        tmm.destroyTiles(destroyList);
        isNew = false;
        pool.free(this);
    }

    private void setup(int radius, int layer, int coordX, int coordY, Condition condition) {
        if (radius < 0) throw new RuntimeException("Radius can't be negative: " + radius);

        this.radius = radius;
        this.originX = coordX;
        this.originY = coordY;
        this.layer = layer;
        this.condition = condition;
        isNew = true;
    }

    @Override
    public void reset() {
        destroyList.clear();
        radius = layer = originX = originY = 0;
        condition = null;
        isNew = false;
    }

    public interface Condition {
        boolean isConditionMet(TilemapTile tile);
    }
}