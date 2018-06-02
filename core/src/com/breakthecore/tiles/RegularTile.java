package com.breakthecore.tiles;

import com.breakthecore.GameController;
import com.breakthecore.tilemap.TilemapTile;

public abstract class RegularTile extends Tile {
    static RegularTile newBall(int id) {
        switch (id) {
            case 0:
                return new RegularTile0();
            case 1:
                return new RegularTile1();
            case 2:
                return new RegularTile2();
            case 3:
                return new RegularTile3();
            case 4:
                return new RegularTile4();
            case 5:
                return new RegularTile5();
            case 6:
                return new RegularTile6();
            case 7:
                return new RegularTile7();
            case 8:
                return new RegularTile8();
            case 9:
                return new RegularTile9();
            case 10:
                return new RegularTile10();
            case 11:
                return new RegularTile11();
            case 12:
                return new RegularTile12();
            case 13:
                return new RegularTile13();
            case 14:
                return new RegularTile14();
            case 15:
                return new RegularTile15();
            default:
                throw new IllegalArgumentException("Not a Regular Tile ID: " + id);
        }
    }

    /* Disable constructor */
    private RegularTile() {
    }

    @Override
    public void onCollide(MovingBall movingBall, TilemapTile tileHit, GameController.BehaviourPowerPack pack) {
        TilemapTile tile = pack.tilemapManager.attachBall(movingBall,tileHit,pack.collisionDetector);
        assert (tile != null);
        pack.tilemapManager.handleColorMatchesFor(tile);
    }

    @Override
    public void update(float delta) {

    }

    private static class RegularTile0 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(0);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile1 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(1);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile2 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(2);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile3 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(3);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile4 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(4);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile5 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(5);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile6 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(6);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile7 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(7);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile8 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(8);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile9 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(9);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile10 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(10);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile11 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(11);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile12 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(12);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile13 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(13);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile14 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(14);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

    private static class RegularTile15 extends RegularTile {
        private final static TileAttributes ballAttr = TileDictionary.getAttributesFor(15);

        @Override
        public boolean isMatchable() {
            return ballAttr.isMatchable();
        }

        @Override
        public boolean isBreakable() {
            return ballAttr.isBreakable();
        }

        @Override
        public boolean isPlaceable() {
            return ballAttr.isPlaceable();
        }

        @Override
        public TileType getTileType() {
            return ballAttr.getTileType();
        }

        @Override
        public int getID() {
            return ballAttr.getID();
        }

        @Override
        public TileAttributes getTileAttributes() {
            return ballAttr;
        }

    }

}
