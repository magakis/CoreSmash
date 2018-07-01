package com.breakthecore.levelbuilder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.breakthecore.managers.RenderManager;
import com.breakthecore.tilemap.Map;
import com.breakthecore.tiles.TileFactory;

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
        mapSettings.get(0).chained = true;
    }

    public void setTileID(int id) {
        tileID = id;
    }

    public void moveOffsetBy(float x, float y) {
        setOffset(getOffsetX() + x, getOffsetY() + y);
    }

    public void rotateLayer(float degrees) {
        map.forceRotateLayer(layer, degrees);
    }

    /**
     * Draws at the given screen coordinates
     */
    public void paintAt(float x, float y) {
        Vector3 worldPos = screenToWorld.convert(x, y);

        if (layer != 0 && map.getTileCountFrom(layer) == 0) {
            map.setMapPosition(layer, (int) worldPos.x, (int) worldPos.y);
            mapSettings.get(layer).offset.set(map.getLayerOffsetX(layer), map.getLayerOffsetY(layer));
        }

        Vector3 relative = map.getWorldToLayerCoords(layer, worldPos);

        if (map.isTileEmpty(layer, (int) relative.x, (int) relative.y)) {
            map.placeTile(layer, (int) relative.x, (int) relative.y, TileFactory.getTileFromID(tileID));
        }
    }

    public float getPositionX(int layer) {
        return map.getLayerPositionX(layer);
    }

    public int getTotalTileCount() {
        return map.totalBallCount();
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

    public void setChained(boolean chained) {
        mapSettings.get(layer).chained = chained;
    }

    public void setCCWRotation(boolean ccw) {
        mapSettings.get(layer).rotateCCW = ccw;
    }

    public void setOriginMinSpeed(int min) {
        mapSettings.get(layer).minMapSpeed = min;
    }

    public void setOriginMaxSpeed(int max) {
        mapSettings.get(layer).maxMapSpeed = max;
    }

    public int getOriginMinSpeed() {
        return mapSettings.get(layer).minMapSpeed;
    }

    public int getOriginMaxSpeed() {
        return mapSettings.get(layer).maxMapSpeed;
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

    public void setOrigin(float x, float y) {
        mapSettings.get(layer).origin.set(x, y);
        map.setOrigin(layer, x, y);
        map.validate(layer);
    }

    public void setOffset(float x, float y) {
        mapSettings.get(layer).offset.set(x, y);
        map.setOffset(layer, x, y);
        map.validate(layer);
    }

    public float getOriginX() {
        return map.getLayerOriginX(layer);
    }

    public float getOriginY() {
        return map.getLayerOriginY(layer);
    }

    public float getOffsetX() {
        return map.getLayerOffsetX(layer);
    }

    public float getOffsetY() {
        return map.getLayerOffsetY(layer);
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

    public boolean isChained() {
        return mapSettings.get(layer).isChained();
    }

    public boolean isCCWRotationEnabled() {
        return mapSettings.get(layer).rotateCCW;
    }

    public float getDefPositionX() {
        return map.getDefPositionX(); //should return per layer
    }

    public float getDefPositionY() {
        return map.getDefPositionY(); //should return per layer
    }

    public void draw(RenderManager renderManager) {
        renderManager.spriteBatchBegin(camera.combined);
        if (layerIndicatorEnabled) {
            int maxTilemaps = map.layerCount();
            renderManager.setColorTint(Color.GRAY);
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

        if (map.layerCount() > 0) {
            ShapeRenderer shapeRenderer = renderManager.shapeRendererStart(camera.combined, ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.GOLDENROD);
            shapeRenderer.circle(map.getLayerOriginX(layer) + getDefPositionX(), map.getLayerOriginY(layer) + getDefPositionY(), 20);

            shapeRenderer.line(getPositionX(layer) - 20, getPositionY(layer), getPositionX(layer) + 20, getPositionY(layer));
            shapeRenderer.line(getPositionX(layer), getPositionY(layer) - 20, getPositionX(layer), getPositionY(layer) + 20);
            shapeRenderer.circle(map.getLayerOriginX(layer) + getDefPositionX(), map.getLayerOriginY(layer) + getDefPositionY(), 20);
            renderManager.shapeRendererEnd();
        }
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

        for (int parsedLayer = 0; parsedLayer < parsedLevel.getMapCount(); ++parsedLayer) {
            map.newLayer();
            map.setOrigin(parsedLayer, mapSettings.get(parsedLayer).getOrigin());
            map.setOffset(parsedLayer, mapSettings.get(parsedLayer).getOffset());

            for (ParsedTile tile : parsedLevel.mapTiles.get(parsedLayer)) {
                map.placeTile(parsedLayer, tile.x, tile.y, TileFactory.getTileFromID(tile.tileID));
            }
        }

        if (map.layerCount() == 0) {
            map.newLayer();
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
