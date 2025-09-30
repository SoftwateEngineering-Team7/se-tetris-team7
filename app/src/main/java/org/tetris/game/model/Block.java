package org.tetris.game.model;

import org.extension.Point;

public class Block {
    protected int[][] shape;
    protected Point pivot;

    public Block(int[][] shape, Point pivot) {
        this.shape = shape;
        this.pivot = pivot;
    }

    public int getShape(int r, int c) {
        return shape[r][c];
    }

    public int height() {
        return shape.length;
    }

    public int width() {
        if (shape.length > 0)
            return shape[0].length;
        return 0;
    }
}