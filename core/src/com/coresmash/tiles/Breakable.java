package com.coresmash.tiles;

import com.coresmash.tilemap.TilemapManager;
import com.coresmash.tilemap.TilemapTile;

public interface Breakable {
    void onDestroy(TilemapTile self, TilemapManager tilemapManager);
}
