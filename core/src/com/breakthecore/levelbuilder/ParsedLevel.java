package com.breakthecore.levelbuilder;

import com.breakthecore.tilemap.TilemapManager;

import java.util.ArrayList;
import java.util.List;

public class ParsedLevel {
    LevelSettings levelSettings;
    MapSettings mapSettings[];
    List<ParsedTile>[] mapTiles;

    ParsedLevel() {
        levelSettings = new LevelSettings();
        mapSettings = new MapSettings[TilemapManager.MAX_TILEMAP_COUNT];
        mapTiles = new List[TilemapManager.MAX_TILEMAP_COUNT];

        for (int i = 0; i < mapSettings.length; ++i) {
            mapSettings[i] = new MapSettings();
        }
        for (int i = 0; i < mapTiles.length; ++i) {
            mapTiles[i] = new ArrayList<>();
        }
    }

    public LevelSettings getLevelSettings() {return levelSettings;}
    public MapSettings getMapSettings(int layer) {return mapSettings[layer];}
    public List<ParsedTile> getTiles(int layer) {return mapTiles[layer];}

    void reset() {
        levelSettings.ballSpeed = 0;
        levelSettings.launcherCooldown = 0;
        levelSettings.ballSpeed = 5;
        levelSettings.launcherSize = 1;
        levelSettings.launcherCooldown = 0f;

        for (MapSettings map : mapSettings) {
            map.colorCount = 1;
            map.minSpeed = 0;
            map.maxSpeed = 0;
            map.rotateCCW = false;
        }

        for (List tileList : mapTiles) {
            tileList.clear();
        }
    }
}
