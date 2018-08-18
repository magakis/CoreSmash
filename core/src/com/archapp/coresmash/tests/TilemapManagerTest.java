package com.archapp.coresmash.tests;

import com.archapp.coresmash.Coords2D;
import com.archapp.coresmash.tilemap.TilemapBuilder;
import com.archapp.coresmash.tilemap.TilemapManager;
import com.badlogic.gdx.math.Vector2;

import org.junit.BeforeClass;
import org.junit.Test;

@Deprecated//("INVALID TEST .. IT'S BROKEN!")
public class TilemapManagerTest extends ProjectTestCase {
    TilemapManager tilemapManager;

    @BeforeClass
    public static void testInit() {
    }

    public TilemapManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        tilemapManager = new TilemapManager();
    }

    @Test
    public void testLayerRequests() {
        assertFalse(tilemapManager.layerExists(0));

        try {
            tilemapManager.layerExists(-1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException ignored) {
        }

        tilemapManager.newLayer();
        tilemapManager.newLayer();
        tilemapManager.newLayer();
        tilemapManager.newLayer();

        assertEquals(tilemapManager.layerCount(), 4);

        assertTrue(tilemapManager.layerExists(0));
        assertTrue(tilemapManager.layerExists(1));
        assertTrue(tilemapManager.layerExists(3));

        assertFalse(tilemapManager.layerExists(4));
        try {
            tilemapManager.layerExists(-1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException ignored) {
        }

    }

    @Test
    public void testWorldPosition() {
        TilemapBuilder builder = tilemapManager.newLayer();
        Coords2D def = tilemapManager.getDefTilemapPosition();

        assertEquals(def.x, tilemapManager.getLayerPositionX(0), 0.001);
        assertEquals(def.y, tilemapManager.getLayerPositionY(0), 0.001);

        Vector2 offset = new Vector2(100, 100);
        Vector2 origin = new Vector2(100, 100);

        builder.setOffset(offset);
        builder.setOrigin(origin);
        builder.build();

        assertEquals(def.x + origin.x + offset.x, tilemapManager.getLayerPositionX(0), 0.001);
        assertEquals(def.y + origin.y + offset.y, tilemapManager.getLayerPositionY(0), 0.001);

    }
}
