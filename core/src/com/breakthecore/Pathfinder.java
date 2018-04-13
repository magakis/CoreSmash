package com.breakthecore;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.breakthecore.tiles.TilemapTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Pathfinder {
    Node[][] nodeMap;
    ArrayList<Node> opened;
    ArrayList<Node> closed;
    ArrayList<Node> invalid;
    ArrayList<TilemapTile> path;
    Comparator<Node> compF;

    public Pathfinder(int tileMapSize) {
        path = new ArrayList<TilemapTile>();
        opened = new ArrayList<Node>();
        closed = new ArrayList<Node>();
        invalid = new ArrayList<Node>();
        int centerTile = tileMapSize/2;

        nodeMap = new Node[tileMapSize][tileMapSize];
        for (int y = 0; y < tileMapSize; ++y) {
            for (int x = 0; x < tileMapSize; ++x) {
                nodeMap[y][x] = new Node(x - centerTile, y - centerTile);
            }
        }

        compF = new Comparator<Node>() {
            @Override
            public int compare(Node t0, Node t1) {
                return t0.F < t1.F ? -1 : 1;
            }
        };
    }

/*
    public boolean isConnectedToCenterTile(TilemapTile tt, Tilemap tm) {
        opened.clear();
        closed.clear();

        opened.add();
        while (true) {
            if (curr.getDistanceFromCenter() == 0) return true;
            getSurroundingTiles(curr, tm, opened, closed);
            if (opened.size() == 0) return false;
            Collections.sort(opened, tileValueComp);
            curr = opened.getFirst();
            closed.add(curr);
            opened.remove(curr);
        }
    }
*/
    private void getSurroundingTiles(TilemapTile tmt, Tilemap tm, LinkedList<TilemapTile> active, ArrayList<TilemapTile> exclude) {
        Vector2 tpos = tmt.getRelativePositionInTilemap();
        int tx = (int) tpos.x;
        int ty = (int) tpos.y;

        TilemapTile tt;

        //top_left
        tt = tm.getRelativeTile(tx - 1, ty + 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //top_right
        tt = tm.getRelativeTile(tx, ty + 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //right
        tt = tm.getRelativeTile(tx + 1, ty);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //bottom_right
        tt = tm.getRelativeTile(tx + 1, ty - 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }


        //bottom_left
        tt = tm.getRelativeTile(tx, ty - 1);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

        //left
        tt = tm.getRelativeTile(tx - 1, ty);
        if (tt != null && !exclude.contains(tt) && !active.contains(tt)) {
            active.add(tt);
        }

    }

    public ArrayList<TilemapTile> getPathToCenter(TilemapTile tt, Tilemap tm) {
        Node endNode = getFastestPathForCenter(tt, tm);

        if (endNode == null) return null;

        path.clear();
        do {
            path.add(endNode.tile);
            endNode = endNode.parentNode;
        } while(endNode != null);

        return path;
    }

    private Node getFastestPathForCenter(TilemapTile startTile, Tilemap tm) {
        Vector2 absolutePosition = startTile.getAbsolutePositionInTilemap();
        int centerTilePos = tm.getCenterTilePos();

        Node initNode = nodeMap[(int) absolutePosition.y][(int) absolutePosition.x];
        initNode.parentNode = null;
        initNode.tile = startTile;
        initNode.G = 0;
        initNode.F = initNode.H;

        if (initNode.H == 0) return initNode;

        opened.clear();
        closed.clear();
        invalid.clear();

        evaluateSurroundingNodes(initNode, tm);

        while (opened.size() > 0) {
            if (opened.get(0).H == 0) {
                return opened.get(0);
            }
            evaluateSurroundingNodes(opened.get(0), tm);

            Collections.sort(opened, compF);
        }

        return null;
    }

    private void evaluateSurroundingNodes(Node midNode, Tilemap tm) {
        Vector2 posT = midNode.tile.getAbsolutePositionInTilemap();
        int tmSize = tm.getSize();

        int curX = (int) posT.x;
        int curY = (int) posT.y;
        int x, y;

        closed.add(midNode);
        opened.remove(midNode);

        //top_left
        x = curX - 1;
        y = curY + 1;
        if (x >= 0 && y < tmSize) {
            evaluateNode(x, y, midNode, tm);
        }

        //top_right
        x = curX;
        y = curY + 1;
        if (y < tmSize) {
            evaluateNode(x, y, midNode, tm);
        }

        //right
        x = curX + 1;
        y = curY;
        if (x < tmSize) {
            evaluateNode(x, y, midNode, tm);
        }

        //bottom_right
        x = curX + 1;
        y = curY - 1;
        if (x >= 0 && y < tmSize) {
            evaluateNode(x, y, midNode, tm);
        }

        //bottom_left
        x = curX;
        y = curY - 1;
        if (y >= 0) {
            evaluateNode(x, y, midNode, tm);
        }

        //left
        x = curX - 1;
        y = curY;
        if (x >= 0) {
            evaluateNode(x, y, midNode, tm);
        }
    }

    private void evaluateNode(int x, int y, Node midNode, Tilemap tm) {
        Node node = nodeMap[y][x];
        if (!closed.contains(node) && !invalid.contains(node)) {
            if (opened.contains(node)) {
                if (midNode.G + 1 < node.G) {
                    node.parentNode = midNode;
                    node.G = midNode.G + 1;
                    node.F = node.G + node.H;
                }
            } else {
                node.tile = tm.getAbsoluteTile(x, y);
                if (node.tile == null) {
                    invalid.add(node);
                } else {
                    node.G = midNode.G + 1;
                    node.F = node.G + node.H;
                    node.parentNode = midNode;
                    opened.add(node);
                }
            }
        }
    }

    public int getTileDistance(int aX1, int aY1, int aX2, int aY2) {
        int dx = aX1 - aX2;     // signed deltas
        int dy = aY1 - aY2;
        int x = Math.abs(dx);  // absolute deltas
        int y = Math.abs(dy);

        return Math.max(x, Math.max(y, Math.abs(dx + dy)));
    }

    private class Node implements Poolable {
        final Coords2D relativePosition;
        final int H;

        TilemapTile tile;

        Node parentNode;
        int G;
        int F;

        private Node(int x, int y) {
            relativePosition = new Coords2D(x,y);
            H = getTileDistance(x, y,0,0);
        }


        @Override
        public void reset() {
            tile = null;
            parentNode = null;
        }
    }
}
