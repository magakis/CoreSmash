package com.archapp.coresmash.managers;

import com.archapp.coresmash.WorldSettings;
import com.archapp.coresmash.themes.AbstractTheme;
import com.archapp.coresmash.tilemap.Tilemap;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.archapp.coresmash.tilemap.TilemapTile;
import com.archapp.coresmash.tiles.MovingBall;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;

import java.util.List;

/**
 * Created by Michail on 24/3/2018.
 */
public class RenderManager {
    private final SpriteBatch batch;
    private final AssetManager assetManager;
    private final ShapeRenderer shapeRenderer;
    private final float sideLength = WorldSettings.getTileSize();
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

    public void drawCenterTileIndicator(TilemapManager manager) {
        TilemapTile centerTile = manager.getTilemapTile(0, 0, 0);
        if (centerTile == null) return;

        Vector2 coords = centerTile.getPositionInWorld();
        float size = WorldSettings.getTileSize() * .7f;
        float halfSize = size / 2f;
        Texture texture = assetManager.get("CenterTileIndicator.png", Texture.class);
        float rotation = (float) Math.toDegrees(manager.getLayerRotation(0));

        batch.draw(texture,
                coords.x - halfSize, coords.y - halfSize,
                halfSize, halfSize,
                size, size,
                1, 1, -rotation, 0, 0,
                texture.getWidth(), texture.getHeight(), false, false);

    }

    public void draw(MovingBall mt) {
        Vector2 atPos = mt.getPositionInWorld();
        float sideLen = sideLengthHalf * mt.getScale();

        TextureRegion texture = theme.getTexture(mt.getTile().getID());
        float drawWidthHalf = texture.getRegionWidth() / (texture.getRegionHeight() / sideLen);

        batch.draw(texture, atPos.x - drawWidthHalf, atPos.y - sideLen, drawWidthHalf * 2, sideLen * 2);
    }

    public void draw(List<MovingBall> mt) {
        for (MovingBall tile : mt) {
            draw(tile);
        }
    }

    public TextureRegion getTextureFor(int id) {
        return new TextureRegion(theme.getTexture(id));
    }

    public void draw(Tilemap tm) {
        float rotation = (float) Math.toDegrees(tm.getRotation());


        for (TilemapTile tile : tm.getTileList()) {
            Vector2 pos = tile.getPositionInWorld();
            TextureRegion texture = theme.getTexture(tile.getTileID());

            batch.draw(texture, pos.x - sideLengthHalf, pos.y - sideLengthHalf,
                    sideLengthHalf, sideLengthHalf, sideLength, sideLength,
                    1, 1, -rotation);

        }
    }

    public void drawLauncher(Queue<MovingBall> launcher, Vector2 atPos) {
        MovingBall mt;

        for (int i = 0; i < launcher.size; ++i) {
            mt = launcher.get(i);
            draw(mt);
        }
    }
}
