package com.coresmash.tilemap;

import com.coresmash.managers.RenderManager;

public interface TilemapCollection {
    int layerCount();

    boolean layerExists(int layer);

    float getLayerPositionX(int layer);

    float getLayerPositionY(int layer);

    void draw(RenderManager renderManager);

    com.coresmash.tilemap.TilemapTile getTilemapTile(int layer, int x, int y);
}

