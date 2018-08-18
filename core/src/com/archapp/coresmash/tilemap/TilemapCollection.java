package com.archapp.coresmash.tilemap;

import com.archapp.coresmash.managers.RenderManager;

public interface TilemapCollection {
    int layerCount();

    boolean layerExists(int layer);

    float getLayerPositionX(int layer);

    float getLayerPositionY(int layer);

    void draw(RenderManager renderManager);

    TilemapTile getTilemapTile(int layer, int x, int y);
}

