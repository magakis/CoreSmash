package com.breakthecore.tilemap;

import com.breakthecore.managers.RenderManager;

public interface TilemapCollection {
    int layerCount();

    boolean layerExists(int layer);

    float getLayerPositionX(int layer);

    float getLayerPositionY(int layer);

    void draw(RenderManager renderManager);

    TilemapTile getTilemapTile(int layer, int x, int y);
}

