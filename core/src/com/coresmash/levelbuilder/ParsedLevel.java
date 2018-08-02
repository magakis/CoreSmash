package com.coresmash.levelbuilder;

import java.util.ArrayList;
import java.util.List;

public class ParsedLevel {
    LevelSettings levelSettings;
    List<com.coresmash.levelbuilder.MapSettings> mapSettings;
    List<List<ParsedTile>> mapTiles;

    ParsedLevel() {
        levelSettings = new LevelSettings();
        mapSettings = new ArrayList<>();
        mapTiles = new ArrayList<>();
    }

    public int getMapCount() {
        return mapSettings.size();
    }

    public LevelSettings getLevelSettings() {
        return levelSettings;
    }

    public com.coresmash.levelbuilder.MapSettings getMapSettings(int layer) {
        if (layer >= mapSettings.size()) throw new RuntimeException();
        return mapSettings.get(layer);
    }

    public List<ParsedTile> getTiles(int layer) {
        return mapTiles.get(layer);
    }

    void reset() {
        levelSettings.ballSpeed = 0;
        levelSettings.launcherCooldown = 0;
        levelSettings.ballSpeed = 5;
        levelSettings.launcherSize = 1;
        levelSettings.launcherCooldown = 0f;

        for (com.coresmash.levelbuilder.MapSettings map : mapSettings) {
            map.reset();
        }

        for (List tileList : mapTiles) {
            tileList.clear();
        }
    }
}
