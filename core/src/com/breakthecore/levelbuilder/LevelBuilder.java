package com.breakthecore.levelbuilder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.breakthecore.Coords2D;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.tilemap.TilemapBuilder;
import com.breakthecore.tilemap.TilemapManager;

import java.util.ArrayList;
import java.util.List;

final public class LevelBuilder {
    private OrthographicCamera camera;
    private ScreenToWorld screenToWorld;
    private TilemapManager tilemapManager;

    private LevelSettings levelSettings;
    private List<MapSettings> mapSettings;

    private int layer;
    private int tileID;

    boolean layerIndicatorEnabled = true;

    LevelBuilder(TilemapManager tilemapManager, OrthographicCamera cam) {
        camera = cam;
        this.tilemapManager = tilemapManager;
        screenToWorld = new ScreenToWorld();
        levelSettings = new LevelSettings();
        mapSettings = new ArrayList<>();

        tilemapManager.newLayer();
        mapSettings.add(new MapSettings());
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
        Vector3 worldPos = screenToWorld.convert(x, y);

        if (tilemapManager.getTileCountFrom(layer) == 0) {
            tilemapManager.setMapPosition(layer, (int) worldPos.x, (int) worldPos.y);
            mapSettings.get(layer).offset.set(tilemapManager.getLayerOffsetX(layer), tilemapManager.getLayerOffsetY(layer));
        }

        Vector3 relative = tilemapManager.getWorldToLayerCoords(layer, worldPos);

        if (tilemapManager.isTileEmpty(layer, (int) relative.x, (int) relative.y)) {
            tilemapManager.placeTile(layer, (int) relative.x, (int) relative.y, tileID);
        }
    }

    public void eraseAt(float x, float y) {
        Vector3 relative = tilemapManager.getWorldToLayerCoords(layer, screenToWorld.convert(x, y));
        tilemapManager.removeTile(layer, (int) relative.x, (int) relative.y);
    }

    public void setLayerIndicator(boolean enabled) {
        layerIndicatorEnabled = enabled;
    }

    public void setCCWRotation(boolean ccw) {
        mapSettings.get(layer).rotateCCW = ccw;
    }

    public void setMinSpeed(int min) {
        mapSettings.get(layer).minSpeed = min;
    }

    public void setMaxSpeed(int max) {
        mapSettings.get(layer).maxSpeed = max;
    }

    public void setColorCount(int amount) {
        mapSettings.get(layer).colorCount = amount;
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
        return mapSettings.get(layer).minSpeed;
    }

    public int getMaxSpeed() {
        return mapSettings.get(layer).maxSpeed;
    }

    public int getColorCount() {
        return mapSettings.get(layer).colorCount;
    }

    public boolean isCCWRotationEnabled() {
        return mapSettings.get(layer).rotateCCW;
    }

    public Coords2D getLayerPosition() {
        return tilemapManager.getDefTilemapPosition(); //should return per layer
    }

    public void draw(RenderManager renderManager) {
        if (layerIndicatorEnabled) {
            int maxTilemaps = tilemapManager.getTilemapCount();
            renderManager.setColorTint(Color.DARK_GRAY);
            for (int i = 0; i < maxTilemaps; ++i) {
                if (i == layer) continue;
                tilemapManager.draw(renderManager, i);
            }
            if (tilemapManager.layerExists(layer)) {
                renderManager.setColorTint(Color.WHITE);
                tilemapManager.draw(renderManager, layer);
            }
        } else {
            tilemapManager.draw(renderManager);
        }
    }

    public boolean saveAs(String name) {
        // XXX(16/6/2018): Fix this array shit
        return LevelParser.saveAs(name, tilemapManager, levelSettings, mapSettings.toArray(new MapSettings[mapSettings.size()]));
    }

    public boolean load(String name) {
        ParsedLevel parsedLevel = LevelParser.loadFrom(name);
        if (parsedLevel == null) return false;

        tilemapManager.reset();

        levelSettings.copy(parsedLevel.levelSettings);

        for (int i = 0; i < parsedLevel.getMapCount(); ++i) {
            if (i < mapSettings.size()) {
                mapSettings.get(i).copy(parsedLevel.mapSettings.get(i));
            } else {
                MapSettings ms = new MapSettings();
                ms.copy(parsedLevel.mapSettings.get(i));
                mapSettings.add(ms);
            }
        }

        for (int layer = 0; layer < parsedLevel.getMapCount(); ++layer) {
            TilemapBuilder builder = tilemapManager.newLayer()
//                    .debug() //XXX:Is this totaly useless?
                    .setOrigin(mapSettings.get(layer).getOrigin())
                    .setOffset(mapSettings.get(layer).getOffset());

            for (ParsedTile tile : parsedLevel.mapTiles.get(layer)) {
                tilemapManager.placeTile(layer, tile.x, tile.y, tile.tileID);
            }

            builder.build();
        }


        return true;
    }

    public int upLayer() {
        ++layer;
        if (!tilemapManager.layerExists(layer)) {
            tilemapManager.newLayer();
            mapSettings.add(new MapSettings());
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
