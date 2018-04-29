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
import com.breakthecore.Coords2D;
import com.breakthecore.Tilemap;
import com.breakthecore.WorldSettings;
import com.breakthecore.tiles.MovingTile;
import com.breakthecore.tiles.Tile;
import com.breakthecore.tiles.TilemapTile;

import java.util.List;


/**
 * Created by Michail on 24/3/2018.
 */

public class RenderManager {
    private SpriteBatch m_batch;
    private AssetManager assetManager;
    private ShapeRenderer m_shapeRenderer;
    private float sideLength = WorldSettings.getTileSize();
    private float sideLengthHalf;

    private BitmapFont defaultFont;
    private Texture texture;
    private final Color[] colorList;

    public RenderManager(AssetManager am) {
        m_batch = new SpriteBatch();
        m_shapeRenderer = new ShapeRenderer();
        assetManager = am;

        texture = assetManager.get("asteroid.png");

        defaultFont = assetManager.get("comic_48.fnt",BitmapFont.class);
        sideLengthHalf = sideLength / 2.f;
        colorList = new Color[10];

        colorList[0] = new Color(0xff0000ff);
        colorList[1] = new Color(0x00ff00ff);
        colorList[2] = new Color(0x0000ffff);
        colorList[3] = new Color(0xffff00ff);
        colorList[4] = new Color(0x00ffffff);
        colorList[5] = new Color(0xff00ffff);
        colorList[6] = new Color(0xc0c0c0ff);
        colorList[7] = new Color(0x800000ff);
        colorList[8] = new Color(0x808000ff);
        colorList[9] = new Color(0x808080ff);

//        colorList[1] = new Color(0x3cb44bff);
//        colorList[2] = new Color(0xffe119ff);
//        colorList[3] = new Color(0x0082c8ff);
//        colorList[4] = new Color(0xf58231ff);
//        colorList[5] = new Color(0x911eb4ff);
//        colorList[6] = new Color(0x46f0f0ff);
    }

    public void start(Matrix4 combined) {
        m_batch.setProjectionMatrix(combined);
        m_batch.begin();
    }

    public Color[] getColorList() {
        return colorList;
    }

    public void end() {
        m_batch.end();
    }

    public void renderCenterDot(Coords2D pos, Matrix4 combined) {
        m_shapeRenderer.setProjectionMatrix(combined);
        m_shapeRenderer.setColor(Color.BLACK);
        m_shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        m_shapeRenderer.circle(pos.x, pos.y, 15);
        m_shapeRenderer.end();
    }

    public void draw(MovingTile mt) {
        Vector2 atPos = mt.getPositionInWorld();
        Tile tile = mt.getTile();
        float sideLen = sideLengthHalf * mt.getScale();
        m_batch.setColor(colorList[mt.getColor()]);

        switch (tile.getType()) {
            case REGULAR:
                m_batch.draw(texture, atPos.x - sideLen, atPos.y - sideLen, sideLen * 2, sideLen * 2);
                break;

            case BOMB:
                m_batch.draw(assetManager.get("balloon.png", Texture.class), atPos.x - sideLen, atPos.y - sideLen, sideLen * 2, sideLen * 2);
                break;
        }
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
        Vector2 pos;
        int tilesPerSide = tm.getTilemapSize();
        float rotation = tm.getRotation();
        int texWidth = texture.getWidth();
        int texHeight = texture.getHeight();

        TilemapTile tile;
        for (int y = 0; y < tilesPerSide; ++y) {
            for (int x = 0; x < tilesPerSide; ++x) {
                tile = tm.getAbsoluteTile(x, y);
                if (tile != null) {
                    pos = tile.getPositionInWorld();
                    m_batch.setColor(colorList[tile.getColor()]);
                    m_batch.draw(texture, pos.x - sideLengthHalf, pos.y - sideLengthHalf,
                            sideLengthHalf, sideLengthHalf, sideLength, sideLength,
                            1, 1, -rotation, 0, 0, texWidth, texHeight, false, false);
                }
            }
        }
    }

    public void DBdraw(CollisionManager cm, Tilemap tm) {
        Vector2 pos;
        int tilesPerSide = tm.getTilemapSize();
        int sideLen = tm.getTileSize() / 2;

        m_shapeRenderer.setColor(Color.WHITE);
        m_shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float[] vetices = cm.getVerticesOnMiddleEdges(tm.getCos(), tm.getSin());

        TilemapTile tile;
        for (int y = 0; y < tilesPerSide; ++y) {
            for (int x = 0; x < tilesPerSide; ++x) {
                tile = tm.getAbsoluteTile(x, y);
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

    public void DBTileColor(Tilemap tm) {
        int tilesPerSide = tm.getTilemapSize();
        TilemapTile tile;

        for (int y = 0; y < tilesPerSide; ++y) {
            for (int x = 0; x < tilesPerSide; ++x) {
                tile = tm.getAbsoluteTile(x, y);
                if (tile != null) {
                    Vector2 pos = tile.getPositionInWorld();
                    m_batch.setColor(Color.BLACK);
                    defaultFont.draw(m_batch, String.valueOf(tile.getColor()), pos.x -sideLengthHalf/2, pos.y);
                }
            }
        }
    }

    public void DBTileDistances(TilemapTile[][] map) {
        Vector2 pos;
        Coords2D post;
        for (TilemapTile[] arr : map) {
            for (TilemapTile tile : arr) {
                if (tile != null) {
                    pos = tile.getPositionInWorld();
                    post = tile.getRelativePosition();
                    m_batch.setColor(Color.BLACK);
                    defaultFont.draw(m_batch,String.format("%.0f,%.0f", post.x, post.y) , pos.x -sideLengthHalf/2, pos.y);
                }
            }
        }
    }

    public void drawLauncher(Queue<MovingTile> launcher, Vector2 atPos) {
//        float scale;
//        float sideLength;
        MovingTile mt;

        for (int i = 0; i < launcher.size; ++i) {
            mt = launcher.get(i);
            draw(mt);
            //            scale = mt.getScale();
//            sideLength = sideLengthHalf * scale;
//            m_batch.setColor(colorList[mt.getColor()]);
//            m_batch.draw(texture, atPos.x - sideLength, atPos.y - sideLength - i * this.sideLength, sideLength * 2, sideLength * 2);
        }
    }
}
