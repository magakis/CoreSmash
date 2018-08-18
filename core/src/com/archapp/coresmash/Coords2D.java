package com.archapp.coresmash;

public class Coords2D {
    public int x, y;

    public Coords2D(){};

    public Coords2D(Coords2D coords2D) {
        x = coords2D.x;
        y = coords2D.y;
    }

    public Coords2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
