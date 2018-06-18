package com.breakthecore.tests;

import com.breakthecore.tilemap.TilemapManager;

import org.junit.Test;

public class TilemapManagerTest extends ProjectTestCase {
    TilemapManager tilemapManager;

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

        assertEquals(tilemapManager.getTilemapCount(), 4);

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
    public void testTilePlacement() {
        tilemapManager.getTilemapTile(0, 0, 0);

    }
}
