package com.breakthecore;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class TilemapManager extends Observable implements Observer {
    private Tilemap tm;
    private CollisionManager m_collisionManager;
    private ArrayList<TilemapTile> match = new ArrayList<TilemapTile>();
    private ArrayList<TilemapTile> exclude = new ArrayList<TilemapTile>();
    private boolean isRotating = true;
    private float rotationDegrees = 0;
    private Pathfinder m_pathfinder;
    private float initTileCount;
    private float minRotSpeed = 0;
    private float maxRotSpeed = 0;

    private Vector2 direction;
    private float maxRotAddedSpeed;
    private float rotationSpeed;

    public TilemapManager(Tilemap map) {
        tm = map;
        m_collisionManager = new CollisionManager();
        m_pathfinder = new Pathfinder();
        direction = new Vector2();
        maxRotAddedSpeed = maxRotSpeed - minRotSpeed;
    }

    public void update(float delta) {
        if (isRotating) {
            rotationSpeed = MathUtils.clamp(maxRotSpeed - maxRotAddedSpeed * (tm.getTileCount() / initTileCount), minRotSpeed, maxRotSpeed);
            rotationDegrees += rotationSpeed * delta;
            tm.setRotation(rotationDegrees);
        }
//        onNotify(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
    }

    public void setMinMaxRotationSpeed(float min, float max) {
        minRotSpeed = min;
        maxRotSpeed = max;
        maxRotAddedSpeed = maxRotSpeed - minRotSpeed;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public Vector2 getDBDirection() {
        return direction;
    }

    public void checkForCollision(List<MovingTile> list) {
        TilemapTile solidTile;

        for (MovingTile mt : list) {
            solidTile = m_collisionManager.findCollision(tm, mt);
            if (solidTile == null) continue;

            direction.set(mt.m_positionInWorld).sub(solidTile.getPositionInWorld());
            direction.nor();

            addTile(mt.getColor(), solidTile, m_collisionManager.getClosestSides(solidTile, direction));

            mt.dispose();
        }

    }

    public void setAutoRotation(boolean autoRotation) {
        isRotating = autoRotation;
    }

    public boolean addTile(int color, TilemapTile solidTile, Tile.Side[] sides) {
        for (int i = 0; i < 6; ++i) {
            if (addTile(color, solidTile, sides[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean addTile(int color, TilemapTile solidTile, Tile.Side side) {
        TilemapTile newTile = null;
        boolean placedNewTile = false;
        Vector2 tilePos = solidTile.getPositionInTilemap();
        int xOffset;
        switch (side) {
            case top:
                if (tm.getTile((int) tilePos.x, (int) tilePos.y + 2) == null) {
                    newTile = createTilemapTile(color);
                    tm.setTile((int) tilePos.x, (int) tilePos.y + 2, newTile);
                    placedNewTile = true;
                }
                break;
            case topLeft:
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y + 1) == null) {
                    newTile = createTilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case topRight:
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y + 1) == null) {
                    newTile = createTilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y + 1, newTile);
                    placedNewTile = true;
                }
                break;
            case bottom:
                if (tm.getTile((int) tilePos.x, (int) tilePos.y - 2) == null) {
                    newTile = createTilemapTile(color);
                    tm.setTile((int) tilePos.x, (int) tilePos.y - 2, newTile);
                    placedNewTile = true;
                }
                break;
            case bottomLeft:
                xOffset = (tilePos.y % 2) == 0 ? -1 : 0;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y - 1) == null) {
                    newTile = createTilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
            case bottomRight:
                xOffset = (tilePos.y % 2) == 0 ? 0 : 1;
                if (tm.getTile((int) tilePos.x + xOffset, (int) tilePos.y - 1) == null) {
                    newTile = createTilemapTile(color);
                    tm.setTile((int) tilePos.x + xOffset, (int) tilePos.y - 1, newTile);
                    placedNewTile = true;
                }
                break;
        }
        if (placedNewTile) {
            checkForColorMatches(newTile);
            return true;
        }
        return false;
    }

    public TilemapTile createTilemapTile(int color) {
        TilemapTile res = new TilemapTile(color);
        res.addObserver(this);
        notifyObservers(NotificationType.NOTIFICATION_TYPE_NEW_TILE_CREATED, null);
        return res;
    }

    public void checkForColorMatches(TilemapTile tile) {
        boolean centerTileBroke = false;
        match.clear();
        exclude.clear();

        addSurroundingColorMatches(tile, match, exclude);

        if (match.size() < 3) {
            return;
        }

        for (TilemapTile t : match) {
            if (t.getDistanceFromCenter() == 0) {
                centerTileBroke = true;
            }
            tm.desrtoyTile((int) t.getPositionInTilemap().x, (int) t.getPositionInTilemap().y);
        }

        if (centerTileBroke) {
            notifyObservers(NotificationType.NOTIFICATION_TYPE_CENTER_TILE_DESRTOYED, null);
        }

        ArrayList<TilemapTile> deadTiles = m_pathfinder.getDeadTiles(tm);
        for (TilemapTile t : deadTiles) {
            tm.desrtoyTile((int) t.getPositionInTilemap().x, (int) t.getPositionInTilemap().y);
        }
    }

    //FIXME: Somehow I matched three on the outside and the middle tile got removed??!?
    //XXX: HORRIBLE CODE! DON'T READ OR YOUR BRAIN MIGHT CRASH! Blehh..
    public void addSurroundingColorMatches(TilemapTile tile, List<TilemapTile> match, List<TilemapTile> exclude) {
        Vector2 tpos = tile.getPositionInTilemap();
        boolean isLeft = tpos.y % 2 == 0 ? true : false;
        int tx = (int) tpos.x;
        int ty = (int) tpos.y;

        TilemapTile tt;
        boolean flag = true;
        int x;

        match.add(tile);
        exclude.add(tile);


        for (int y = -2; y < 3; ++y) {
            if (y == 0) continue;
            if ((flag) && (y == -1 || y == +1)) {
                x = isLeft ? -1 : 1;
            } else {
                x = 0;
            }
            tt = tm.getTile(tx + x, ty + y);

            if (flag && y == 1) {
                flag = false;
                y = -2;
            }

            if (tt == null || exclude.contains(tt))
                continue;

            if (tt.getColor() == tile.getColor()) {
                addSurroundingColorMatches(tt, match, exclude);
            }
        }
    }


    public void initHexTilemap(Tilemap tm, int radius) {
        TilemapTile dummy;
        if (radius == 0) {
            dummy = new TilemapTile(WorldSettings.getRandomInt(7));
            dummy.addObserver(this);
            tm.setTile(0, 0, dummy);
            return;
        }

        for (int y = -radius * 3; y < radius * 3; ++y) {
            float xOffset = ((y) % 2 == 0) ? 0 : .75f;
            for (int x = -radius; x < radius; ++x) {
                if (Vector2.dst(x * 1.5f + xOffset, y * 0.5f, 0, 0) <= radius) {
                    dummy = new TilemapTile(WorldSettings.getRandomInt(7));
                    dummy.addObserver(this);
                    tm.setTile(x, y, dummy);
                }
            }
        }
        initTileCount = tm.getTileCount();
    }

    private void fillEntireTilemap(Tilemap tm) {
        Tile[][] tiles = tm.getTilemapTiles();
        int center_tile = tm.getSize() / 2;
        int oddOrEvenFix = tm.getSize() % 2 == 1 ? 1 : 0;
        int tmp = (tm.getSize() * 3) / 2;
        for (int y = -tmp; y < tmp; ++y) {
            for (int x = -tm.getSize() / 2; x < tm.getSize() / 2 + oddOrEvenFix; ++x) {
                tiles[y + (center_tile) * 3 + oddOrEvenFix][x + center_tile] = new TilemapTile(
                        x, y,
                        WorldSettings.getRandomInt(7));
            }
        }
    }

    @Override
    public void onNotify(NotificationType type, Object ob) {
        notifyObservers(type, ob);
    }
}
