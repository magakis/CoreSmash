package com.archapp.coresmash.tiles;

import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;

public interface Destroyable {
    void onDestroy(TilemapTile self, TilemapManager tilemapManager);
}
