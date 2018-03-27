package com.breakthecore;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * Created by Michail on 18/3/2018.
 */

public class TileMap {
    private int m_size;
    private int m_sideLength;
    private Vector2 m_position;
    private TilemapTile[][] m_hexTiles;

    private float m_sideLengthHalf;
    private float rotationDegrees;
    private float rotationSpeed = 360 / 10.f;

    public TileMap(Vector2 pos, int size, int sideLength) {
        m_size = size;
        m_position = pos;
        m_sideLength = sideLength;
        m_sideLengthHalf = sideLength/2.f;

        m_hexTiles = new TilemapTile[size*3][size];

        rotationDegrees = 0;

        float[] vert = Tile.getVertices();
        for (int i = 0; i < 12; ++i) {
            vert[i] *= 30;
        }

    }

    public int getSize() {
        return m_size;
    }

    public Vector2 getWorldPosition() {
        return m_position;
    }

    public void update(float delta) {
        rotationDegrees += rotationSpeed * delta;
        float rotRad = (float) Math.toRadians(rotationDegrees);

        float cosX = (float) Math.cos(rotRad);
        float sineX = (float) Math.sin(rotRad);

        for (TilemapTile[] arr : m_hexTiles) {
            for (TilemapTile hex : arr) {
                if (hex != null) {
                    updateTilemapTile(hex, cosX, sineX);
                }
            }
        }
    }

    public Tile[][] getTiles() {
        return m_hexTiles;
    }

    public void handleCollision(List<MovingTile> list) {
        TilemapTile collidedTile;
        float minDist;
        Vector2 movhexPos;
        Vector2 direction;

        for (MovingTile movhex : list){
            collidedTile = null;
            movhexPos = movhex.getPositionInWorld();
            minDist = m_sideLengthHalf + m_sideLengthHalf * movhex.getScale();


            collisionSearch:
            for (TilemapTile[] arr : m_hexTiles) {
                for (TilemapTile tile : arr) {
                    if (tile != null) {
                        if (movhexPos.dst(tile.getPositionInWorld()) < minDist) {
                            collidedTile = tile;
                            break collisionSearch;
                        }
                    }
                }
            }

            if (collidedTile == null)
                continue;

            direction = new Vector2(movhexPos).sub(collidedTile.getPositionInWorld());
            direction.nor();

            Tile.HexagonSide test = getClosestSide(collidedTile, direction);

            addTile(new TilemapTile(movhex.getColor()), collidedTile, test);
            movhex.dispose();
        }
    }

    public void updateTilemapTile(TilemapTile hex, float cos, float sin) {
        hex.setRotation(cos, sin);
        Vector2 tilePos = hex.getPositionInTilemap();
        float x = tilePos.x;
        float y = tilePos.y;

        float X_world, Y_world;
        float tileXDistance = m_sideLength + m_sideLengthHalf;
        float tileYDistance = m_sideLengthHalf;

        float xOffset = ((y) % 2 == 1) || ((y) % 2 == -1) ? m_sideLengthHalf*1.5f : 0;

        X_world = m_position.x +
                (x * tileXDistance + xOffset) * cos +
                (y * tileYDistance) * sin;

        Y_world = m_position.y +
                (x * tileXDistance + xOffset) * -sin +
                (y * tileYDistance) * cos;

        hex.setPositionInWorld(X_world, Y_world);
    }

    public void addTile(TilemapTile newHex, TilemapTile tile, Tile.HexagonSide side) {
        int centerTile = m_size / 2;
        int xOffset;
        Vector2 tilePos;

        switch (side) {
            case top:
                tilePos = tile.getPositionInTilemap();
                if (m_hexTiles[(int) tilePos.y + centerTile + 2][(int) tilePos.x + centerTile] == null) {
                    newHex.setPositionInTilemap(tilePos.x, tilePos.y + 2);
                    m_hexTiles[(int) tilePos.y + centerTile + 2][(int) tilePos.x + centerTile] = newHex;
                }
                break;
            case topLeft:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y + 1);
                    m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
            case topRight:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y + 1);
                    m_hexTiles[(int) tilePos.y + centerTile + 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
            case bottom:
                tilePos = tile.getPositionInTilemap();
                if (m_hexTiles[(int) tilePos.y + centerTile - 2][(int) tilePos.x + centerTile] == null) {
                    newHex.setPositionInTilemap(tilePos.x, tilePos.y - 2);
                    m_hexTiles[(int) tilePos.y + centerTile - 2][(int) tilePos.x + centerTile] = newHex;
                }
                break;
            case bottomLeft:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y - 1);
                    m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
            case bottomRight:
                tilePos = tile.getPositionInTilemap();
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] == null) {
                    newHex.setPositionInTilemap(tilePos.x + xOffset, tilePos.y - 1);
                    m_hexTiles[(int) tilePos.y + centerTile - 1][(int) tilePos.x + centerTile + xOffset] = newHex;
                }
                break;
        }
        updateTilemapTile(newHex,tile.getCosTheta(), tile.getSinTheta());
    }

    ///
    //  hex and point should be in unit length
    ///
    public Tile.HexagonSide getClosestSide(Tile hex, Vector2 point) {
        float[] vertices = hex.getVerticesOnMiddleEdges();

        Vector2 hPos = hex.getPositionInWorld();
        float sl = m_sideLengthHalf;

//        ShapeRenderer db = DebugRenderer.get().getShapeRenderer();
//        db.begin(ShapeRenderer.ShapeType.Filled);
//        db.setColor(Color.RED);
//        db.circle(hPos.x+point.x*sl, point.y*sl+hPos.y, 2);
//        db.setColor(Color.BLUE);
//        db.circle(vertices[0]*sl+hPos.x, vertices[1]*sl+hPos.y, 4);
//        db.setColor(Color.GREEN);
//        db.circle(vertices[2]*sl+hPos.x, vertices[3]*sl+hPos.y, 4);
//        db.setColor(Color.MAGENTA);
//        db.circle(vertices[4]*sl+hPos.x, vertices[5]*sl+hPos.y, 4);
//        db.setColor(Color.CYAN);
//        db.circle(vertices[6]*sl+hPos.x, vertices[7]*sl+hPos.y, 4);
//        db.setColor(Color.GOLD);
//        db.circle(vertices[8]*sl+hPos.x, vertices[9]*sl+hPos.y, 4);
//        db.setColor(Color.WHITE);
//        db.circle(vertices[10]*sl+hPos.x, vertices[11]*sl+hPos.y, 4);
//
//        db.end();

        float topLeft = Vector2.dst(vertices[0], vertices[1], point.x, point.y);
        float top = Vector2.dst(vertices[2], vertices[3], point.x, point.y);
        float topRight = Vector2.dst(vertices[4], point.x, vertices[5], point.y);
        float bottomRight = Vector2.dst(vertices[6], vertices[7], point.x, point.y);
        float bottom = Vector2.dst(vertices[8], vertices[9], point.x, point.y);
        float bottomLeft = Vector2.dst(vertices[10], vertices[11], point.x, point.y);

        float closestSideLength = bottomRight;
        Tile.HexagonSide closestSide = Tile.HexagonSide.bottomRight;

        if (top < closestSideLength) {
            closestSideLength = top;
            closestSide = Tile.HexagonSide.top;
        }
        if (bottomLeft < closestSideLength) {
            closestSideLength = bottomLeft;
            closestSide = Tile.HexagonSide.bottomLeft;
        }
        if (topLeft < closestSideLength) {
            closestSideLength = topLeft;
            closestSide = Tile.HexagonSide.topLeft;
        }
        if (bottom < closestSideLength) {
            closestSideLength = bottom;
            closestSide = Tile.HexagonSide.bottom;
        }
        if (topRight < closestSideLength) {
            closestSideLength = topRight;
            closestSide = Tile.HexagonSide.topRight;
        }

        return closestSide;
    }

    public static class TilemapTile extends Tile {
        private Vector2 m_positionInTilemap;

        public TilemapTile(float tilex , float tiley) {
            m_positionInTilemap = new Vector2(tilex, tiley);
        }

        public TilemapTile(float tilex, float tiley, int colorid) {
            this(tilex, tiley);
            setColor(colorid);
        }

        public TilemapTile(int color) {
            super(color);
            m_positionInTilemap = new Vector2();
        }

        public Vector2 getPositionInTilemap() {
            return m_positionInTilemap;
        }

        public void setPositionInTilemap(float tilex, float tiley) {
            m_positionInTilemap.set(tilex, tiley);
        }
    } //class TilemapTile

}