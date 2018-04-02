package com.breakthecore.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.utils.Queue;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.MovingTile;
import com.breakthecore.tiles.TilemapTile;

import java.util.List;


/**
 * Created by Michail on 24/3/2018.
 */

public class RenderManager {
    private SpriteBatch m_batch;
    private AssetManager m_assetManager;
    private ShapeRenderer m_shapeRenderer;
    private float sideLength = 64;
    private float sideLengthHalf;

    private BitmapFont defaultFont;
    private Texture texture;
    private Color[] colorList;

    public RenderManager(AssetManager am) {
        m_batch = new SpriteBatch();
        m_shapeRenderer = new ShapeRenderer();
        m_assetManager = am;

        texture = m_assetManager.get("asteroid.png");

        defaultFont = new BitmapFont();
        sideLengthHalf = sideLength / 2.f;
        colorList = new Color[7];

        colorList[0] = new Color(1, 0, 0, 1);
        colorList[1] = new Color(1, 1, 0, 1);
        colorList[2] = new Color(1, 1, 1, 1);
        colorList[3] = new Color(0, 1, 0, 1);
        colorList[4] = new Color(1, 0, 1, 1);
        colorList[5] = new Color(0, 1, 1, 1);
        colorList[6] = new Color(0, 0, 1, 1);
    }

    public void start(Matrix4 combined) {
        m_batch.setProjectionMatrix(combined);
        m_batch.begin();
    }

    public void end() {
        m_batch.end();
    }

    public void renderCenterDot(Matrix4 combined) {
        m_shapeRenderer.setProjectionMatrix(combined);
        m_shapeRenderer.setColor(Color.BLACK);
        m_shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        m_shapeRenderer.circle(WorldSettings.getWorldWidth() / 2, WorldSettings.getWorldHeight() - WorldSettings.getWorldHeight() / 4, 15);
        m_shapeRenderer.end();

    }

    public void draw(MovingTile mt) {
        Vector2 atPos = mt.getPositionInWorld();
        float sideLen = sideLengthHalf * mt.getScale();
        m_batch.setColor(colorList[mt.getColor()]);
        m_batch.draw(texture, atPos.x - sideLen, atPos.y - sideLen, sideLen * 2, sideLen * 2);
    }

    public void draw(List<MovingTile> mt) {
        for (MovingTile tile : mt) {
            draw(tile);
        }
    }

    public void drawOnLeftSide(Queue<MovingTile> list) {
        int i = 0;
        for (MovingTile mt : list) {
            m_batch.setColor(colorList[mt.getColor()]);
            m_batch.draw(texture, 20, WorldSettings.getWorldHeight() / 2 - 10 * i, 80, 80);
            ++i;
        }
    }

    public void draw(Tilemap tm) {
        TilemapTile[][] map = tm.getTilemapTiles();
        Vector2 pos;
        float rotation = tm.getRotDegrees();
        int texWidth = texture.getWidth();
        int texHeight = texture.getHeight();

        for (TilemapTile[] arr : map) {
            for (TilemapTile tile : arr) {
                if (tile != null) {
                    pos = tile.getPositionInWorld();
                    m_batch.setColor(colorList[tile.getColor()]);
//                    m_batch.draw(texture, pos.x - sideLengthHalf, pos.y - sideLengthHalf, sideLength, sideLength);
                    m_batch.draw(texture, pos.x - sideLengthHalf, pos.y - sideLengthHalf, sideLengthHalf, sideLengthHalf, sideLength, sideLength,
                            1, 1, -rotation, 0, 0, texWidth, texHeight, false, false);
                }
            }
        }
    }

    public void DBdraw(CollisionManager cm, Tilemap tm) {
        TilemapTile[][] map = tm.getTilemapTiles();
        Vector2 pos;
        int sideLen = tm.getSideLength() / 2;

        m_shapeRenderer.setColor(Color.WHITE);
        m_shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float[] vetices = cm.getVerticesOnMiddleEdges(tm.getCosT(), tm.getSinT());
        for (TilemapTile[] arr : map) {
            for (TilemapTile tile : arr) {
                if (tile != null) {
                    pos = tile.getPositionInWorld();

                    for (int i = 0; i < 12; i += 2) {
                        m_shapeRenderer.circle(vetices[i] * sideLen + pos.x, vetices[i + 1] * sideLen + pos.y, 3);
                    }
                }
            }
        }
        m_shapeRenderer.end();
    }

    public void DBTileDistances(TilemapTile[][] map) {
        Vector2 pos;
        for (TilemapTile[] arr : map) {
            for (TilemapTile tile : arr) {
                if (tile != null) {
                    pos = tile.getPositionInWorld();
                    m_batch.setColor(Color.BLACK);
                    defaultFont.draw(m_batch, String.valueOf(tile.getDistanceFromCenter()), pos.x, pos.y);
                }
            }
        }
    }

    public void drawLauncher(Queue<MovingTile> launcher, Vector2 atPos) {
        float scale;
        float sideLength;
        MovingTile mt;

        for (int i = 0; i < launcher.size; ++i) {
            mt = launcher.get(i);
            scale = mt.getScale();
            sideLength = sideLengthHalf * scale;
            m_batch.setColor(colorList[mt.getColor()]);
            m_batch.draw(texture, atPos.x - sideLength, atPos.y - sideLength - i * this.sideLength, sideLength * 2, sideLength * 2);
        }
    }
}
