package org.tetris.game.model;

public class Block {
    protected int[][] shape;
    protected Point pivot;
    protected BlockType type;

    public Block(int[][] shape, Point pivot, BlockType type) {
        this.shape = shape;
        this.pivot = pivot;
        this.type = type;
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