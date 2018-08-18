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
public class DestroyRadiusEffect implements TilemapManager.TilemapEffect, Pool.Poolable {
    private static Pool<DestroyRadiusEffect> pool;

    static {
        pool = new Pool<DestroyRadiusEffect>() {
            @Override
            protected DestroyRadiusEffect newObject() {
                return new DestroyRadiusEffect();
            }
        };
    }

    public static DestroyRadiusEffect newInstance(int radius, int layer, int coordX, int coordY) {
        DestroyRadiusEffect result = pool.obtain();
        result.setup(radius, layer, coordX, coordY);
        return result;
    }

    private int radius, layer, originX, originY;
    private boolean isNew;
    private List<TilemapTile> destroyList;

    private DestroyRadiusEffect() {
        destroyList = new ArrayList<>();
    }

    @Override
    public void apply(TilemapManager manager) {
        if (!isNew) throw new RuntimeException("Effects can only be used once!");

        if (radius == 0) {
            manager.destroyTiles(layer, originX, originY);
        } else {
            int minX = originX - radius;
            int minY = originY - radius;
            int maxX = originX + radius + 1;
            int maxY = originY + radius + 1;

            for (int y = minY; y < maxY; ++y) {
                for (int x = minX; x < maxX; ++x) {
                    if (Tilemap.getTileDistance(x, y, originX, originY) <= radius) {
                        TilemapTile tile = manager.getTilemapTile(layer, x, y);
                        if (tile != null) {
                            destroyList.add(tile);
                        }
                    }
                }
            }
        }

        manager.destroyTiles(destroyList);
        isNew = false;
        pool.free(this);
    }

    private void setup(int radius, int layer, int coordX, int coordY) {
        if (radius < 0) throw new RuntimeException("Radius can't be negative: " + radius);

        this.radius = radius;
        this.originX = coordX;
        this.originY = coordY;
        this.layer = layer;
        isNew = true;
    }

    @Override
    public void reset() {
        destroyList.clear();
        radius = layer = originX = originY = 0;
        isNew = false;
    }
}