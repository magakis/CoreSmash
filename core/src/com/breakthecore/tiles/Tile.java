package com.breakthecore.tiles;

public class Tile {
    private TileType m_type;
    private int m_color;

    public Tile(int color) {
        m_type = TileType.REGULAR;
        m_color = color;
    }

    public int getColor() {
        return m_color;
    }

    public void setColor(int colorId) {
        m_color = colorId;
    }

    public enum TileType {
        REGULAR,

    }
}
