package com.coresmash.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.coresmash.themes.AbstractTheme;
import com.coresmash.tilemap.Tilemap;
import com.coresmash.tilemap.TilemapTile;
import com.coresmash.tiles.MovingBall;
import com.coresmash.tiles.Tile;

import java.util.List;

/**
 * Created by Michail on 24/3/2018.
 */
public class RenderManager {
    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final ShapeRenderer shapeRenderer;
    private final float sideLength = com.coresmash.WorldSettings.getTileSize();
    private final float sideLengthHalf = sideLength / 2.f;
    private AbstractTheme theme;

    public RenderManager(AssetManager am) {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        assetManager = am;
    }

    public SpriteBatch spriteBatchBegin(Matrix4 combined) {
        batch.setProjectionMatrix(combined);
        batch.setColor(Color.WHITE);
        batch.begin();
        return batch;
    }

    public void setColorTint(Color color) {
        batch.setColor(color);
    }

    public void setTheme(AbstractTheme newTheme) {
        if (theme != null) {
            theme.dispose();
        }
        theme = newTheme;
    }

    public void spriteBatchEnd() {
        batch.end();
    }

    public ShapeRenderer shapeRendererStart(Matrix4 combined, ShapeRenderer.ShapeType shapeType) {
        shapeRenderer.setProjectionMatrix(combined);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.begin(shapeType);
        return shapeRenderer;
    }

    public void shapeRendererEnd() {
        shapeRenderer.end();
    }

    public void renderCenterDot(com.coresmash.Coords2D pos, Matrix4 combined) {
        shapeRenderer.setProjectionMatrix(combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GOLDENROD);
        shapeRenderer.circle(pos.x, pos.y, 15);
        shapeRenderer.end();
    }

    public void draw(MovingBall mt) {
        Vector2 atPos = mt.getPositionInWorld();
        Tile tile = mt.getTile();
        float sideLen = sideLengthHalf * mt.getScale();

        Texture texture = theme.getTexture(tile.getID());

        batch.draw(texture, atPos.x - sideLen, atPos.y - sideLen, sideLen * 2, sideLen * 2);
    }

    public void draw(List<MovingBall> mt) {
        for (MovingBall tile : mt) {
            draw(tile);
        }
    }

    public Texture getTextureFor(int id) {
        return new Texture(theme.getTexture(id).getTextureData());
    }

    public void draw(Tilemap tm) {
        for (TilemapTile tile : tm.getTileList()) {
            Vector2 pos = tile.getPositionInWorld();
            Texture texture = theme.getTexture(tile.getTileID());
            float rotation = (float) Math.toDegrees(tm.getRotation());

            batch.draw(texture, pos.x - sideLengthHalf, pos.y - sideLengthHalf,
                    sideLengthHalf, sideLengthHalf, sideLength, sideLength,
                    1, 1, -rotation, 0, 0, texture.getWidth(), texture.getHeight(), false, false);

        }
    }
//
//    public void DBdraw(CollisionDetector cm, Tilemap tm) {
//        Vector2 pos;
//        int tilesPerSide = tm.getTilemapSize();
//        int sideLen = tm.getTileSize() / 2;
//
//        shapeRenderer.setColor(Color.WHITE);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        float[] vetices = cm.getVerticesOnMiddleEdges(tm.getCos(), tm.getSin());
//
//        TilemapTile tile;
//        for (int y = 0; y < tilesPerSide; ++y) {
//            for (int x = 0; x < tilesPerSide; ++x) {
//                tile = tm.getAbsoluteTile(x, y);
//                if (tile != null) {
//                    pos = tile.getPositionInWorld();
//
//                    for (int i = 0; i < 12; i += 2) {
//                        shapeRenderer.circle(vetices[i] * sideLen + pos.x, vetices[i + 1] * sideLen + pos.y, 3);
//                    }
//                }
//            }
//        }
//        shapeRenderer.spriteBatchEnd();
//    }

//    public void DBTileColor(Tilemap tm) {
//        int tilesPerSide = tm.getTilemapSize();
//        TilemapTile tile;
//
//        for (int y = 0; y < tilesPerSide; ++y) {
//            for (int x = 0; x < tilesPerSide; ++x) {
//                tile = tm.getAbsoluteTile(x, y);
//                if (tile != null) {
//                    Vector2 pos = tile.getPositionInWorld();
//                    batch.setColor(Color.BLACK);
//                    defaultFont.draw(batch, String.valueOf(tile.getTileID()), pos.x - sideLengthHalf / 2, pos.y);
//                }
//            }
//        }
//    }

//    public void DBTileDistances(Tilemap map) {
//        Vector2 pos;
//        Coords2D post;
//        for (int y = 0; y < map.getTilemapSize(); ++y) {
//            for (int x = 0; x < map.getTilemapSize(); ++x) {
//                TilemapTile tile = map.getAbsoluteTile(x, y);
//                if (tile != null) {
//                    pos = tile.getPositionInWorld();
//                    post = tile.getCoords();
//                    batch.setColor(Color.BLACK);
//                    defaultFont.draw(batch, String.format(Locale.ENGLISH, "%d,%d", post.x, post.y), pos.x - sideLengthHalf / 2, pos.y);
//                }
//            }
//        }
//    }

    public void drawLauncher(Queue<MovingBall> launcher, Vector2 atPos) {
//        float scale;
//        float sideLength;
        MovingBall mt;

        for (int i = 0; i < launcher.size; ++i) {
            mt = launcher.get(i);
            draw(mt);
//            scale = mt.getScale();
//            sideLength = sideLengthHalf * scale;
//            batch.setColor(colorList[mt.getCurrentTileID()]);
//            batch.draw(texture, atPos.x - sideLength, atPos.y - sideLength - i * this.sideLength, sideLength * 2, sideLength * 2);
        }
    }
}
