package com.breakthecore.levelbuilder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.breakthecore.Coords2D;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.tilemap.TilemapManager;

final public class LevelBuilder {
    private OrthographicCamera camera;
    private ScreenToWorld screenToWorld;
    private TilemapManager tilemapManager;

    private LevelSettings levelSettings;
    private MapSettings mapSettings[];

    private int layer;
    private int tileID;

    boolean layerIndicatorEnabled = true;

    public LevelBuilder(TilemapManager tilemapManager, OrthographicCamera cam) {
        camera = cam;
        this.tilemapManager = tilemapManager;
        screenToWorld = new ScreenToWorld();
        levelSettings = new LevelSettings();
        mapSettings = new MapSettings[tilemapManager.getMaxTilemapCount()];
        for(int i = 0; i < mapSettings.length; ++i) {
            mapSettings[i] = new MapSettings();
        }
    }

    public void setTileID(int id) {
        tileID = id;
    }

    public void rotateLayer(float degrees) {
        tilemapManager.forceRotateLayer(layer, degrees);
    }

    /**
     * Draws at the given screen coordinates
     */
    public void paintAt(float x, float y) {
        Coords2D relative = tilemapManager.getWorldToLayerCoords(layer, screenToWorld.convert(x, y));
        if (tilemapManager.isTileEmpty(layer, relative.x, relative.y)) {
            tilemapManager.placeTile(layer, relative.x, relative.y, tileID);
        }
    }

    public void eraseAt(float x, float y) {
        Coords2D relative = tilemapManager.getWorldToLayerCoords(layer, screenToWorld.convert(x, y));
        tilemapManager.removeTile(layer, relative.x, relative.y);
    }

    public void setLayerIndicator(boolean enabled) {
        layerIndicatorEnabled = enabled;
    }

    public void setCCWRotation (boolean ccw) {
        mapSettings[layer].rotateCCW = ccw;
    }

    public void setMinSpeed(int min) {
        mapSettings[layer].minSpeed = min;
    }

    public void setMaxSpeed(int max) {
        mapSettings[layer].maxSpeed = max;
    }

    public void setColorCount(int amount) {
        mapSettings[layer].colorCount = amount;
    }

    public void setLives(int lives) {
        levelSettings.lives = lives;
    }

    public void setMoves(int moves) {
        levelSettings.moves = moves;
    }

    public void setTime(int time) {
        levelSettings.time = time;
    }

    public void setLauncherSize(int size) {
        levelSettings.launcherSize = size;
    }

    public void setLauncherCooldown(float cooldown) {
        levelSettings.launcherCooldown = cooldown;
    }

    public void setBallSpeed(int speed) {
        levelSettings.ballSpeed = speed;
    }

    public int getBallSpeed() {
        return levelSettings.ballSpeed;
    }

    public int getLives() {
        return levelSettings.lives;
    }

    public int getMoves() {
        return levelSettings.moves;
    }

    public int getTime() {
        return levelSettings.time;
    }

    public int getLauncherSize() {
        return levelSettings.launcherSize;
    }

    public float getLauncherCooldown() {
        return levelSettings.launcherCooldown;
    }

    public int getMinSpeed() {
        return mapSettings[layer].minSpeed;
    }

    public int getMaxSpeed() {
        return mapSettings[layer].maxSpeed;
    }

    public int getColorCount() {
        return mapSettings[layer].colorCount;
    }

    public boolean isCCWRotationEnabled() {
        return mapSettings[layer].rotateCCW;
    }

    public Coords2D getLayerPosition() {
        return tilemapManager.getTilemapPosition(); //should return per layer
    }

    public void draw(RenderManager renderManager) {
        if (layerIndicatorEnabled) {
            int maxTilemaps = tilemapManager.getMaxTilemapCount();
            renderManager.setColorTint(Color.DARK_GRAY);
            for (int i = 0; i < maxTilemaps; ++i) {
                if (i == layer) continue;
                tilemapManager.draw(renderManager, i);
            }
            renderManager.setColorTint(Color.WHITE);
            tilemapManager.draw(renderManager, layer);
        } else {
            tilemapManager.draw(renderManager);
        }
    }

    public boolean saveAs(String name) {
        return LevelParser.saveAs(name, tilemapManager, levelSettings, mapSettings);
    }

    public boolean load(String name) {
        ParsedLevel parsedLevel = LevelParser.loadFrom(name);
        if (parsedLevel == null) return false;

        tilemapManager.reset();

        levelSettings.copy(parsedLevel.levelSettings);

        for (int i = 0; i < mapSettings.length; ++i) {
            mapSettings[i].copy(parsedLevel.mapSettings[i]);
        }

        for (int layer = 0; layer < parsedLevel.mapTiles.length; ++layer) {
            for (ParsedTile tile : parsedLevel.mapTiles[layer]) {
                tilemapManager.placeTile(layer, tile.x, tile.y, tile.tileID);
            }
        }

        return true;
    }

    public int upLayer() {
        if (layer != tilemapManager.getMaxTilemapCount() - 1) {
            ++layer;
        }
        return layer;
    }

    public int downLayer() {
        if (layer != 0) {
            --layer;
        }
        return layer;
    }

    public int getLayer() {
        return layer;
    }

    public int getCurrentTileID() {
        return tileID;
    }

    private class ScreenToWorld {
        private Vector3 screenCoords = new Vector3();

        public Vector3 convert(float x, float y) {
            screenCoords.set(x, y, 0);
            return camera.unproject(screenCoords);
        }
    }
}
