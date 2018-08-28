package com.archapp.coresmash.themes;

public enum PropType {
    CENTER_TILE_INDICATOR(1);

    private int id;

    PropType(int id) {
        this.id = id + 10_000;
    }

    public int getID() {
        return id;
    }
}
