package org.tetris.game.model;

public class Point {
    public int r;
    public int c;

    public Point(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public Point(Point other) {
        this.r = other.r;
        this.c = other.c;
    }

    public Point up() {
        return new Point(r - 1, c);
    }

    public Point down() {
        return new Point(r + 1, c);
    }

    public Point right() {
        return new Point(r, c + 1);
    }

    public Point left() {
        return new Point(r, c - 1);
    }
}
