package com.monstertoss.swl;

import android.graphics.Point;

public class Dot extends Point {
    int iX;
    int iY;

    Dot(int iX, int iY, int x, int y) {
        super(x, y);
        this.iX = iX;
        this.iY = iY;
    }

    void set(int iX, int iY, int x, int y) {
        super.set(x, y);
        this.iX = iX;
        this.iY = iY;
    }

    boolean isValid() {
        return iX >= 0 && iY >= 0 && x >= 0 && y >= 0;
    }

    public String toString() {
        return "Dot(" + iX + "," + iY + ")";
    }
}