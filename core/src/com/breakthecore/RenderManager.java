package com.breakthecore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.utils.Queue;

import java.util.List;

/**
 * Created by Michail on 24/3/2018.
 */

public class RenderManager {
    private SpriteBatch m_batch;
    private ShapeRenderer m_shapeRenderer;
    private float sideLength;
    private float sideLengthHalf;

    private Texture texture;
    private Color[] colorList;

    public RenderManager(int sideLen, int colorCount) {
        m_batch = new SpriteBatch();
        m_shapeRenderer = new ShapeRenderer();
        texture = new Texture("ball.png");
        sideLength = sideLen;
        sideLengthHalf = sideLength /2.f;
        colorList = new Color[colorCount];

        colorList[0] = new Color(1,0,0,1);
        colorList[1] = new Color(1,1,0,1);;
        colorList[2] = new Color(1,1,1,1);;
        colorList[3] = new Color(0,1,0,1);;
        colorList[4] = new Color(1,0,1,1);;
        colorList[5] = new Color(0,1,1,1);;
        colorList[6] = new Color(0,0,1,1);;
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
        m_shapeRenderer.setColor(Color.GOLD);
        m_shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        m_shapeRenderer.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - Gdx.graphics.getHeight() / 4, 5);
        m_shapeRenderer.end();

    }

    public void draw(MovingTile mt) {
        Vector2 atPos = mt.getPositionInWorld();
        float sideLen = sideLengthHalf*mt.getScale();
        m_batch.setColor(colorList[mt.getColor()]);
        m_batch.draw(texture, atPos.x - sideLen, atPos.y - sideLen, sideLen*2, sideLen*2);
    }

    public void draw(List<MovingTile> mt) {
        for (MovingTile tile : mt) {
            draw(tile);
        }
    }

    public void draw(TileMap tm) {
        Tile[][]map = tm.getTiles();
        Vector2 pos;

        for (Tile[] arr : map) {
            for (Tile tile : arr) {
                if (tile != null) {
                    pos = tile.getPositionInWorld();
                    m_batch.setColor(colorList[tile.getColor()]);
                    m_batch.draw(texture,pos.x-sideLengthHalf, pos.y-sideLengthHalf , sideLength, sideLength);
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
            m_batch.draw(texture, atPos.x - sideLength, atPos.y - sideLength - i* this.sideLength, sideLength * 2, sideLength * 2);
        }
    }
}
