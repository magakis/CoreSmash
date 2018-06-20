package com.breakthecore.levelbuilder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.tilemap.Map;

import java.util.ArrayList;
import java.util.List;

final public class LevelBuilder {
    private OrthographicCamera camera;
    private ScreenToWorld screenToWorld;
    private Map map;

    private LevelSettings levelSettings;
    private List<MapSettings> mapSettings;

    private int layer;
    private int tileID;

    boolean layerIndicatorEnabled = true;

    LevelBuilder(OrthographicCamera cam) {
        camera = cam;
        screenToWorld = new ScreenToWorld();
        levelSettings = new LevelSettings();
        mapSettings = new ArrayList<>();
        map = new Map();

        map.newLayer();
        mapSettings.add(new MapSettings());
    }

    public void setTileID(int id) {
        tileID = id;
    }

    public void rotateLayer(float degrees) {
        map.forceRotateLayer(layer, degrees);
    }

    /**
     * Draws at the given screen coordinates
     */
    public void paintAt(float x, float y) {
        Vector3 worldPos = screenToWorld.convert(x, y);

        if (map.getTileCountFrom(layer) == 0) {
            map.setMapPosition(layer, (int) worldPos.x, (int) worldPos.y);
            mapSettings.get(layer).offset.set(map.getLayerOffsetX(layer), map.getLayerOffsetY(layer));
        }

        Vector3 relative = map.getWorldToLayerCoords(layer, worldPos);

        if (map.isTileEmpty(layer, (int) relative.x, (int) relative.y)) {
            map.placeTile(layer, (int) relative.x, (int) relative.y, tileID);
        }
    }

    public float getPositionX(int layer) {
        return map.getLayerPositionX(layer);
    }

    public int getTotalTileCount() {
        return map.getTotalTileCount();
    }

    public float getPositionY(int layer) {
        return map.getLayerPositionY(layer);
    }

    public void eraseAt(float x, float y) {
        Vector3 relative = map.getWorldToLayerCoords(layer, screenToWorld.convert(x, y));
        map.removeTile(layer, (int) relative.x, (int) relative.y);
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

    public float getDefPositionX() {
        return map.getDefPostionsX(); //should return per layer
    }

    public float getDefPositionY() {
        return map.getDefPostionsY(); //should return per layer
    }

    public void draw(RenderManager renderManager) {
        renderManager.spriteBatchBegin(camera.combined);
        if (layerIndicatorEnabled) {
            int maxTilemaps = map.getTilemapCount();
            renderManager.setColorTint(Color.DARK_GRAY);
            for (int i = 0; i < maxTilemaps; ++i) {
                if (i == layer) continue;
                map.draw(renderManager, i);
            }
            if (map.layerExists(layer)) {
                renderManager.setColorTint(Color.WHITE);
                map.draw(renderManager, layer);
            }
        } else {
            map.draw(renderManager);
        }
        renderManager.spriteBatchEnd();

        ShapeRenderer shapeRenderer = renderManager.shapeRendererStart(camera.combined, ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GOLDENROD);
        shapeRenderer.circle(getDefPositionX(), getDefPositionY(), 15);
        renderManager.shapeRendererEnd();
    }

    public boolean saveAs(String name) {
        // XXX(16/6/2018): Fix this array shit
        return LevelParser.saveAs(name, map, levelSettings, mapSettings.toArray(new MapSettings[mapSettings.size()]));
    }

    public boolean load(String name) {
        ParsedLevel parsedLevel = LevelParser.loadFrom(name);
        if (parsedLevel == null) return false;

        map.reset();

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
            map.newLayer();
            map.setOrigin(layer, mapSettings.get(layer).getOrigin());
            map.setOffset(layer, mapSettings.get(layer).getOffset());

            for (ParsedTile tile : parsedLevel.mapTiles.get(layer)) {
                map.placeTile(layer, tile.x, tile.y, tile.tileID);
            }
        }


        map.validate();
        return true;
    }

    public int upLayer() {
        ++layer;
        if (!map.layerExists(layer)) {
            map.newLayer();
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
