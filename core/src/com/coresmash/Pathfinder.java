//package com.breakthecore;
//
//import com.badlogic.gdx.utils.Pool;
//import com.badlogic.gdx.utils.Pool.Poolable;
//import Tilemap;
//import TilemapTile;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//
///**
// * Pathfinder implements a tweaked version of AStar algorithm that doesn't take into account the
// * G value (cost of path to reach tile) but only the H value (distance from target).
// *
// * I've commented out the fields that use the G value so that it can quickly be enabled if needed.
// */
//public class Pathfinder {
//    Node[][] nodeMap;
//    ArrayList<Node> opened;
//    ArrayList<Node> closed;
//    ArrayList<TilemapTile> path;
//    Pool<Node> nodePool;
//    Comparator<Node> compF;
//
//    public Pathfinder(int tileMapSize) {
//        path = new ArrayList<TilemapTile>();
//        opened = new ArrayList<Node>();
//        closed = new ArrayList<Node>();
//        nodePool = new Pool<Node>() {
//            @Override
//            protected Node newObject() {
//                return new Node();
//            }
//        };
//
//        nodeMap = new Node[tileMapSize][tileMapSize];
//
//        compF = new Comparator<Node>() {
//            @Override
//            public int compare(Node t0, Node t1) {
//                return Integer.compare(t0.F ,t1.F);
//            }
//        };
//    }
//
//    public ArrayList<TilemapTile> getPathToCenter(TilemapTile tt, Tilemap tm) {
//        Node endNode = getFastestPathForCenter(tt, tm);
//
//        if (endNode == null) {
//            reset();
//            return null;
//        }
//
//        path.clear();
//        do {
//            path.add(endNode.tile);
//            endNode = endNode.parentNode;
//        } while (endNode != null);
//
//        reset();
//        return path;
//    }
//
//    private Node getFastestPathForCenter(TilemapTile startTile, Tilemap tm) {
//        Coords2D absPos = startTile.getAbsolutePosition();
//
//        Node initNode = nodePool.obtain();
//        initNode.init(startTile);
//        initNode.parentNode = null;
//        initNode.G = 0;
//        initNode.F = initNode.H;
//        nodeMap[absPos.y][absPos.x] = initNode;
//        opened.add(initNode);
//
//        if (initNode.H == 0) return initNode;
//
//        evaluateSurroundingNodes(initNode, tm);
//
//        while (opened.size() > 0) {
//            if (opened.get(0).H == 0) {
//                return opened.get(0);
//            }
//            evaluateSurroundingNodes(opened.get(0), tm);
//            Collections.sort(opened, compF);
//        }
//
//        return null;
//    }
//
//    private void evaluateSurroundingNodes(Node midNode, Tilemap tm) {
//        int tmSize = tm.getTilemapSize();
//        int curX = midNode.x;
//        int curY = midNode.y;
//        int x, y;
//
//        closed.add(midNode);
//        opened.remove(midNode);
//
//        //top_left
//        x = curX - 1;
//        y = curY + 1;
//        if (x >= 0 && y < tmSize) {
//            evaluateNode(x, y, midNode, tm);
//        }
//
//        //top_right
//        x = curX;
//        y = curY + 1;
//        if (y < tmSize) {
//            evaluateNode(x, y, midNode, tm);
//        }
//
//        //right
//        x = curX + 1;
//        y = curY;
//        if (x < tmSize) {
//            evaluateNode(x, y, midNode, tm);
//        }
//
//        //bottom_right
//        x = curX + 1;
//        y = curY - 1;
//        if (x >= 0 && y < tmSize) {
//            evaluateNode(x, y, midNode, tm);
//        }
//
//        //bottom_left
//        x = curX;
//        y = curY - 1;
//        if (y >= 0) {
//            evaluateNode(x, y, midNode, tm);
//        }
//
//        //left
//        x = curX - 1;
//        y = curY;
//        if (x >= 0) {
//            evaluateNode(x, y, midNode, tm);
//        }
//    }
//
//    private void evaluateNode(int x, int y, Node midNode, Tilemap tm) {
//        Node node = nodeMap[y][x];
//
//        if (node == null) {
//            TilemapTile tmTile = tm.getAbsoluteTile(x, y);
//            if (tmTile == null) return;
//
//            node = nodePool.obtain();
//            node.init(tmTile);
//            node.G = midNode.G + 1;
//            node.F = node.H;//node.G + node.H;
//            node.parentNode = midNode;
//            nodeMap[y][x] = node;
//            opened.add(node);
//        } else {
//            if (!closed.contains(node)) {
//                if (midNode.G + 1 < node.G) {
//                    node.parentNode = midNode;
//                    node.G = midNode.G + 1;
//                    node.F = node.H;//node.G + node.H;
//                }
//            }
//        }
//    }
//
//    /**
//     * Has to be called <u>before</u> a result is returned because it uses an absolute position for
//     * cleanup that is taken from a TilemapTile and it might change if not reset immediately!
//     */
//    private void reset() {
//        for (Node n : opened) {
//            nodeMap[n.y][n.x] = null;
//            nodePool.free(n);
//        }
//        opened.clear();
//
//        for (Node n : closed) {
//            nodeMap[n.y][n.x] = null;
//            nodePool.free(n);
//        }
//        closed.clear();
//    }
//
//    private class Node implements Poolable {
//        Node parentNode;
//        int x, y;
//        TilemapTile tile;
//        int H;
//        int G;
//        int F;
//
//        void init(TilemapTile tmTile) {
//            tile = tmTile;
//            Coords2D absPos = tmTile.getAbsolutePosition();
//            x = absPos.x;
//            y = absPos.y;
//            H = tmTile.getDistanceFromCenter();
//        }
//
//        @Override
//        public void reset() {
//            tile = null;
//            parentNode = null;
//            H = 0;
//            G = 0;
//            F = 0;
//            x = 0;
//            y = 0;
//        }
//    }
//}
