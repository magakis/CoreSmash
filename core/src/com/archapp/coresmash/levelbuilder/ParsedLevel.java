package com.archapp.coresmash.levelbuilder;

import java.util.ArrayList;
import java.util.List;

public class ParsedLevel {
    LevelSettings levelSettings;
    List<MapSettings> mapSettings;
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

    public MapSettings getMapSettings(int layer) {
        if (layer >= mapSettings.size()) throw new RuntimeException();
        return mapSettings.get(layer);
    }

    public List<ParsedTile> getTiles(int layer) {
        return mapTiles.get(layer);
    }

    void reset() {
        levelSettings.reset();

        for (MapSettings map : mapSettings) {
            map.reset();
        }

        for (List tileList : mapTiles) {
            tileList.clear();
        }
    }
}
